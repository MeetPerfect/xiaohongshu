package com.kaiming.xiaohongshu.note.biz.consumer;

import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * ClassName: DelayDeleteNoteRedisCacheConsumer
 * Package: com.kaiming.xiaohongshu.note.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/26 22:30
 * @Version 1.0
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE,
        topic = MQConstants.TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE)
public class DelayDeleteNoteRedisCacheConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 延迟消息消费成功, noteId: {}", noteId);
        // 删除Redis笔记缓存
        String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
        redisTemplate.delete(noteDetailRedisKey);
    }
}
