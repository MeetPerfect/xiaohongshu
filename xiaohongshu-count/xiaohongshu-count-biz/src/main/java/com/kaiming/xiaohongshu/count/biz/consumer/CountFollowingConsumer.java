package com.kaiming.xiaohongshu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.enums.FollowUnfollowTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * ClassName: CountFollowingConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 22:13
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_FOLLOWING,
        topic = MQConstants.TOPIC_COUNT_FOLLOWING)
@Slf4j
public class CountFollowingConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String body) {
        log.info("## 消费到了 MQ 【计数: 关注数】, {}...", body);
        
        if(StringUtils.isBlank(body)) return;

        // 关注数和粉丝数计数场景不同，单个用户无法短时间内关注大量用户，所以无需聚合
        // 直接对 Redis 中的 Hash 进行 +1 或 -1 操作即可
        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class);
        
        // 操作类型：关注 and 取关
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户 Id
        Long userId = countFollowUnfollowMqDTO.getUserId();
        
        // 更新 Redis
        String redisKey = RedisKeyConstants.buildCountUserKey(userId);
        // 判断 HashKey
        Boolean isExisted = redisTemplate.hasKey(redisKey);
        
        // 若存在
        if (isExisted) {
            // 关注数：关注 +1， 取关 -1
            long count = Objects.equals(type, FollowUnfollowTypeEnum.FOLLOW.getCode()) ? 1 : -1;
            // 对 Hash 中的 followingTotal 字段进行加减操作
            redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_FOLLOWING_TOTAL, count);
        }
        
        // 发送 MQ 消息，关注数据库
        // 构建消息体
        Message<String> message = MessageBuilder.withPayload(body).build();
        // 发送消息到 RocketMQ
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数入库】MQ 发送异常: ", throwable);
            }
        });
    }

}
