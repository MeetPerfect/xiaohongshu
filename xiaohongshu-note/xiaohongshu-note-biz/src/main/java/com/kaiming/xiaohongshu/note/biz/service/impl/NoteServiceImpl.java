package com.kaiming.xiaohongshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;

import cn.hutool.core.util.RandomUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteCollectionDO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteDOMapper;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.TopicDOMapper;
import com.kaiming.xiaohongshu.note.biz.enums.*;
import com.kaiming.xiaohongshu.note.biz.model.dto.CollectUnCollectNoteMqDTO;
import com.kaiming.xiaohongshu.note.biz.model.dto.LikeUnlikeNoteMqDTO;
import com.kaiming.xiaohongshu.note.biz.model.vo.*;
import com.kaiming.xiaohongshu.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.kaiming.xiaohongshu.note.biz.rpc.KeyValueRpcService;
import com.kaiming.xiaohongshu.note.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.note.biz.service.NoteService;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: NoteServiceImpl
 * Package: com.kaiming.xiaohongshu.note.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 22:16
 * @Version 1.0
 */
@Service
@Slf4j
public class NoteServiceImpl implements NoteService {

    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private TopicDOMapper topicDOMapper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private UserRpcService userRpcService;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;
    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;
    /**
     * 本地缓存，使用 Caffeine 实现
     */
    private static final Cache<Long, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000)
            .maximumSize(100000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    /**
     * 发布笔记
     *
     * @param publishNoteReqVO
     * @return
     */
    @Override
    public Response<?> publishNote(PublishNoteReqVO publishNoteReqVO) {
        // 笔记类型
        Integer type = publishNoteReqVO.getType();

        // 获取对应类型的枚举
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }
        String imgUris = null;
        // 笔记内容默认为空， 默认值为true,即为空
        Boolean isContentEmpty = true;
        String videoUri = null;
        // 根据类型进行不同的处理
        switch (noteTypeEnum) {
            case IMAGE_TEXT -> {
                List<String> imgUriList = publishNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");

                // 校验图片数量
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                //图片链接拼接，以逗号隔离
                imgUris = StringUtils.join(imgUriList, ",");
                break;
            }
            case VIDEO -> {
                videoUri = publishNoteReqVO.getVideoUri();
                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            }
            default -> {
                break;
            }
        }

        // RPC 调用分布式ID生成服务，生成笔记ID
        String snowflakeId = distributedIdGeneratorRpcService.getSnowflakeId();
        // 笔记内容UUID
        String contentUuid = null;

        // 笔记内容
        String content = publishNoteReqVO.getContent();
        // 如果笔记内容不为空
        if (StringUtils.isNotBlank(content)) {
            // 设置笔记内容布尔值为false
            isContentEmpty = false;

            contentUuid = UUID.randomUUID().toString();
            // RPC 调用 KV 服务，存储笔记内容
            boolean isSaveSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);

            // 如果存储失败,抛出异常,提示存储笔记内容失败
            if (!isSaveSuccess) {
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }
        // 话题
        Long topicId = publishNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.nonNull(topicId)) {
            // 获取话题名称
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);

        }
        // 获取发布者用户ID
        Long creatorId = LoginUserContextHolder.getUserId();

        NoteDO noteDO = NoteDO.builder()
                .id(Long.valueOf(snowflakeId))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUris(imgUris)
                .title(publishNoteReqVO.getTitle())
                .topicId(topicId)
                .topicName(topicName)
                .type(type)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.NORMAL.getCode())
                .isTop(Boolean.FALSE)
                .videoUri(videoUri)
                .contentUuid(contentUuid)
                .build();

        try {
            // 笔记存储
            noteDOMapper.insert(noteDO);

        } catch (Exception e) {
            log.error("==> 笔记存储失败", e);
            // RPC: 笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        return Response.success();
    }

    /**
     * 查询笔记详情
     *
     * @param findNoteDetailReqVO
     * @return
     */
    @Override
    @SneakyThrows
    public Response<FindNoteDetailRespVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO) {
        // 查询的笔记ID
        Long noteId = findNoteDetailReqVO.getId();

        // 当前登录用户Id
        Long userId = LoginUserContextHolder.getUserId();
        // 1. 查询本地缓存
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            FindNoteDetailRespVO findNoteDetailRespVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRespVO.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRespVO);
            return Response.success(findNoteDetailRespVO);
        }

        // 2. Redis缓存中获取
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若缓存中存在该笔记，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)) {
            FindNoteDetailRespVO findNoteDetailRespVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRespVO.class);

            // 异步线程将笔记信息存入本地缓存
            threadPoolTaskExecutor.submit(() -> {
                // 写入本地缓存
                LOCAL_CACHE.put(noteId, Objects.isNull(findNoteDetailRespVO) ? "" : JsonUtils.toJsonString(findNoteDetailRespVO));
            });
            // 可见性校验
            checkNoteVisibleFromVO(userId, findNoteDetailRespVO);
            return Response.success(findNoteDetailRespVO);
        }

        // 3. 缓存中不存在，走数据库查询
        NoteDO noteDO = noteDOMapper.selectByPrimaryKey(noteId);
        // 如果笔记不存在
        if (Objects.isNull(noteDO)) {
            threadPoolTaskExecutor.submit(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数 
                long expireTime = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(noteDetailRedisKey, null, expireTime, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 校验可见性
        Integer visible = noteDO.getVisible();
        checkNoteVisible(visible, userId, noteDO.getCreatorId());

        // RPC: 调用用户服务
        Long creatorId = noteDO.getCreatorId();
//        FindUserByIdRespDTO findUserByIdRespDTO = userRpcService.findById(creatorId);
        // 使用 CompletableFuture 异步调用用户服务
        CompletableFuture<FindUserByIdRespDTO> userResultFuture = CompletableFuture.supplyAsync(
                () -> userRpcService.findById(creatorId), threadPoolTaskExecutor);

        // RPC: 调用 KV 服务，查询笔记内容
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);

        if (Objects.equals(noteDO.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture.supplyAsync(
                    () -> keyValueRpcService.findNoteContent(noteDO.getContentUuid()), threadPoolTaskExecutor
            );
        }
        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailRespVO> resultFuture = CompletableFuture
                .allOf(userResultFuture, contentResultFuture)
                .thenApply(s -> {
                    // 获取 Future结果
                    FindUserByIdRespDTO findUserByIdRespDTO = userResultFuture.join();
                    String content = finalContentResultFuture.join();

                    // 笔记类型
                    Integer noteType = noteDO.getType();
                    // 图文笔记图片链接(字符串)
                    String imgUrisStr = noteDO.getImgUris();
                    // 图文笔记图片链接(集合)
                    List<String> imgUris = null;
                    // 如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
                    if (Objects.equals(noteType, NoteTypeEnum.IMAGE_TEXT.getCode())
                            && StringUtils.isNotBlank(imgUrisStr)) {
                        imgUris = List.of(imgUrisStr.split(","));
                    }

                    // 构建返回VO实体类
                    return FindNoteDetailRespVO.builder()
                            .id(noteDO.getId())
                            .type(noteDO.getType())
                            .title(noteDO.getTitle())
                            .content(content)
                            .imgUris(imgUris)
                            .topicId(noteDO.getTopicId())
                            .topicName(noteDO.getTopicName())
                            .creatorId(noteDO.getCreatorId())
                            .creatorName(findUserByIdRespDTO.getNickName())
                            .avatar(findUserByIdRespDTO.getAvatar())
                            .videoUri(noteDO.getVideoUri())
                            .updateTime(noteDO.getUpdateTime())
                            .visible(noteDO.getVisible())
                            .build();
                });

        // 获取拼装后的 findNoteDetailRspVO
        FindNoteDetailRespVO findNoteDetailRespVO = resultFuture.get();

        threadPoolTaskExecutor.submit(() -> {
            String noteDetailJson1 = JsonUtils.toJsonString(findNoteDetailRespVO);
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
            redisTemplate.opsForValue().set(noteDetailRedisKey, noteDetailJson1, expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(findNoteDetailRespVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO) {
        // 笔记Id
        Long noteId = updateNoteReqVO.getId();
        // 笔记类型
        Integer noteType = updateNoteReqVO.getType();

        // 获取对应的枚举类型
        NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(noteType);

        // 若非图文视频，则抛出业务异常
        if (Objects.isNull(noteTypeEnum)) {
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }
        String imgUris = null;
        String videoUri = null;
        switch (noteTypeEnum) {
            case IMAGE_TEXT -> {
                List<String> imgUriList = updateNoteReqVO.getImgUris();
                // 校验图片是否为空
                Preconditions.checkArgument(CollUtil.isNotEmpty(imgUriList), "笔记图片不能为空");
                Preconditions.checkArgument(imgUriList.size() <= 8, "笔记图片不能多于 8 张");

                imgUris = StringUtils.join(imgUriList, ",");
                break;
            }
            case VIDEO -> {
                videoUri = updateNoteReqVO.getVideoUri();

                // 校验视频链接是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(videoUri), "笔记视频不能为空");
                break;
            }
            default -> {
                break;
            }
        }

        // 当前登录用户Id
        Long currUserId = LoginUserContextHolder.getUserId();
        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限，非发布者不允许操作笔记
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 话题
        Long topicId = updateNoteReqVO.getTopicId();
        String topicName = null;
        if (Objects.isNull(topicId)) {
            topicName = topicDOMapper.selectNameByPrimaryKey(topicId);

            // 判断话题名称是否为空
            if (StringUtils.isBlank(topicName)) {
                throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
            }
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 更新笔记元数据表
        String content = updateNoteReqVO.getContent();
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUris(imgUris)
                .title(updateNoteReqVO.getTitle())
                .topicId(updateNoteReqVO.getTopicId())
                .topicName(topicName)
                .type(noteType)
                .updateTime(LocalDateTime.now())
                .videoUri(videoUri)
                .build();

        noteDOMapper.updateByPrimaryKey(noteDO);

        // 删除 Redis 缓存  一致性保证：延迟双删策略
        // 异步发送延时消息
        Message<String> message = MessageBuilder.withPayload(String.valueOf(noteId)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 笔记缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 笔记缓存消息发送失败...", e);
                    }
                },
                3000,
                1);

//        redisTemplate.delete(noteDetailRedisKey);

        // 删除本地内存
//        LOCAL_CACHE.invalidate(noteId);
        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");
        // 笔记内容更新
        // 查询笔记内容UUID
        NoteDO noteDO1 = noteDOMapper.selectByPrimaryKey(noteId);
        String contentUuid = noteDO1.getContentUuid();

        // 笔记内容是否更新成功
        boolean isUpdateSuccess = false;
        if (StringUtils.isBlank(content)) {
            // 若笔记内容为空, 删除K-V存储
            isUpdateSuccess = keyValueRpcService.deleteNoteContent(contentUuid);
        } else {
            // 若将无内容的笔记，更新为了有内容的笔记，需要重新生成 UUID
            contentUuid = StringUtils.isBlank(content) ? UUID.randomUUID().toString() : contentUuid;
            // 调用 K-V 更新短文本
            isUpdateSuccess = keyValueRpcService.saveNoteContent(contentUuid, content);
        }
        if (!isUpdateSuccess) {
            throw new BizException(ResponseCodeEnum.NOTE_UPDATE_FAIL);
        }
        return Response.success();
    }

    /**
     * 删除本地笔记缓存
     *
     * @param noteId
     */
    @Override
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }

    /**
     * 删除笔记
     *
     * @param deleteNoteReqVO
     */
    @Override
    public Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO) {
        // 笔记ID
        Long noteId = deleteNoteReqVO.getId();

        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }
        // 判断权限：非笔记发布者不允许删除笔记
        Long currUserId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
        // 逻辑删除
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .status(NoteStatusEnum.DELETED.getCode())
                .updateTime(LocalDateTime.now())
                .build();
        int count = noteDOMapper.updateByPrimaryKeySelective(noteDO);

        // 如果更新的行数为0，说明笔记不存在
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 同步广播
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");
        return Response.success();
    }

    /**
     * 更新笔记可见性为仅自己可见
     *
     * @param updateNoteVisibleOnlyMeReqVO
     * @return
     */
    @Override
    public Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO) {

        Long noteId = updateNoteVisibleOnlyMeReqVO.getId();

        NoteDO selectNoteDO = noteDOMapper.selectByPrimaryKey(noteId);

        // 判断笔记是否存在
        if (Objects.isNull(selectNoteDO)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }

        // 判断权限：非笔记发布者不允许修改笔记权限
        Long currUserId = LoginUserContextHolder.getUserId();
        if (!Objects.equals(currUserId, selectNoteDO.getCreatorId())) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        // 构建笔记实体类
        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .visible(NoteVisibleEnum.PRIVATE.getCode())
                .updateTime(LocalDateTime.now())
                .build();

        // 更新笔记可见性
        int count = noteDOMapper.updateVisibleOnlyMe(noteDO);
        // 若影响的行数为 0，则表示该笔记无法修改为仅自己可见
        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();

    }

    /**
     * 置顶笔记
     *
     * @param topNoteReqVO
     * @return
     */
    @Override
    public Response<?> topNote(TopNoteReqVO topNoteReqVO) {
        // 笔记ID
        Long noteId = topNoteReqVO.getId();
        // 是否置顶
        Boolean isTop = topNoteReqVO.getIsTop();
        // 当前用户Id
        Long userId = LoginUserContextHolder.getUserId();

        NoteDO noteDO = NoteDO.builder()
                .id(noteId)
                .isTop(isTop)
                .updateTime(LocalDateTime.now())
                .creatorId(userId)
                .build();

        int count = noteDOMapper.updateIsTop(noteDO);

        if (count == 0) {
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }
        // 删除 Redis 缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);

        // 同步发送广播模式 MQ，将所有实例中的本地缓存都删除掉
        rocketMQTemplate.syncSend(MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("====> MQ：删除笔记本地缓存发送成功...");

        return Response.success();
    }

    /**
     * 笔记点赞服务
     *
     * @param likeNoteReqVO
     * @return
     */
    @Override
    public Response<?> likeNote(LikeNoteReqVO likeNoteReqVO) {
        // 笔记ID
        Long noteId = likeNoteReqVO.getId();
        // 1. 校验被点赞的笔记是否存在, 若存在返回发布笔记作者Id
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        // 2. 判断目标笔记，是否已经点赞过
        // 当前用户 Id
        Long userId = LoginUserContextHolder.getUserId();

        //  Roaring Bitmap Key
        String rbitmapUserNoteLikeListKey = RedisKeyConstants.buildRBitmapUserNoteLikeListKey(userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // 执行 Lua 脚本
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_note_like_check.lua")));
        // Lua 返回类型
        script.setResultType(Long.class);
        // 返回 Lua 结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId);

        NoteLikeLuaResultEnum noteLikeLuaResultEnum = NoteLikeLuaResultEnum.valueOf(result);

        // 用户点赞列表 ZSet Key
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);

        switch (noteLikeLuaResultEnum) {
            // Redis 中 Roaring Bitmap 不存在
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被点赞，并异步初始化布隆过滤器，设置过期时间
                int count = noteLikeDOMapper.selectCountByUserIdAndNoteId(userId, noteId);
                // 保底1天+随机秒数
                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                // 目标笔记点赞
                if (count > 0) {
//                    asynBatchAddNoteLike2BloomAndExpire(userId, expireSeconds, bloomUserNoteLikeListKey);
                    threadPoolTaskExecutor.submit(() ->
                            batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey));
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }

                // 若目标笔记未被点赞，查询当前用户是否有点赞其他笔记，有则同步初始化 Roaring Bitmap
                batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey);

                // 若数据库中也没有点赞记录，说明该用户还未点赞过任何笔记
                // Lua 脚本路径
//                script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_add_note_like_and_expire.lua")));
                script.setResultType(Long.class);
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId, expireSeconds);
            }
            // 目标笔记已经被点赞
            case NOTE_LIKED -> {
                Double score = redisTemplate.opsForZSet().score(userNoteLikeZSetKey, noteId);

                if (Objects.nonNull(score)) {
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }
                // 若 Score 为空，则表示 ZSet 点赞列表中不存在，查询数据库校验
                int count = noteLikeDOMapper.selectNoteIsLiked(userId, noteId);

                if (count > 0) {
                    // 数据库里面有点赞记录，而 Redis 中 ZSet 不存在，需要重新异步初始化 ZSet
                    asynInitUserNoteLikesZSet(userId, userNoteLikeZSetKey);
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_LIKED);
                }
            }
        }
        // 3. 更新用户 ZSET 点赞列表
        LocalDateTime now = LocalDateTime.now();
        // Lua 脚本
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_like_check_and_update_zset.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        result = redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
        // 若 ZSet 列表不存在，需要重新初始化
        if (Objects.equals(result, NoteLikeLuaResultEnum.NOT_EXIST.getCode())) {
            // 查询当前用户最新点赞的 100 篇笔记
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);

            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
            // Lua 脚本路径
            script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
            // 返回值类型
            script2.setResultType(Long.class);

            // 若数据库中存在点赞记录，需要批量同步
            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                // 构建 Lua 参数
                Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireTime);

                redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);
                // 再次调用 note_like_check_and_update_zset.lua 脚本，将点赞的笔记添加到 zset 中
                redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));

            } else {    // 若数据库中，无点赞过的笔记记录，则直接将当前点赞的笔记 ID 添加到 ZSet 中，随机过期时间
                List<Object> luaArgs = Lists.newArrayList();
                luaArgs.add(DateUtils.localDateTime2Timestamp(LocalDateTime.now()));        // score ：点赞时间戳
                luaArgs.add(noteId);
                luaArgs.add(expireTime);
                redisTemplate.execute(script, Collections.singletonList(userNoteLikeZSetKey), luaArgs.toArray());
            }
        }
        // 4. 发送 MQ, 将点赞数据落库

        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(LikeUnlikeNoteTypeEnum.LIKE.getCode())
                .createTime(now)
                .noteCreatorId(creatorId)
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeNoteMqDTO)).build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_LIKE;

        String hashKey = String.valueOf(userId);

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记点赞】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    @Override
    public Response<?> unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO) {
        // 笔记Id
        Long noteId = unlikeNoteReqVO.getId();

        // 1.校验笔记是否存在, 若存在返回发布笔记的作者Id
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2.校验笔记是否被点赞
        // 当前登录用户
        Long userId = LoginUserContextHolder.getUserId();
        // 布隆过滤器 Redis Key
        String rbitmapUserNoteLikeListKey = RedisKeyConstants.buildRBitmapUserNoteLikeListKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_note_unlike_check.lua")));
        script.setResultType(Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), noteId);

        NoteUnlikeLuaResultEnum noteUnlikeLuaResultEnum = NoteUnlikeLuaResultEnum.valueOf(result);

        switch (noteUnlikeLuaResultEnum) {
            // 不存在
            case NOT_EXIST -> {
                // 异步初始化布隆过滤器
                threadPoolTaskExecutor.submit(() -> {
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    batchAddNoteLike2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteLikeListKey);
                });
                // 数据库中校验笔记是否被点赞
                int count = noteLikeDOMapper.selectCountByUserIdAndNoteId(userId, noteId);
                // 未点赞，无法取消点赞操作，抛出业务异常
                if (count == 0) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
                }

            }
            case NOTE_NOT_LIKED -> // 布隆过滤器校验目标笔记未被点赞（判断绝对正确）
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_LIKED);
        }

        // 3.删除 ZSET 中的已点赞的笔记 Id
        String userNoteLikeZSetKey = RedisKeyConstants.buildUserNoteLikeZSetKey(userId);

        redisTemplate.opsForZSet().remove(userNoteLikeZSetKey, noteId);

        // 更新数据库
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = LikeUnlikeNoteMqDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(LikeUnlikeNoteTypeEnum.UNLIKE.getCode())
                .createTime(LocalDateTime.now())
                .noteCreatorId(creatorId)
                .build();
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnlikeNoteMqDTO)).build();

        String destination = MQConstants.TOPIC_LIKE_OR_UNLIKE + ":" + MQConstants.TAG_UNLIKE;

        String hashKey = String.valueOf(userId);
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消点赞】MQ 发送异常: ", throwable);
            }
        });
        return Response.success();
    }

    /**
     * 收藏笔记
     *
     * @param collectNoteReqVO
     * @return
     */
    @Override
    public Response<?> collectNote(CollectNoteReqVO collectNoteReqVO) {
        Long noteId = collectNoteReqVO.getId();

        // 1. 校验被收藏的笔记是否存在, 返回发布笔记的作者Id
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);

        // 2. 判断目标笔记，是否已经收藏过
        // 当前登录用户 Id
        Long userId = LoginUserContextHolder.getUserId();

//        String bloomUserNoteCollectListKey = RedisKeyConstants.buildBloomUserNoteCollectListKey(userId);
        // Roaring Bitmap Key
        String rbitmapUserNoteCollectListKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_note_collect_check.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId);
        NoteCollectLuaResultEnum noteCollectLuaResultEnum = NoteCollectLuaResultEnum.valueOf(result);

        // 用户收藏列表 ZSet Key
        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);
        switch (noteCollectLuaResultEnum) {
            case NOT_EXIST -> {
                // 从数据库中校验笔记是否被收藏，并异步初始化布隆过滤器，设置过期时间
                int count = noteCollectionDOMapper.selectCountByUserIdAndNoteId(userId, noteId);

                long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                // 目标笔记已经被收藏
                if (count > 0) {
                    // 异步初始化布隆过滤器
                    threadPoolTaskExecutor.submit(() -> {
                        batchAddNoteCollect2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey);
                    });
                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
                }
                // 若目标笔记未被收藏，查询当前用户是否有收藏其他笔记，有则同步初始化布隆过滤器
                batchAddNoteCollect2RBitmapAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey);
                // 添加当前收藏笔记 ID 到 Roaring Bitmap 中
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_add_note_collect_and_expire.lua")));
                script.setResultType(Long.class);
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId, expireSeconds);

            }
            // 目标笔记已经被收藏 (可能存在误判，需要进一步确认)
            case NOTE_COLLECTED -> {
//                // 校验 ZSet 列表中是否包含被收藏的笔记ID
//                Double score = redisTemplate.opsForZSet().score(userNoteCollectZSetKey, noteId);
//                
//                if (Objects.nonNull(score)) throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
//                
//                // 若 Score 为空，则表示 ZSet 收藏列表中不存在，查询数据库校验
//                int count = noteCollectionDOMapper.selectNoteIsCollected(userId, noteId);
//
//                if (count > 0) {
//                    // 数据库里面有收藏记录，而 Redis 中 ZSet 已过期被删除的话，需要重新异步初始化 ZSet
//                    asynInitUserNoteCollectsZset(userId, userNoteCollectZSetKey);
//
//                    throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
//                }

                throw new BizException(ResponseCodeEnum.NOTE_ALREADY_COLLECTED);
            }
        }
        // 3. 更新用户 ZSET 收藏列表
        LocalDateTime now = LocalDateTime.now();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_collect_check_and_update_zset.lua")));
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        result = redisTemplate.execute(script, Collections.singletonList(userNoteCollectZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
        // ZSET 列表不存在，需要重新初始化
        if (Objects.equals(result, NoteCollectLuaResultEnum.NOT_EXIST.getCode())) {
            // 查询当前用户最新收藏的 300 篇笔记
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectCollectedByUserIdAndLimit(userId, 300);
            // 设置过期时间
            long expireTime = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

            DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
            script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
            script2.setResultType(Long.class);
            // 若数据库中存在历史收藏笔记，需要批量同步
            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                // 构建 Lua 参数
                Object[] luaArgs = buildNoteCollectZSetLuaArgs(noteCollectionDOS, expireTime);
                redisTemplate.execute(script2, Collections.singletonList(userNoteCollectZSetKey), luaArgs);

                // 再次调用 note_collect_check_and_update_zset.lua 脚本，将收藏的笔记添加到 zset 中
                redisTemplate.execute(script, Collections.singletonList(userNoteCollectZSetKey), noteId, DateUtils.localDateTime2Timestamp(now));
            } else {// 若无历史收藏的笔记，则直接将当前收藏的笔记 ID 添加到 ZSet 中，随机过期时间
                List<Object> luaArgs = Lists.newArrayList();
                luaArgs.add(DateUtils.localDateTime2Timestamp(LocalDateTime.now())); // score ：收藏时间戳
                luaArgs.add(noteId);
                luaArgs.add(expireTime);
                redisTemplate.execute(script2, Collections.singletonList(userNoteCollectZSetKey), luaArgs.toArray());
            }
        }
        // 4. 发送 MQ, 将收藏数据落库
        CollectUnCollectNoteMqDTO collectUnCollectNoteDTO = CollectUnCollectNoteMqDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(CollectUnCollectNoteTypeEnum.COLLECT.getCode())
                .createTime(now)
                .noteCreatorId(creatorId)
                .build();

        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(collectUnCollectNoteDTO)).build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT + ":" + MQConstants.TAG_COLLECT;

        String hasKey = String.valueOf(userId);
        // 异步发送顺序 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hasKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记收藏】MQ 发送异常: ", throwable);
            }
        });
        return Response.success();
    }

    /**
     * 初始化笔记收藏
     *
     * @param userId
     * @param expireSeconds
     * @param rbitmapUserNoteCollectListKey
     */
    private void batchAddNoteCollect2RBitmapAndExpire(Long userId, long expireSeconds, String rbitmapUserNoteCollectListKey) {
        try {
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectByUserId(userId);
            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_batch_add_note_collect_and_expire.lua")));
                // 返回值类型
                script.setResultType(Long.class);

                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteCollectionDOS.forEach(noteCollectionDO -> luaArgs.add(noteCollectionDO.getNoteId())); // 将每个收藏的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记收藏】Roaring Bitmap 异常: ", e);
        }
    }

    /**
     * 取消收藏笔记
     *
     * @param unCollectNoteReqVO
     * @return
     */
    @Override
    public Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO) {
        // 笔记Id
        Long noteId = unCollectNoteReqVO.getId();
        // 1.校验笔记是否存在, 返回发布笔记的作者Id
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        // 当前用户 Id
        Long userId = LoginUserContextHolder.getUserId();

        // 2. 校验笔记是否被收藏过
        // 布隆过滤器 Redis Key
//        String bloomUserNoteCollectListKey = RedisKeyConstants.buildBloomUserNoteCollectListKey(userId);
        // Roaring Bitmap Key
        String rbitmapUserNoteCollectListKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(userId);
        // Lua 脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_note_uncollect_check.lua")));
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteCollectListKey), noteId);

        NoteUnCollectLuaResultEnum noteUnCollectLuaResultEnum = NoteUnCollectLuaResultEnum.valueOf(result);

        switch (noteUnCollectLuaResultEnum) {
            case NOT_EXIST -> {
                // 异步初始化布隆过滤器
                threadPoolTaskExecutor.submit(() -> {
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                    batchAddNoteCollect2BloomAndExpire(userId, expireSeconds, rbitmapUserNoteCollectListKey);

                });
                // 数据库中校验笔记是否被收藏
                int count = noteCollectionDOMapper.selectNoteIsCollected(userId, noteId);
                // 未收藏，无法取消收藏操作，抛出业务异常
                if (count == 0) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
                }
            }
            case NOTE_NOT_COLLECTED -> {
                // 异步初始化布隆过滤器

                throw new BizException(ResponseCodeEnum.NOTE_NOT_COLLECTED);
            }
        }
        // 3. 删除 ZSET 中已收藏的笔记 ID
        String userNoteCollectZSetKey = RedisKeyConstants.buildUserNoteCollectZSetKey(userId);
        redisTemplate.opsForZSet().remove(userNoteCollectZSetKey, noteId);
        // 4. 发送 MQ, 数据更新落库
        // 构建消息体, DTO 转成 Json 字符串设置到消息体中
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = CollectUnCollectNoteMqDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(CollectUnCollectNoteTypeEnum.UN_COLLECT.getCode())
                .noteCreatorId(creatorId)
                .createTime(LocalDateTime.now())
                .build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(collectUnCollectNoteMqDTO)).build();
        String destination = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT + ":" + MQConstants.TAG_UN_COLLECT;

        String hashKey = String.valueOf(userId);
        // 异步发送顺序 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消收藏】MQ 发送异常: ", throwable);
            }
        });

        return Response.success();
    }

    /**
     * 异步初始化用户笔记收藏 ZSet
     *
     * @param userId
     * @param userNoteCollectZSetKey
     */
    private void asynInitUserNoteCollectsZset(Long userId, String userNoteCollectZSetKey) {
        threadPoolTaskExecutor.submit(() -> {
            // 判断用户笔记收藏 ZSET 是否存在
            Boolean hasKey = redisTemplate.hasKey(userNoteCollectZSetKey);
            // 不存在，则重新初始化
            if (!hasKey) {
                // 查询当前用户最新收藏的 300 篇笔记
                List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectCollectedByUserIdAndLimit(userId, 300);
                if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                    // 过期时间
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                    // 构建 Lua 参数
                    Object[] luaArgs = buildNoteCollectZSetLuaArgs(noteCollectionDOS, expireSeconds);

                    DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                    script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
                    script2.setResultType(Long.class);

                    // 执行 Lua 脚本
                    redisTemplate.execute(script2, Collections.singletonList(userNoteCollectZSetKey), luaArgs);
                }
            }
        });
    }

    /**
     * 构建 Lua 参数
     *
     * @param noteCollectionDOS
     * @param expireSeconds
     * @return
     */
    private Object[] buildNoteCollectZSetLuaArgs(List<NoteCollectionDO> noteCollectionDOS, long expireSeconds) {
        int argsLength = noteCollectionDOS.size() * 2 + 1; // 每个笔记收藏关系有 2 个参数（score 和 value），最后再跟一个过期时间

        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (NoteCollectionDO noteCollectionDO : noteCollectionDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(noteCollectionDO.getCreateTime());
            luaArgs[i + 1] = noteCollectionDO.getNoteId();
            i += 2;
        }
        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是过期时间（秒）
        return luaArgs;
    }

    /**
     * 初始化笔记收藏布隆过滤器
     *
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteCollectListKey
     */
    private void batchAddNoteCollect2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteCollectListKey) {
        try {
            // 异步全量同步，并设置过期时间
            List<NoteCollectionDO> noteCollectionDOS = noteCollectionDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteCollectionDOS)) {
                // Lua 脚本
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_batch_add_note_collect_and_expire.lua")));
                script.setResultType(Long.class);

                List<Object> luaArgs = Lists.newArrayList();
                noteCollectionDOS.forEach(noteCollectionDO -> luaArgs.add(noteCollectionDO.getNoteId())); // 将每个收藏的笔记 ID 传入
                luaArgs.add(expireSeconds);
                // 执行 Lua 脚本
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteCollectListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记收藏】布隆过滤器异常: ", e);
        }
    }

    /**
     * 批量添加笔记点赞到布隆过滤器，并设置过期时间
     *
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteLikeListKey
     */
    private void batchAddNoteLike2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteLikeListKey) {
        try {
            // 异步全量同步一下，并设置过期时间
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                // Lua 脚本路径
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_like_and_expire.lua")));
                // 返回值类型
                script.setResultType(Long.class);

                // 构建 Lua 参数
                List<Object> luaArgs = Lists.newArrayList();
                noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId())); // 将每个点赞的笔记 ID 传入
                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());
            }
        } catch (Exception e) {
            log.error("## 异步初始化【笔记点赞】布隆过滤器异常: ", e);
        }
    }

    /**
     * 异步初始化用户点赞笔记 ZSet
     *
     * @param userId
     * @param userNoteLikeZSetKey
     */
    private void asynInitUserNoteLikesZSet(Long userId, String userNoteLikeZSetKey) {
        threadPoolTaskExecutor.submit(() -> {
            // 判断用户笔记点赞 ZSET 是否存在
            Boolean hasKey = redisTemplate.hasKey(userNoteLikeZSetKey);

            // 不存在，则重新初始化
            if (Boolean.FALSE.equals(hasKey)) {
                // 查询当前用户最新点赞的 100 篇笔记
                List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectLikedByUserIdAndLimit(userId, 100);
                if (CollUtil.isNotEmpty(noteLikeDOS)) {
                    long expireSeconds = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);

                    // 构建 Lua 参数
                    Object[] luaArgs = buildNoteLikeZSetLuaArgs(noteLikeDOS, expireSeconds);

                    DefaultRedisScript<Long> script2 = new DefaultRedisScript<>();
                    // Lua 脚本路径
                    script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
                    // 返回值类型
                    script2.setResultType(Long.class);

                    redisTemplate.execute(script2, Collections.singletonList(userNoteLikeZSetKey), luaArgs);
                }
            }

        });
    }

    /**
     * 构建 Lua 参数
     *
     * @param noteLikeDOS
     * @param expireSeconds
     * @return
     */
    private Object[] buildNoteLikeZSetLuaArgs(List<NoteLikeDO> noteLikeDOS, long expireSeconds) {
        int argsLength = noteLikeDOS.size() * 2 + 1; // 每个笔记点赞关系有 2 个参数（score 和 value），最后再跟一个过期时间
        Object[] luaArgs = new Object[argsLength];

        int i = 0;
        for (NoteLikeDO noteLikeDO : noteLikeDOS) {
            luaArgs[i] = DateUtils.localDateTime2Timestamp(noteLikeDO.getCreateTime()); // 点赞时间作为 score
            luaArgs[i + 1] = noteLikeDO.getNoteId();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expireSeconds; // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    /**
     * 初始化笔记点赞 Roaring Bitmap
     *
     * @param userId
     * @param expireSeconds
     * @param rbitmapUserNoteLikeListKey
     */
    private void batchAddNoteLike2RBitmapAndExpire(Long userId, long expireSeconds, String rbitmapUserNoteLikeListKey) {
        try {
            // 用户点赞的所有笔记
            List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);

            if (CollUtil.isNotEmpty(noteLikeDOS)) {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_batch_add_note_like_and_expire.lua")));

                script.setResultType(Long.class);
                List<Object> luaArgs = Lists.newArrayList();
                noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId())); // 将每个点赞的笔记 ID 传入

                luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                redisTemplate.execute(script, Collections.singletonList(rbitmapUserNoteLikeListKey), luaArgs.toArray());
            }

        } catch (Exception e) {
            log.error("## 异步初始化【笔记点赞】Roaring Bitmap 异常: ", e);
        }
    }

    /**
     * 异步初始化布隆过滤器
     *
     * @param userId
     * @param expireSeconds
     * @param bloomUserNoteLikeListKey
     */
    private void asynBatchAddNoteLike2BloomAndExpire(Long userId, long expireSeconds, String bloomUserNoteLikeListKey) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                // 用户点赞所有笔记
                List<NoteLikeDO> noteLikeDOS = noteLikeDOMapper.selectByUserId(userId);
                if (CollUtil.isNotEmpty(noteLikeDOS)) {
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_like_and_expire.lua")));
                    script.setResultType(Long.class);

                    // 构建 Lua 脚本参数
                    List<Object> luaArgs = Lists.newArrayList();
                    noteLikeDOS.forEach(noteLikeDO -> luaArgs.add(noteLikeDO.getNoteId()));     // 将每个点赞的笔记 ID 传入

                    luaArgs.add(expireSeconds);  // 最后一个参数是过期时间（秒）
                    redisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());
                }
            } catch (Exception e) {
                log.error("## 异步初始化布隆过滤器异常: ", e);
            }
        });
    }

    /**
     * 校验笔记是否存在, 若存在，则获取笔记的发布者 Id
     *
     * @param noteId
     */
    private Long checkNoteIsExistAndGetCreatorId(Long noteId) {
        // 先从本地缓存校验
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        // 解析 Json 字符串为 VO 对象
        FindNoteDetailRespVO findNoteDetailRespVO = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailRespVO.class);
        // 若本地缓存没有
        if (Objects.isNull(findNoteDetailRespVO)) {
            // 查询 Redis
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);

            String noteDetailJson = redisTemplate.opsForValue().get(noteDetailRedisKey);

            // 解析 Json 字符串为 VO 对象
            findNoteDetailRespVO = JsonUtils.parseObject(noteDetailJson, FindNoteDetailRespVO.class);

            // 若 Redis 中也没有，则查询数据库
            if (Objects.isNull(findNoteDetailRespVO)) {
                // 查询数据库
                Long creatorId = noteDOMapper.selectCreatorIdByNoteId(noteId);
                // 数据库不存在，提示用户
                if (Objects.isNull(creatorId)) {
                    throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
                }
                // 数据库存在，异步同步缓存
                threadPoolTaskExecutor.submit(() -> {
                    FindNoteDetailReqVO findNoteDetailReqVO = FindNoteDetailReqVO.builder().id(noteId).build();
                    findNoteDetail(findNoteDetailReqVO);
                });
                return creatorId;
            }
        }
        return findNoteDetailRespVO.getCreatorId();
    }

    private void checkNoteVisibleFromVO(Long userId, FindNoteDetailRespVO findNoteDetailRespVO) {
        if (Objects.isNull(findNoteDetailRespVO)) {
            Integer visible = findNoteDetailRespVO.getVisible();
            checkNoteVisible(visible, userId, findNoteDetailRespVO.getCreatorId());
        }
    }

    /**
     * 校验笔记的可见性
     *
     * @param visible   是否可见
     * @param userId    当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long userId, Long creatorId) {
        if (Objects.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !Objects.equals(userId, creatorId)) {
            throw new BizException(ResponseCodeEnum.NOTE_NOT_FOUND);
        }
    }
}
