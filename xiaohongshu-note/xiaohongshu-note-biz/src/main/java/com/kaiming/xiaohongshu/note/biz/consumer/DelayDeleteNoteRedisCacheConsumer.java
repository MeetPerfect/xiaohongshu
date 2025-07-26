package com.kaiming.xiaohongshu.note.biz.consumer;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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
        try {
            List<Long> noteIdAndUserId = JsonUtils.parseList(body, Long.class);
            Long noteId = noteIdAndUserId.get(0);
            Long userId = noteIdAndUserId.get(1);
            log.info("## 延迟消息消费成功, noteId: {}, userId: {}", noteId, userId);
            // 删除 Redis 笔记缓存
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
            // 删除个人主页 - 已发布笔记列表缓存
            String publishedNoteListRedisKey  = RedisKeyConstants.buildPublishedNoteListKey(userId);
            // 批量删除
            redisTemplate.delete(Arrays.asList(noteDetailRedisKey, publishedNoteListRedisKey));
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
