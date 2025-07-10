package com.kaiming.xiaohongshu.note.biz.consumer;


import cn.hutool.core.collection.CollUtil;
import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteCollectionDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.kaiming.xiaohongshu.note.biz.model.dto.CollectUnCollectNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ClassName: CollectUnCollectNoteConsumer
 * Package: com.kaiming.xiaohongshu.note.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 14:50
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
        topic = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
        consumeMode = ConsumeMode.ORDERLY)
@Slf4j
public class CollectUnCollectNoteConsumer implements RocketMQListener<Message> {

    private RateLimiter rateLimiter = RateLimiter.create(5000);
    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public void onMessage(Message message) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();

        // 幂等性: 通过联合唯一索引保证

        // 消息体
        String bodyJsonStr = new String(message.getBody());

        // tag
        String tags = message.getTags();

        log.info("==> CollectUnCollectNoteConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);
        if (Objects.equals(tags, MQConstants.TAG_COLLECT)) {
            handleCollectNoteTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UN_COLLECT)) {
            handleUnCollectNoteTagMessage(bodyJsonStr);
        }
    }

    /**
     * 处理收藏笔记的消息
     *
     * @param bodyJsonStr
     */
    private void handleCollectNoteTagMessage(String bodyJsonStr) {
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMqDTO.class);
        if (Objects.isNull(collectUnCollectNoteMqDTO)) return;
        
        // 用于Id
        Long userId = collectUnCollectNoteMqDTO.getUserId();
        // 笔记Id
        Long noteId = collectUnCollectNoteMqDTO.getNoteId();
        // 收藏状态
        Integer type = collectUnCollectNoteMqDTO.getType();
        // 创建时间
        LocalDateTime createTime = collectUnCollectNoteMqDTO.getCreateTime();

        NoteCollectionDO noteCollectionDO = NoteCollectionDO.builder()
                .userId(userId)
                .noteId(noteId)
                .status(type)
                .createTime(createTime)
                .build();
        // 添加或更新笔记收藏记录
        int count = noteCollectionDOMapper.insertOrUpdate(noteCollectionDO);
        
        if (count == 0) return;
        // 更新数据库, 发送计数 MQ
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();
        
        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数: 笔记收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数: 笔记收藏】MQ 发送异常: ", throwable);
            }
        });
    }

    /**
     * 处理取消收藏笔记的消息
     *
     * @param bodyJsonStr
     */
    private void handleUnCollectNoteTagMessage(String bodyJsonStr) {
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMqDTO.class);
        
        if (Objects.isNull(collectUnCollectNoteMqDTO)) return;
        // 用于Id
        Long userId = collectUnCollectNoteMqDTO.getUserId();
        // 笔记Id
        Long noteId = collectUnCollectNoteMqDTO.getNoteId();
        // 收藏状态
        Integer type = collectUnCollectNoteMqDTO.getType();
        // 创建时间
        LocalDateTime createTime = collectUnCollectNoteMqDTO.getCreateTime();

        NoteCollectionDO noteCollectionDO = NoteCollectionDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();
        
        // 更新笔记收藏记录
        int count = noteCollectionDOMapper.update2UnCollectByUserIdAndNoteId(noteCollectionDO);
        
        if (count == 0) return;
        // 发送计数 MQ

        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数: 笔记取消收藏】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数: 笔记取消收藏】MQ 发送异常: ", throwable);
            }
        });
    }
}
