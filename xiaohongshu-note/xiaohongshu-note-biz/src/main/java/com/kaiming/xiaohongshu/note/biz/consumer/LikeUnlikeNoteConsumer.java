package com.kaiming.xiaohongshu.note.biz.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.kaiming.xiaohongshu.note.biz.model.dto.LikeUnlikeNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

/**
 * ClassName: LikeUnlikeNoteConsumer
 * Package: com.kaiming.xiaohongshu.note.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/6 15:44
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE)
@Slf4j
public class LikeUnlikeNoteConsumer implements RocketMQListener<Message> {
    // 每秒创建 5000 个令牌
    private RateLimiter rateLimiter = RateLimiter.create(5000);

    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();

        // 幂等性: 通过联合唯一索引保证

        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();
        log.info("==> LikeUnlikeNoteConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);
        // 根据 MQ 标签，判断操作类型

        if (Objects.equals(tags, MQConstants.TAG_LIKE)) {
            handleLikeNoteTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNLIKE)) {
            handleUnlikeNoteTagMessage(bodyJsonStr);
        }
    }

    /**
     * 笔记点赞
     *
     * @param bodyJsonStr
     */
    private void handleLikeNoteTagMessage(String bodyJsonStr) {
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);

        if (Objects.isNull(likeUnlikeNoteMqDTO)) return;

        Long userId = likeUnlikeNoteMqDTO.getUserId();
        Long noteId = likeUnlikeNoteMqDTO.getNoteId();
        Integer type = likeUnlikeNoteMqDTO.getType();
        LocalDateTime createTime = likeUnlikeNoteMqDTO.getCreateTime();

        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .status(type)
                .createTime(createTime)
                .build();

        // 添加或更新笔记点赞记录
        int count = noteLikeDOMapper.insertOrUpdate(noteLikeDO);

        if (count == 0) return;

        // 更新数据库后，发送计数 MQ
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();
        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数: 笔记点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数: 笔记点赞】MQ 发送异常: ", throwable);
            }
        });
    }

    /**
     * 笔记取消点赞
     *
     * @param bodyJsonStr
     */
    private void handleUnlikeNoteTagMessage(String bodyJsonStr) {
        LikeUnlikeNoteMqDTO likeUnlikeNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);

        if (Objects.isNull(likeUnlikeNoteMqDTO)) return;

        // 用户 Id
        Long userId = likeUnlikeNoteMqDTO.getUserId();
        // 笔记 Id
        Long noteId = likeUnlikeNoteMqDTO.getNoteId();
        // 操作类型
        Integer type = likeUnlikeNoteMqDTO.getType();
        // 创建时间
        LocalDateTime createTime = likeUnlikeNoteMqDTO.getCreateTime();

        // 构建 DTO 对象
        NoteLikeDO noteLikeDO = NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .status(type)
                .createTime(createTime)
                .build();
        // 取消点赞：数据库记录更新
        int count = noteLikeDOMapper.update2UnlikeByUserIdAndNoteId(noteLikeDO);

        if (count == 0) return;
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();
        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数: 笔记取消点赞】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数: 笔记取消点赞】MQ 发送异常: ", throwable);
            }
        });
    }
}
