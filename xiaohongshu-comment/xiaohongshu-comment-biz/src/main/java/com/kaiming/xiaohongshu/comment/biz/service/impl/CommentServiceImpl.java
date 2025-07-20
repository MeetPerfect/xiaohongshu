package com.kaiming.xiaohongshu.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentDOMapper;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.NoteCountDOMapper;
import com.kaiming.xiaohongshu.comment.biz.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.comment.biz.model.dto.PublishCommentMqDTO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.FindCommentItemRespVO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.FindCommentPageListReqVO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.PublishCommentReqVO;
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
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import org.apache.commons.lang3.StringUtils;

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
                Map<Long, String> commentIdAndDetailJsonMap  = LOCAL_CACHE.getAll(localCacheKeys, missingKeys -> {
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
                        if (StringUtils.isBlank(value)) continue ;
                        FindCommentItemRespVO commentRespVO  = JsonUtils.parseObject(value, FindCommentItemRespVO.class);
                        commentRespVOS.add(commentRespVO);
                    }
                }

                // 若 localCacheExpiredCommentIds 大小等于 0，说明评论详情数据都在本地缓存中，直接响应返参
                if (CollUtil.size(localCacheExpiredCommentIds) == 0) {
                    return PageResponse.success(commentRespVOS, pageNo, count, pageSize);
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

                // 对于不存在的一级评论，需要批量从数据库中查询，并添加到 commentRspVOS 中
                if (CollUtil.isNotEmpty(expiredCommentIds)) {
                    List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(expiredCommentIds);
                    getCommentDataAndSync2Redis(commentDOS, noteId, commentRespVOS);
                }
            }

            // 按热度值进行降序排列
            commentRespVOS  = commentRespVOS.stream()
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
     * 同步评论详情到本地缓存中
     * @param commentRespVOS
     */
    private void syncCommentDetail2LocalCache(List<FindCommentItemRespVO> commentRespVOS) {
        // 开启一个异步线程
        threadPoolTaskExecutor.execute(() -> {
            // 构建缓存所需的键值
            Map<Long, String> localCacheData  = Maps.newHashMap();
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
