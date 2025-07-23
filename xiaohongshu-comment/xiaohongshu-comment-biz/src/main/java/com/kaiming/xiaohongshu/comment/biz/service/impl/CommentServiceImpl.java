package com.kaiming.xiaohongshu.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.constant.DateConstants;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentDO;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentLikeDO;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentDOMapper;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentLikeDOMapper;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.NoteCountDOMapper;
import com.kaiming.xiaohongshu.comment.biz.enums.*;
import com.kaiming.xiaohongshu.comment.biz.model.dto.LikeUnlikeCommentMqDTO;
import com.kaiming.xiaohongshu.comment.biz.model.dto.PublishCommentMqDTO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.*;
import com.kaiming.xiaohongshu.comment.biz.retry.SendMqRetryHelper;
import com.kaiming.xiaohongshu.comment.biz.rpc.DistributedIdGeneratorRpcService;
import com.kaiming.xiaohongshu.comment.biz.rpc.KeyValueRpcService;
import com.kaiming.xiaohongshu.comment.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.comment.biz.service.CommentService;
import com.kaiming.xiaohongshu.kv.dto.req.FindCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;


import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: CommentServiceImpl
 * Package: com.kaiming.xiaohongshu.comment.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Autowired
    private UserRpcService userRpcService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private CommentLikeDOMapper commentLikeDOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;

    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000)     // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(1, TimeUnit.HOURS)    // 设置缓存条目在写入后 1 小时过期
            .build();

    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {

        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图像
        String imageUrl = publishCommentReqVO.getImageUrl();
        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 当前登录用户Id, 发布者Id
        Long creatorId = LoginUserContextHolder.getUserId();
        // RPC: 调用分布式 ID 生成服务, 评论内容Id
        String commentId = distributedIdGeneratorRpcService.generateCommentId();
        // 发布 MQ 消息
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .commentId(Long.valueOf(commentId))
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        sendMqRetryHelper.send(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));
        return Response.success();
    }

    /**
     * 评论内容分页查询
     *
     * @param findCommentPageListReqVO
     * @return
     */
    @Override
    public PageResponse<FindCommentItemRespVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        // 笔记 Id
        Long noteId = findCommentPageListReqVO.getNoteId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        // 每页展示的评论数为10
        long pageSize = 10;

        // Redis 缓存中查找
        String noteCommentTotalKey = RedisKeyConstants.buildNoteCommentTotalKey(noteId);
        Number commentTotal = (Number) redisTemplate.opsForHash()
                .get(noteCommentTotalKey, RedisKeyConstants.FIELD_COMMENT_TOTAL);
        long count = Objects.isNull(commentTotal) ? 0L : commentTotal.longValue();

        // 若缓存不存在, 查询数据库
        if (Objects.isNull(commentTotal)) {
            // 查询评论总数 (从 t_note_count 笔记计数表查，提升查询性能, 避免 count(*))
            Long dbCount = noteCountDOMapper.selectCommentTotalByNoteId(noteId);

            // 若数据库不存在，抛出异常
            if (Objects.isNull(dbCount)) {
                throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
            }
            count = dbCount;
            // 异步同步到 Redis
            threadPoolTaskExecutor.submit(() -> syncNoteCommentTotal2Redis(noteCommentTotalKey, dbCount));
        }

        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }
        // 分页返回参数
        List<FindCommentItemRespVO> commentRespVOS = Lists.newArrayList();

        // 计算分页查询的偏移量 offset
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 评论分页缓存使用 ZSET + STRING 实现
        // 构建评论 ZSET Key
        String commentZSetKey = RedisKeyConstants.buildCommentListKey(noteId);
        boolean hasKey = redisTemplate.hasKey(commentZSetKey);
        // 不存在
        if (!hasKey) {
            // 异步将热点评论同步到 redis 中（最多同步 500 条）
            threadPoolTaskExecutor.submit(() -> syncHeatComments2Redis(commentZSetKey, noteId));
        }

        // 若 ZSET 缓存存在, 并且查询的是前 50 页的评论
        if (hasKey && offset < 500) {
            // 使用 ZRevRange 获取某篇笔记下，按热度降序排序的一级评论 ID
            Set<Object> commentIds = redisTemplate.opsForZSet()
                    .reverseRangeByScore(commentZSetKey, -Double.MAX_VALUE, Double.MAX_VALUE, offset, pageSize);
            // 结果不为空
            if (CollUtil.isNotEmpty(commentIds)) {
                // Set 转 List
                List<Object> commentIdList = Lists.newArrayList(commentIds);

                // 先查询本地缓存
                // 新建一个集合，用于存储本地缓存中不存在的评论 ID
                List<Long> localCacheExpiredCommentIds = Lists.newArrayList();

                // 构建本地缓存的 Key
                List<Long> localCacheKeys = commentIdList.stream()
                        .map(commentId -> Long.valueOf(commentId.toString()))
                        .toList();
                // 批量查询本地缓存
                Map<Long, String> commentIdAndDetailJsonMap = LOCAL_CACHE.getAll(localCacheKeys, missingKeys -> {
                    // 对于本地缓存中缺失的 key，返回空字符串
                    Map<Long, String> missingData = Maps.newHashMap();
                    missingKeys.forEach(key -> {
                        // 记录缓存中不存在的评论 ID
                        localCacheExpiredCommentIds.add(key);
                        // 不存在的评论详情, 对其 Value 值设置为空字符串
                        missingData.put(key, Strings.EMPTY);
                    });
                    return missingData;
                });

                // 若 localCacheExpiredCommentIds 的大小不等于 commentIdList 的大小，说明本地缓存中有数据
                if (localCacheExpiredCommentIds.size() != commentIdList.size()) {
                    // 将本地缓存中的评论详情 Json, 转换为实体类，添加到 VO 返参集合中
                    for (String value : commentIdAndDetailJsonMap.values()) {
                        if (StringUtils.isBlank(value)) continue;
                        FindCommentItemRespVO commentRespVO = JsonUtils.parseObject(value, FindCommentItemRespVO.class);
                        commentRespVOS.add(commentRespVO);
                    }
                }

                // 若 localCacheExpiredCommentIds 大小等于 0，说明评论详情数据都在本地缓存中，直接响应返参
                if (CollUtil.size(localCacheExpiredCommentIds) == 0) {
                    // 计数数据需要从 Redis 中查
                    if (CollUtil.isNotEmpty(commentRespVOS)) {
                        setCommentCountData(commentRespVOS, localCacheExpiredCommentIds);
                    }
                }

                // 构建 MGET 批量查询评论详情的 Key 集合
                List<String> commentIdKeys = localCacheExpiredCommentIds.stream()
                        .map(RedisKeyConstants::buildCommentDetailKey)
                        .toList();

                // MGET 批量获取评论数据
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentIdKeys);

                // 可能存在部分评论不在缓存中，已经过期被删除，这些评论 ID 需要提取出来，等会查数据库
                List<Long> expiredCommentIds = Lists.newArrayList();

                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    // 缓存中存在的评论 Json，直接转换为 VO 添加到返参集合中
                    if (Objects.nonNull(commentJson)) {
                        FindCommentItemRespVO commentRespVO = JsonUtils.parseObject(commentJson, FindCommentItemRespVO.class);
                        commentRespVOS.add(commentRespVO);
                    } else {
                        // 评论失效，添加到失效评论列表
                        expiredCommentIds.add(Long.valueOf(commentsJsonList.get(i).toString()));
                    }
                }

                // 对于缓存中存在的评论详情, 需要再次查询其计数数据
                if (CollUtil.isNotEmpty(commentRespVOS)) {
                    setCommentCountData(commentRespVOS, expiredCommentIds);
                }

                // 对于不存在的一级评论，需要批量从数据库中查询，并添加到 commentRspVOS 中
                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDOS, noteId, commentRespVOS);
                }
            }

            // 按热度值进行降序排列
            commentRespVOS = commentRespVOS.stream()
                    .sorted(Comparator.comparing(FindCommentItemRespVO::getHeat).reversed())
                    .collect(Collectors.toList());

            // 异步将评论详情，同步到本地缓存
            syncCommentDetail2LocalCache(commentRespVOS);

            return PageResponse.success(commentRespVOS, pageNo, count, pageSize);

        }

        // 查询数据库
        // 查询一级评论
        List<CommentDO> oneLevelCommentDOS = commentDOMapper.selectPageList(noteId, offset, pageSize);
        getCommentDataAndSync2Redis(oneLevelCommentDOS, noteId, commentRespVOS);

        // 同步到本地缓存
        syncCommentDetail2LocalCache(commentRespVOS);
        return PageResponse.success(commentRespVOS, pageNo, count, pageSize);
    }

    /**
     * 设置评论 VO 的计数
     *
     * @param commentRespVOS    返参 VO 集合
     * @param expiredCommentIds 缓存中已失效的评论 ID 集合
     */
    private void setCommentCountData(List<FindCommentItemRespVO> commentRespVOS,
                                     List<Long> expiredCommentIds) {
        // 准备从评论 Hash 中查询计数 (子评论总数、被点赞数) 
        // 缓存中存在的评论 ID
        List<Long> notExpiredCommentIds = Lists.newArrayList();

        // 遍历从缓存中解析出的 VO 集合，提取一级、二级评论 ID
        commentRespVOS.forEach(commentRespVO -> {
            Long oneLevelCommentId = commentRespVO.getCommentId();
            notExpiredCommentIds.add(oneLevelCommentId);
            FindCommentItemRespVO firstCommentVO = commentRespVO.getFirstReplyComment();
            if (Objects.nonNull(firstCommentVO)) {
                notExpiredCommentIds.add(firstCommentVO.getCommentId());
            }
        });

        // 已失效的 Hash 评论 ID
        Map<Long, Map<Object, Object>> commentIdAndCountMap = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);

        // 遍历 VO, 设置对应评论的二级评论数、点赞数
        for (FindCommentItemRespVO commentRespVO : commentRespVOS) {
            // 评论Id
            Long commentId = commentRespVO.getCommentId();

            // 若当前这条评论是从数据库中查询出来的, 则无需设置二级评论数、点赞数，以数据库查询出来的为主
            if (CollUtil.isNotEmpty(expiredCommentIds)
                    && expiredCommentIds.contains(commentId)) {
                continue;
            }

            // 设置一级评论的子评论总数、点赞数
            Map<Object, Object> hash = commentIdAndCountMap.get(commentId);
            if (CollUtil.isNotEmpty(hash)) {
                Object likeTotalObj = hash.get(RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL);
                Long childCommentTotal = Objects.isNull(likeTotalObj) ? 0 : Long.parseLong(likeTotalObj.toString());
                Long likeTotal = Long.valueOf(hash.get(RedisKeyConstants.FIELD_LIKE_TOTAL).toString());
                commentRespVO.setChildCommentTotal(childCommentTotal);
                commentRespVO.setLikeTotal(likeTotal);

                // 最初回复的二级评论
                FindCommentItemRespVO firstCommentVO = commentRespVO.getFirstReplyComment();
                if (Objects.nonNull(firstCommentVO)) {
                    Long firstCommentId = firstCommentVO.getCommentId();
                    Map<Object, Object> firstCommentHash = commentIdAndCountMap.get(firstCommentId);
                    if (CollUtil.isNotEmpty(firstCommentHash)) {
                        Long firstCommentLikeTotal = Long.valueOf(firstCommentHash.get(RedisKeyConstants.FIELD_LIKE_TOTAL).toString());
                        firstCommentVO.setLikeTotal(firstCommentLikeTotal);
                    }
                }

            }

        }
    }

    /**
     * 二级评论内容分页查询
     *
     * @param findChildCommentPageListReqVO
     * @return
     */
    @Override
    public PageResponse<FindChildCommentItemRespVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        // 父评论Id
        Long parentCommentId = findChildCommentPageListReqVO.getParentCommentId();
        // 页码
        Integer pageNo = findChildCommentPageListReqVO.getPageNo();

        // 每页展示的二级评论数 (小红书 APP 中是一次查询 6 条)
        long pageSize = 6;

        // 先从缓存中查
        String countCommentKey = RedisKeyConstants.buildCountCommentKey(parentCommentId);
        Number redisCount = (Number) redisTemplate.opsForHash()
                .get(countCommentKey, RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL);

        long count = Objects.isNull(redisCount) ? 0L : redisCount.longValue();

        // 若缓存不存在，走数据库查询
        if (Objects.isNull(redisCount)) {
            // 查询一级评论下子评论的总数 (直接查询 t_comment 表的 child_comment_total 字段，提升查询性能, 避免 count(*))
            long dbCount = commentDOMapper.selectChildCommentTotalById(parentCommentId);

            // 若数据库中不存在，抛出异常
            if (Objects.isNull(dbCount)) {
                throw new BizException(ResponseCodeEnum.PARENT_COMMENT_NOT_FOUND);
            }
            count = dbCount;

            // 异步将子评论总数同步到 Redis 中
            threadPoolTaskExecutor.execute(() -> {
                syncCommentCount2Redis(countCommentKey, dbCount);
            });
        }

        // 若该一级评论不存在，或者子评论总数为 0
        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        // 分页返回参数
        List<FindChildCommentItemRespVO> childCommentRespVOS = Lists.newArrayList();

        // 偏移量
        long offset = PageResponse.getOffset(pageNo, pageSize) + 1;

        // 子评论分页缓存使用 ZSET + STRING 实现
        // 构建子评论 ZSET Key
        String childCommentZSetKey = RedisKeyConstants.buildChildCommentListKey(parentCommentId);
        boolean hasKey = redisTemplate.hasKey(childCommentZSetKey);
        // 若不存在
        if (!hasKey) {
            // 异步将子评论同步到 Redis 中（最多同步 6*10 条）
            threadPoolTaskExecutor.execute(() -> {
                syncChildComments2Redis(parentCommentId, childCommentZSetKey);
            });
        }

        if (hasKey && offset < 6 * 10) {
            // 使用 ZRevRange 获取某个一级评论下的子评论，按回复时间升序排列
            Set<Object> childCommentIds = redisTemplate.opsForZSet()
                    .rangeByScore(childCommentZSetKey, 0, Double.MAX_VALUE, offset, pageSize);
            // 若结果不为空
            if (CollUtil.isNotEmpty(childCommentIds)) {
                // Set 转 List
                List<Object> childCommentIdList = Lists.newArrayList(childCommentIds);

                // 构建 MGET 批量查询子评论详情的 Key 集合
                List<String> commentIdKeys = childCommentIdList.stream()
                        .map(RedisKeyConstants::buildCommentDetailKey)
                        .toList();
                // MGET 批量获取评论数据
                List<Object> commentsJsonList = redisTemplate.opsForValue().multiGet(commentIdKeys);
                // 可能存在部分评论不在缓存中，已经过期被删除，这些评论 ID 需要提取出来，等会查数据库
                List<Long> expiredChildCommentIds = Lists.newArrayList();

                for (int i = 0; i < commentsJsonList.size(); i++) {
                    String commentJson = (String) commentsJsonList.get(i);
                    Long commentId = Long.valueOf(childCommentIdList.get(i).toString());
                    if (Objects.nonNull(commentId)) {
                        // 缓存中存在的评论 Json，直接转换为 VO 添加到返参集合中
                        FindChildCommentItemRespVO childCommentRespVO = JsonUtils.parseObject(commentJson, FindChildCommentItemRespVO.class);
                        childCommentRespVOS.add(childCommentRespVO);
                    } else {
                        // 评论失效，添加到失效评论列表
                        expiredChildCommentIds.add(commentId);
                    }
                }
                // 对于缓存中存在的子评论, 需要再次查询 Hash, 获取其计数数据
                if (CollUtil.isNotEmpty(childCommentRespVOS)) {
                    setChildCommentCountData(childCommentRespVOS, expiredChildCommentIds);
                }
                // 对于不存在的子评论，需要批量从数据库中查询，并添加到 commentRspVOS 中
                if (CollUtil.isNotEmpty(expiredChildCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredChildCommentIds);
                    getChildCommentDataAndSync2Redis(commentDOS, childCommentRespVOS);
                }
                // 按评论 ID 升序排列（等同于按回复时间升序）
                childCommentRespVOS = childCommentRespVOS.stream()
                        .sorted(Comparator.comparing(FindChildCommentItemRespVO::getCommentId))
                        .collect(Collectors.toList());

                return PageResponse.success(childCommentRespVOS, pageNo, count, pageSize);
            }
        }

        // 分页查询子评论
        List<CommentDO> childCommentDOS = commentDOMapper.selectChildPageList(parentCommentId, offset, pageSize);
        getChildCommentDataAndSync2Redis(childCommentDOS, childCommentRespVOS);
        return PageResponse.success(childCommentRespVOS, pageNo, count, pageSize);
    }

    /**
     * 点赞评论
     *
     * @param likeCommentReqVO
     * @return
     */
    @Override
    public Response<?> LikeComment(LikeCommentReqVO likeCommentReqVO) {
        Long commentId = likeCommentReqVO.getCommentId();
        // 1. 校验被点赞的评论是否存在
        checkCommentIsExist(commentId);
        // 2. 判断目标评论，是否已经被点赞
        Long userId = LoginUserContextHolder.getUserId();
        // 创建Redis key
        String rbitmapUserCommentLikeListKey = RedisKeyConstants.buildRbitmapCommentLikesKey(userId);

        // 脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rbitmap_comment_like_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserCommentLikeListKey), commentId);

        CommentLikeLuaResultEnum commentLikeLuaResultEnum = CommentLikeLuaResultEnum.valueOf(result);

        switch (commentLikeLuaResultEnum) {
            case NOT_EXIST -> {
                // 从数据库中校验评论是否被点赞，并异步初始化布隆过滤器，设置过期时间
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                // 保底1小小时+随机秒数
                long expireSeconds = 60 * 60 + RandomUtil.randomInt(60 * 60);
                if (count > 0) {
                    // 异步初始化
                    threadPoolTaskExecutor.submit(() ->
                            batchAddCommentLike2RbitmapAndExpire(userId, expireSeconds, rbitmapUserCommentLikeListKey));
                    throw new BizException(ResponseCodeEnum.COMMENT_ALREADY_LIKED);
                }
                // 若目标评论未被点赞，查询当前用户是否有点赞其他评论，有则同步初始化布隆过滤器
                batchAddCommentLike2RbitmapAndExpire(userId, expireSeconds, rbitmapUserCommentLikeListKey);

                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_add_comment_like_and_expire.lua")));
                script.setResultType(Long.class);

                redisTemplate.execute(script, Collections.singletonList(rbitmapUserCommentLikeListKey), commentId, expireSeconds);

            }
            case COMMENT_LIKED -> {
                // 该评论已点赞
                // 查询数据库校验是否点赞
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                if (count > 0) {
                    throw new BizException(ResponseCodeEnum.COMMENT_ALREADY_LIKED);
                }
            }
        }
        // 3. 发送 MQ, 异步将评论点赞记录落库
        // 构建消息体 DTO
        LikeUnlikeCommentMqDTO likeUnlikeCommentMqDTO = LikeUnlikeCommentMqDTO.builder()
                .userId(userId)
                .commentId(commentId)
                .type(LikeUnlikeCommentTypeEnum.LIKE.getCode())
                .createTime(LocalDateTime.now())
                .build();
        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeCommentMqDTO)).build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_LIKE;
        // MQ 分区键
        String hashKey = String.valueOf(userId);
        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论点赞】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    /**
     * 取消评论点赞
     *
     * @param unlikeCommentReqVO
     * @return
     */
    @Override
    public Response<?> UnlikeComment(UnlikeCommentReqVO unlikeCommentReqVO) {
        // 评论 Id
        Long commentId = unlikeCommentReqVO.getCommentId();

        // 1.校验评论是否存在
        checkCommentIsExist(commentId);

        // 2. 判断评论未点赞
        // 当前用户Id
        Long userId = LoginUserContextHolder.getUserId();
        String rbitmapCommentLikeListKey = RedisKeyConstants.buildRbitmapCommentLikesKey(userId);

        // 脚本路径
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rbitmap_comment_unlike_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapCommentLikeListKey), commentId);

        CommentUnlikeLuaResultEnum commentUnlikeLuaResultEnum = CommentUnlikeLuaResultEnum.valueOf(result);

        if (Objects.isNull(commentUnlikeLuaResultEnum)) {
            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
        }

        switch (commentUnlikeLuaResultEnum) {
            case NOT_EXIST -> {
                // 异步初始化
                threadPoolTaskExecutor.submit(() -> {
                    // 一个小时的过期时间
                    long expireSeconds = 60 * 60 + RandomUtil.randomInt(60 * 60);
                    batchAddCommentLike2RbitmapAndExpire(userId, expireSeconds, rbitmapCommentLikeListKey);
                });

                // 从数据库中校验评论是否被点赞
                int count = commentLikeDOMapper.selectCountByUserIdAndCommentId(userId, commentId);
                // 未点赞，无法取消点赞操作，抛出业务异常
                if (count == 0) throw new BizException(ResponseCodeEnum.COMMENT_NOT_LIKED);
            }
            case COMMENT_NOT_LIKED -> throw new BizException(ResponseCodeEnum.COMMENT_NOT_LIKED);
        }

        // 3. 发送顺序 MQ，删除评论点赞记录
        LikeUnlikeCommentMqDTO likeUnlikeCommentMqDTO = LikeUnlikeCommentMqDTO.builder()
                .userId(userId)
                .commentId(commentId)
                .type(LikeUnlikeCommentTypeEnum.UNLIKE.getCode())
                .createTime(LocalDateTime.now())
                .build();
        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeCommentMqDTO)).build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_UNLIKE;
        // MQ 分区键
        String hashKey = String.valueOf(userId);
        // 异步发送 MQ 顺序消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论取消点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论取消点赞】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    /**
     * 删除评论
     *
     * @param deleteCommentReqVO
     * @return
     */
    @Override
    public Response<?> deleteComment(DeleteCommentReqVO deleteCommentReqVO) {
        // 评论 Id
        Long commentId = deleteCommentReqVO.getCommentId();
        // 1. 校验评论是否存在
        CommentDO commentDO = commentDOMapper.selectByPrimaryKey(commentId);
        if (Objects.isNull(commentDO)) {
            throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
        }
        // 2. 校验是否有权限删除
        // 当前用户
        Long userId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(userId, commentDO.getUserId())) {
            throw new BizException(ResponseCodeEnum.COMMENT_CANT_OPERATE);
        }
        // 3. 物理删除评论、评论内容
        // 编程式事务，保证多个操作的原子性
        transactionTemplate.execute(status -> {
            try {
                // 删除评论元数据
                commentDOMapper.deleteByPrimaryKey(commentId);

                // 删除评论内容
                keyValueRpcService.deleteCommentContent(commentDO.getNoteId(), commentDO.getCreateTime(),
                        commentDO.getContentUuid());

                return null;
            } catch (Exception ex) {
                status.setRollbackOnly();
                log.error("", ex);
                throw ex;
            }
        });
        // 4. 删除 Redis 缓存（ZSet 和 String）
        Integer level = commentDO.getLevel();
        Long noteId = commentDO.getNoteId();
        Long parentCommentId  = commentDO.getParentId();

        // 根据评论级别，构建对应的 ZSet Key
        String redisZSetKey = Objects.equals(level, 1) ?
                RedisKeyConstants.buildCommentListKey(noteId) : RedisKeyConstants.buildChildCommentListKey(parentCommentId);
        // 使用 RedisTemplate 执行管道操作
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 删除 ZSet 中对应评论 ID
                operations.opsForZSet().remove(redisZSetKey, commentId);
                // 删除评论详情
                operations.delete(RedisKeyConstants.buildCommentDetailKey(commentId));
                return null;
            }
        });
        // 5. 发布广播 MQ, 将本地缓存删除
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE, commentId, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【删除评论详情本地缓存】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【删除评论详情本地缓存】MQ 发送异常: ", throwable);
            }
        });
        // 6. 发送 MQ, 异步去更新计数、删除关联评论、热度值等
        // 构建消息对象，并将 DO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(commentDO)).build();

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELETE_COMMENT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【评论删除】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【评论删除】MQ 发送异常: ", throwable);
            }
        });
        
        return Response.success();
    }

    /**
     * 删除本地缓存中的评论
     * @param commentId
     */
    @Override
    public void deleteCommentLocalCache(Long commentId) {
        LOCAL_CACHE.invalidate(commentId);
    }

    /**
     * 初始化评论点赞
     *
     * @param userId
     * @param expireSeconds
     * @param rbitmapUserCommentLikeListKey
     * @return
     */
    private void batchAddCommentLike2RbitmapAndExpire(Long userId, long expireSeconds, String rbitmapUserCommentLikeListKey) {
        try {
            // 当前用户点赞的所有评论
            List<CommentLikeDO> commentLikeDOS = commentLikeDOMapper.selectByUserId(userId);

            // 若不为空，批量添加到布隆过滤器中
            if (CollUtil.isNotEmpty(commentLikeDOS)) {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_batch_add_comment_like_and_expire.lua")));
                // 返回值类型
                script.setResultType(Long.class);

                List<Object> luaArgs = Lists.newArrayList();
                commentLikeDOS.forEach(commentLikeDO -> {
                    luaArgs.add(commentLikeDO.getCommentId());
                    luaArgs.add(expireSeconds);
                });
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserCommentLikeListKey), luaArgs.toArray());
            }

        } catch (Exception e) {
            log.error("## 异步初始化【评论点赞】布隆过滤器异常: ", e);
        }
    }

    /**
     * 检查评论是否存在
     *
     * @param commentId
     */
    private void checkCommentIsExist(Long commentId) {
        // 本地缓存是否存在
        String localCacheJson = LOCAL_CACHE.getIfPresent(commentId);

        // 若不本地缓存中不存在
        if (StringUtils.isEmpty(localCacheJson)) {
            // 再从 Redis 中校验
            String commentDetailRedisKey = RedisKeyConstants.buildCommentDetailKey(commentId);
            boolean hasKey = redisTemplate.hasKey(commentDetailRedisKey);
            // 缓存中不存在，查询数据库
            if (!hasKey) {
                CommentDO commentDO = commentDOMapper.selectByPrimaryKey(commentId);
                // 若数据库中，该评论也不存在，抛出业务异常
                if (Objects.isNull(commentDO)) {
                    throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
                }
            }
        }

    }

    /**
     * @param commentRespVOS
     * @param expiredCommentIds
     */
    private void setChildCommentCountData(List<FindChildCommentItemRespVO> commentRespVOS,
                                          List<Long> expiredCommentIds) {
        // 准备从评论 Hash 中查询计数 (被点赞数)
        // 缓存中存在的子评论 ID
        List<Long> notExpiredCommentIds = Lists.newArrayList();
        // 遍历从缓存中解析出的 VO 集合，提取二级评论 ID
        commentRespVOS.forEach(commentRespVO -> {
            Long childCommentId = commentRespVO.getCommentId();
            notExpiredCommentIds.add(childCommentId);
        });

        // 从 Redis 中查询评论计数 Hash 数据
        Map<Long, Map<Object, Object>> commentIdAndCountMap = getCommentCountDataAndSync2RedisHash(notExpiredCommentIds);

        // 遍历 VO, 设置对应子评论的点赞数
        for (FindChildCommentItemRespVO commentRespVO : commentRespVOS) {
            // 评论 ID
            Long commentId = commentRespVO.getCommentId();
            // 若当前这条评论是从数据库中查询出来的, 则无需设置点赞数，以数据库查询出来的为主
            if (CollUtil.isNotEmpty(expiredCommentIds)
                    && expiredCommentIds.contains(commentId)) {
                continue;
            }
            // 设置子评论的点赞数
            Map<Object, Object> hash = commentIdAndCountMap.get(commentId);
            if (CollUtil.isNotEmpty(hash)) {
                Long likeTotal = Long.valueOf(hash.get(RedisKeyConstants.FIELD_LIKE_TOTAL).toString());
                commentRespVO.setLikeTotal(likeTotal);
            }
        }
    }

    /**
     * 获取评论计数数据，并同步到 Redis 中
     *
     * @param notExpiredCommentIds
     * @return
     */
    private Map<Long, Map<Object, Object>> getCommentCountDataAndSync2RedisHash(List<Long> notExpiredCommentIds) {
        // 已失效的 Hash 评论 ID
        List<Long> expiredCountCommentIds = Lists.newArrayList();
        // 构建需要查询的 Hash Key 集合
        List<String> commentCountKeys = notExpiredCommentIds.stream()
                .map(RedisKeyConstants::buildCountCommentKey).toList();

        // 使用 RedisTemplate 执行管道批量操作
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                // 遍历需要查询的评论计数的 Hash 键集合
                commentCountKeys.forEach(key -> {
                    // 在管道中执行 Redis 的 hash.entries 操作
                    // 此操作会获取指定 Hash 键中所有的字段和值
                    operations.opsForHash().entries(key);
                });
                return null;
            }
        });

        // 评论 Id - 计数数据字典
        Map<Long, Map<Object, Object>> commentIdAndCountMap = Maps.newHashMap();
        // 遍历未过期的评论 Id 集合
        for (int i = 0; i < notExpiredCommentIds.size(); i++) {
            // 当前评论 Id
            Long currCommentId = Long.valueOf(notExpiredCommentIds.get(i).toString());
            // 从缓存查询结果中，获取对应 Hash
            Map<Object, Object> hash = (Map<Object, Object>) results.get(i);
            // 若 Hash 结果为空，说明缓存中不存在，添加到 expiredCountCommentIds 中，保存一下
            if (CollUtil.isEmpty(hash)) {
                expiredCountCommentIds.add(currCommentId);
                continue;
            }
            // 若存在，则将数据添加到 commentIdAndCountMap 中，方便后续读取
            commentIdAndCountMap.put(currCommentId, hash);
        }

        // 若已过期的计数评论 Id 集合大于 0，说明部分计数数据不在 Redis 缓存中
        // 需要查询数据库，并将这部分的评论计数 Hash 同步到 Redis 中
        if (CollUtil.size(expiredCountCommentIds) > 0) {
            // 查询数据库
            List<CommentDO> commentDOS = commentDOMapper.selectCommentCountByIds(expiredCountCommentIds);

            commentDOS.forEach(commentDO -> {
                Integer level = commentDO.getLevel();
                Map<Object, Object> map = Maps.newHashMap();
                map.put(RedisKeyConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal());
                // 只有一级评论需要统计子评论总数
                if (Objects.equals(level, CommentLevelEnum.ONE.getCode())) {
                    map.put(RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL, commentDO.getChildCommentTotal());
                }
                // 统一添加到 commentIdAndCountMap 字典中，方便后续查询
                commentIdAndCountMap.put(commentDO.getId(), map);
            });

            // 异步同步到 Redis 中
            threadPoolTaskExecutor.execute(() -> {
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {

                        commentDOS.forEach(commentDO -> {
                            // 构建 Hash Key
                            String key = RedisKeyConstants.buildCountCommentKey(commentDO.getId());
                            // 评论级别
                            Integer level = commentDO.getLevel();
                            // 设置 Field 数据
                            Map<String, Long> fieldsMap = Objects.equals(level, CommentLevelEnum.ONE.getCode()) ?
                                    Map.of(RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL, commentDO.getChildCommentTotal(),
                                            RedisKeyConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal()) : Map.of(RedisKeyConstants.FIELD_LIKE_TOTAL, commentDO.getLikeTotal());

                            // 添加 Hash 数据
                            operations.opsForHash().putAll(key, fieldsMap);
                            // 设置随机过期时间 (5小时以内)
                            long expireSeconds = 60 * 60 + RandomUtil.randomInt(4 * 60 * 60);
                            operations.expire(key, expireSeconds, TimeUnit.SECONDS);
                        });
                        return null;
                    }
                });
            });
        }
        return commentIdAndCountMap;
    }

    /**
     * 获取子评论列表，并同步到 Redis 中
     *
     * @param childCommentDOS
     * @param childCommentRespVOS
     */
    private void getChildCommentDataAndSync2Redis(List<CommentDO> childCommentDOS, List<FindChildCommentItemRespVO> childCommentRespVOS) {
        // 调用 KV 服务需要的输入参数
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        Set<Long> userIds = Sets.newHashSet();

        // 归属的笔记 ID
        Long noteId = null;

        for (CommentDO childCommentDO : childCommentDOS) {
            noteId = childCommentDO.getNoteId();
            // 构建调用 KV 服务批量查询评论内容的入参
            boolean isContentEmpty = childCommentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(childCommentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(childCommentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            // 构建调用用户服务批量查询用户信息的入参 (包含评论发布者、回复的目标用户)
            userIds.add(childCommentDO.getUserId());

            Long parentId = childCommentDO.getParentId();
            Long replyCommentId = childCommentDO.getReplyCommentId();
            // 若当前评论的 replyCommentId 不等于 parentId，则前端需要展示回复的哪个用户，如  “回复 小红书：”

            if (!Objects.equals(parentId, replyCommentId)) {
                userIds.add(childCommentDO.getReplyUserId());
            }
        }
        // RPC: 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRespDTO> findCommentContentRespDTOS =
                keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);

        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRespDTOS)) {
            commentUuidAndContentMap = findCommentContentRespDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRespDTO::getContentId, FindCommentContentRespDTO::getContent));
        }

        // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUserByIdRespDTO> findUserByIdRespDTOS = userRpcService.findByIds(userIds.stream().toList());

        // DTO 集合转 Map, 方便后续拼装数据
        Map<Long, FindUserByIdRespDTO> userIdAndDTOMap = null;
        if (CollUtil.isNotEmpty(findUserByIdRespDTOS)) {
            userIdAndDTOMap = findUserByIdRespDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRespDTO::getId, dto -> dto));
        }

        // DO 转 VO
        for (CommentDO childCommentDO : childCommentDOS) {
            // 构建 VO 实体类
            Long userId = childCommentDO.getUserId();
            FindChildCommentItemRespVO childCommentRespVO = FindChildCommentItemRespVO.builder()
                    .userId(userId)
                    .commentId(childCommentDO.getId())
                    .imageUrl(childCommentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(childCommentDO.getCreateTime()))
                    .likeTotal(childCommentDO.getLikeTotal())
                    .build();

            // 填充用户信息(包括评论发布者、回复的用户)
            if (CollUtil.isNotEmpty(userIdAndDTOMap)) {
                FindUserByIdRespDTO findUserByIdRespDTO = userIdAndDTOMap.get(userId);
                // 评论发布者用户信息(头像、昵称)
                if (Objects.nonNull(findUserByIdRespDTO)) {
                    childCommentRespVO.setAvatar(findUserByIdRespDTO.getAvatar());
                    childCommentRespVO.setNickname(findUserByIdRespDTO.getNickName());
                }
                // 评论回复的哪个
                Long replyCommentId = childCommentDO.getReplyCommentId();
                Long parentId = childCommentDO.getParentId();

                if (Objects.nonNull(replyCommentId)
                        && !Objects.equals(replyCommentId, parentId)) {
                    Long replyUserId = childCommentDO.getReplyUserId();
                    FindUserByIdRespDTO replyUser = userIdAndDTOMap.get(replyUserId);
                    childCommentRespVO.setReplyUserName(replyUser.getNickName());
                    childCommentRespVO.setReplyUserId(replyUser.getId());
                }
            }

            // 评论内容
            if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
                String contentUuid = childCommentDO.getContentUuid();
                if (StringUtils.isNotBlank(contentUuid)) {
                    childCommentRespVO.setContent(commentUuidAndContentMap.get(contentUuid));
                }
            }
            childCommentRespVOS.add(childCommentRespVO);
        }

        // 异步将笔记详情，同步到 Redis 中
        threadPoolTaskExecutor.execute(() -> {
            // 准备批量写入的数据
            Map<String, String> data = Maps.newHashMap();
            childCommentRespVOS.forEach(commentRespVO -> {
                // 评论 ID
                Long commentId = commentRespVO.getCommentId();
                // 构建 Key
                String key = RedisKeyConstants.buildCommentDetailKey(commentId);
                data.put(key, JsonUtils.toJsonString(commentRespVO));
            });
            batchAddCommentDetailJson2Redis(data);
        });

    }

    /**
     * 批量添加评论详情 Json 到 Redis
     *
     * @param data
     */
    private void batchAddCommentDetailJson2Redis(Map<String, String> data) {
        // 使用 Redis Pipeline 提升写入性能
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                // 将 Java 对象序列化为 JSON 字符串
                String jsonStr = JsonUtils.toJsonString(entry.getValue());
                // 随机生成过期时间 (5小时以内)
                int randomExpire = 60 * 60 + RandomUtil.randomInt(4 * 60 * 60);

                // 批量写入并设置过期时间
                connection.setEx(
                        redisTemplate.getStringSerializer().serialize(entry.getKey()),
                        randomExpire,
                        redisTemplate.getStringSerializer().serialize(jsonStr)
                );
            }
            return null;
        });
    }

    /**
     * 同步子评论到 Redis 中
     *
     * @param parentCommentId
     * @param childCommentZSetKey
     */
    private void syncChildComments2Redis(Long parentCommentId, String childCommentZSetKey) {
        List<CommentDO> childCommentDOS = commentDOMapper.selectChildCommentsByParentIdAndLimit(parentCommentId, 6 * 10);

        if (CollUtil.isNotEmpty(childCommentDOS)) {
            // 使用 Redis Pipeline 提升写入性能
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

                // 遍历子评论数据并批量写入 ZSet
                for (CommentDO childCommentDO : childCommentDOS) {
                    Long commentId = childCommentDO.getId();
                    // create_time 转时间戳
                    long commentTimestamp = DateUtils.localDateTime2Timestamp(childCommentDO.getCreateTime());
                    zSetOps.add(childCommentZSetKey, commentId, commentTimestamp);
                }
                // 设置随机过期时间，（保底1小时 + 随机时间），单位：秒
                int randomExpiryTime = 60 * 60 + RandomUtil.randomInt(4 * 60 * 60); // 5小时以内
                redisTemplate.expire(childCommentZSetKey, randomExpiryTime, TimeUnit.SECONDS);
                return null; // 无返回值
            });
        }
    }

    /**
     * 同步 Redis
     *
     * @param countCommentKey
     * @param dbCount
     */
    private void syncCommentCount2Redis(String countCommentKey, long dbCount) {
        redisTemplate.executePipelined(new SessionCallback<>() {

            @Override
            public Object execute(RedisOperations operations) {
                // 同步 hash 数据
                operations.opsForHash()
                        .put(countCommentKey, RedisKeyConstants.FIELD_CHILD_COMMENT_TOTAL, dbCount);
                // 随机过期时间 (保底1小时 + 随机时间)，单位：秒
                long expireSeconds = 60 * 60 + RandomUtil.randomInt(4 * 60 * 60);
                operations.expire(countCommentKey, expireSeconds, TimeUnit.SECONDS);
                return null;
            }
        });
    }

    /**
     * 同步评论详情到本地缓存中
     *
     * @param commentRespVOS
     */
    private void syncCommentDetail2LocalCache(List<FindCommentItemRespVO> commentRespVOS) {
        // 开启一个异步线程
        threadPoolTaskExecutor.execute(() -> {
            // 构建缓存所需的键值
            Map<Long, String> localCacheData = Maps.newHashMap();
            commentRespVOS.forEach(commentRespVO -> {
                // 评论 Id
                Long commentId = commentRespVO.getCommentId();
                localCacheData.put(commentId, JsonUtils.toJsonString(commentRespVO));
            });
            // 批量写入本地缓存
            LOCAL_CACHE.putAll(localCacheData);
        });
    }

    /**
     * 获取全部评论数据，并将评论详情同步到 Redis 中
     *
     * @param oneLevelCommentDOS
     * @param noteId
     * @param commentRespVOS
     */
    private void getCommentDataAndSync2Redis(List<CommentDO> oneLevelCommentDOS, Long noteId, List<FindCommentItemRespVO> commentRespVOS) {
        // 过滤出所有最早回复的二级评论 Id
        List<Long> twoLevelCommentIds = oneLevelCommentDOS.stream()
                .map(CommentDO::getFirstReplyCommentId)
                .filter(firstReplyCommentId -> firstReplyCommentId != 0)
                .toList();

        // 查询二级评论
        Map<Long, CommentDO> commentIdAndDOMap = null;
        List<CommentDO> twoLevelCommonDOS = null;
        if (CollUtil.isNotEmpty(twoLevelCommentIds)) {
            twoLevelCommonDOS = commentDOMapper.selectTwoLevelCommentByIds(twoLevelCommentIds);
            //  转 Map 集合，方便后续拼装数据
            commentIdAndDOMap = twoLevelCommonDOS.stream()
                    .collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
        }

        // 调用 KV 服务需要的入参
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        List<Long> userIds = Lists.newArrayList();

        // 将一级评论和二级评论合并到一起
        List<CommentDO> allCommentDOS = Lists.newArrayList();
        CollUtil.addAll(allCommentDOS, oneLevelCommentDOS);
        CollUtil.addAll(allCommentDOS, twoLevelCommonDOS);
        // 循环提取 RPC 调用需要的入参数据
        allCommentDOS.forEach(commentDO -> {
            // 构建调用 KV 服务批量查询评论内容的入参
            boolean isContentEmpty = commentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(commentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }
            // 构建调用用户服务批量查询用户信息的入参
            userIds.add(commentDO.getUserId());
        });

        // RPC: 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRespDTO> findCommentContentRespDTOS =
                keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRespDTOS)) {
            commentUuidAndContentMap = findCommentContentRespDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRespDTO::getContentId, FindCommentContentRespDTO::getContent));
        }

        // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUserByIdRespDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);
        // DTO 集合转 Map, 方便后续拼装数据
        Map<Long, FindUserByIdRespDTO> userIdAndDTOMap = null;
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            userIdAndDTOMap = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRespDTO::getId, dto -> dto));
        }

        // DO 转 VO, 组合拼装一二级评论数据
        for (CommentDO commentDO : oneLevelCommentDOS) {
            // 一级评论
            Long userId = commentDO.getUserId();
            FindCommentItemRespVO oneLevelCommentRespVO = FindCommentItemRespVO.builder()
                    .userId(userId)
                    .commentId(commentDO.getId())
                    .imageUrl(commentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(commentDO.getCreateTime()))
                    .likeTotal(commentDO.getLikeTotal())
                    .childCommentTotal(commentDO.getChildCommentTotal())
                    .heat(commentDO.getHeat())
                    .build();

            // 用户信息
            setUserInfo(commentIdAndDOMap, userIdAndDTOMap, userId, oneLevelCommentRespVO);
            // 笔记内容
            setCommentContent(commentUuidAndContentMap, commentDO, oneLevelCommentRespVO);

            // 二级评论
            Long firstReplyCommentId = commentDO.getFirstReplyCommentId();
            if (CollUtil.isNotEmpty(commentIdAndDOMap)) {
                CommentDO firstReplyCommentDO = commentIdAndDOMap.get(firstReplyCommentId);

                if (Objects.nonNull(firstReplyCommentDO)) {
                    Long firstReplyCommentUserId = firstReplyCommentDO.getUserId();

                    FindCommentItemRespVO firstReplyCommentRespVO = FindCommentItemRespVO.builder()
                            .userId(firstReplyCommentDO.getUserId())
                            .commentId(firstReplyCommentDO.getId())
                            .imageUrl(firstReplyCommentDO.getImageUrl())
                            .createTime(DateUtils.formatRelativeTime(firstReplyCommentDO.getCreateTime()))
                            .likeTotal(firstReplyCommentDO.getLikeTotal())
                            .heat(firstReplyCommentDO.getHeat())
                            .build();
                    // 用户信息
                    setUserInfo(commentIdAndDOMap, userIdAndDTOMap, firstReplyCommentUserId, firstReplyCommentRespVO);
                    // 笔记内容
                    oneLevelCommentRespVO.setFirstReplyComment(firstReplyCommentRespVO);
                    setCommentContent(commentUuidAndContentMap, firstReplyCommentDO, firstReplyCommentRespVO);
                }
            }
            commentRespVOS.add(oneLevelCommentRespVO);
        }

        // 异步将笔记详情，同步到 Redis 中
        threadPoolTaskExecutor.execute(() -> {
            // 批量写入数据
            Map<String, Object> data = Maps.newHashMap();
            commentRespVOS.forEach(commentRespVO -> {
                // 评论 Id
                Long commentId = commentRespVO.getCommentId();
                // 构建 Key
                String key = RedisKeyConstants.buildCommentDetailKey(commentId);
                data.put(key, JsonUtils.toJsonString(commentRespVO));
            });

            // 使用 Redis Pipeline 提升写入性能
            redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    //将 Java 对象序列化为 JSON 字符串
                    String jsonStr = JsonUtils.toJsonString(entry.getValue());
                    // 随机生成过期时间 (5小时以内)
                    int randomExpire = RandomUtil.randomInt(5 * 60 * 60);

                    // 批量写入并设置过期时间
                    connection.setEx(
                            redisTemplate.getStringSerializer().serialize(entry.getKey()),
                            randomExpire,
                            redisTemplate.getStringSerializer().serialize(jsonStr)
                    );
                }
                return null;
            });
        });
    }


    /**
     * 同步热点评论至 Redis
     *
     * @param key
     * @param noteId
     * @return
     */
    private Object syncHeatComments2Redis(String key, Long noteId) {

        List<CommentDO> commentDOS = commentDOMapper.selectHeatComments(noteId);
        if (CollUtil.isNotEmpty(commentDOS)) {

            // 使用 Redis Pipeline 提升写入性能
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

                // 遍历评论数据并批量写入 ZSet
                for (CommentDO commentDO : commentDOS) {
                    Long commentDOId = commentDO.getId();
                    Double heat = commentDO.getHeat();
                    zSetOps.add(key, commentDOId, heat);
                }
                // 设置随机过期时间，单位：秒
                int randomExpiryTime = RandomUtil.randomInt(5 * 60 * 60); // 5小时以内
                redisTemplate.expire(key, randomExpiryTime, TimeUnit.SECONDS);
                return null;
            });

        }

        return null;
    }

    /**
     * 异步同步到 Redis
     *
     * @param noteCommentTotalKey
     * @param dbCount
     * @return
     */
    private Object syncNoteCommentTotal2Redis(String noteCommentTotalKey, Long dbCount) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 同步 Hash 数据
                operations.opsForHash()
                        .put(noteCommentTotalKey, RedisKeyConstants.FIELD_COMMENT_TOTAL, dbCount);

                // 过期时间
                long expireTime = 60 * 60 + RandomUtil.randomInt(4 * 60 * 60);
                operations.expire(noteCommentTotalKey, expireTime, TimeUnit.SECONDS);
                return null;
            }
        });
        return null;
    }

    /**
     * 用户信息
     *
     * @param commentIdAndDOMap
     * @param userIdAndDTOMap
     * @param userId
     * @param oneLevelCommentRespVO
     */
    private static void setUserInfo(Map<Long, CommentDO> commentIdAndDOMap, Map<Long, FindUserByIdRespDTO> userIdAndDTOMap, Long userId, FindCommentItemRespVO oneLevelCommentRespVO) {
        FindUserByIdRespDTO findUserByIdRespDTO = userIdAndDTOMap.get(userId);
        if (Objects.nonNull(findUserByIdRespDTO)) {
            oneLevelCommentRespVO.setAvatar(findUserByIdRespDTO.getAvatar());
            oneLevelCommentRespVO.setNickname(findUserByIdRespDTO.getNickName());
        }
    }

    /**
     * 笔记内容
     *
     * @param commentUuidAndContentMap
     * @param commentDO1
     * @param firstReplyCommentRespVO
     */
    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentDO commentDO1, FindCommentItemRespVO firstReplyCommentRespVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentDO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRespVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }
}
