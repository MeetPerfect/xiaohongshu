package com.kaiming.xiaohongshu.note.biz.listener;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.note.biz.convert.NoteConvert;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteDOMapper;
import com.kaiming.xiaohongshu.note.biz.enums.NoteOperateEnum;
import com.kaiming.xiaohongshu.note.biz.enums.NoteTypeEnum;
import com.kaiming.xiaohongshu.note.biz.model.dto.NoteOperateMqDTO;
import com.kaiming.xiaohongshu.note.biz.model.dto.PublishNoteDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * ClassName: PublishNote2DBLocalTransactionListener
 * Package: com.kaiming.xiaohongshu.note.biz.listener
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/26 16:55
 * @Version 1.0
 */
@RocketMQTransactionListener
@Slf4j
public class PublishNote2DBLocalTransactionListener implements RocketMQLocalTransactionListener {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /***
     * 执行本地事务
     * @param msg
     * @param o
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object o) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始执行本地事务：{}", payload);
        // 2. 执行本地事务（如数据库操作）
        // 消息体 Json 转 DTO
        PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(payload, PublishNoteDTO.class);
        Long noteId = publishNoteDTO.getId();
        Long creatorId = publishNoteDTO.getCreatorId();

        // 删除个人主页 - 已发布笔记列表缓存
        // TODO: 应采取灵活的策略，如果是大V, 应该直接更新缓存，而不是直接删除；普通用户则可直接删除
        String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(noteId);
        redisTemplate.delete(publishedNoteListRedisKey);

        // 2. 执行本地事务（如数据库操作）
        try {
            // DTO 转 DO
            NoteDO noteDO = NoteConvert.INSTANCE.convertDTO2DO(publishNoteDTO);
            // 笔记元数据写库
            noteDOMapper.insert(noteDO);
        } catch (Exception e) {
            log.error("## 笔记元数据存储失败: ", e);
            return RocketMQLocalTransactionState.ROLLBACK; // 回滚事务消息
        }

        // 延迟双删：发送延迟消息
        sendDelayDeleteRedisPublishedNoteListCacheMQ(creatorId);
        // 发送 MQ
        // 构建消息体 DTO
        NoteOperateMqDTO noteOperateMqDTO = NoteOperateMqDTO.builder()
                .creatorId(creatorId)
                .noteId(noteId)
                .type(NoteOperateEnum.PUBLIC.getCode())
                .build();
        // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
        Message<String> message = MessageBuilder
                .withPayload(JsonUtils.toJsonString(noteOperateMqDTO)).build();
        // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
        String destination = MQConstants.TOPIC_NOTE_OPERATE + ":" + MQConstants.TAG_NOTE_PUBLISH;

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记发布】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记发布】MQ 发送异常: ", throwable);
            }
        });
        // 3. 提交事务状态，“half 消息” 转换为正式消息
        return RocketMQLocalTransactionState.COMMIT;

    }

    /**
     * 延时双删
     *
     * @param creatorId
     */
    private void sendDelayDeleteRedisPublishedNoteListCacheMQ(Long creatorId) {
        Message<String> message = MessageBuilder.withPayload(String.valueOf(creatorId)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, message, new SendCallback() {

                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 已发布笔记列表缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 已发布笔记列表缓存消息发送失败...", e);
                    }
                },
                3000,       // 超时时间
                1);                // 延迟级别，1 表示延时 1s
    }

    /**
     * 事务状态回查
     *
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始事务回查：{}", payload);
        // 消息体 Json 转 DTO
        PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(payload, PublishNoteDTO.class);
        Long noteId = publishNoteDTO.getId();
        // 2. 检查本地事务状态（若记录存在，说明本地事务执行成功了；否则执行失败）
        int count = noteDOMapper.selectCountByNoteId(noteId);
        // 3. 返回最终状态
        return count == 1 ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.ROLLBACK;
    }
}
