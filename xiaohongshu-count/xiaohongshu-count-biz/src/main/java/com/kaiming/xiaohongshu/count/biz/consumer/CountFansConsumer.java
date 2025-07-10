package com.kaiming.xiaohongshu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.enums.FollowUnfollowTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: CountFansConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 22:13
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_FANS,
        topic = MQConstants.TOPIC_COUNT_FANS)
@Slf4j
public class CountFansConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)   // 缓存队列的最大容量
            .batchSize(1000) // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1))   // 多久聚合一次
            .setConsumerEx(this::consumeMessage)    // 设置消费者方法
            .build();

    private void consumeMessage(List<String> bodys) {
        log.info("==> 聚合消息, size: {}", bodys.size());
        log.info("==> 聚合消息, {}", JsonUtils.toJsonString(bodys));

        // List<String> 转 List<CountFollowUnfollowMqDTO>
        List<CountFollowUnfollowMqDTO> countFollowUnfollowMqDTOS = bodys.stream()
                .map(body -> (CountFollowUnfollowMqDTO) JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class)).toList();

        // 按目标用户分组
        Map<Long, List<CountFollowUnfollowMqDTO>> groupMap = countFollowUnfollowMqDTOS.stream()
                .collect(Collectors.groupingBy(CountFollowUnfollowMqDTO::getTargetUserId));

        // 按组汇总数据，统计出最终的计数
        // key 为目标用户ID, value 为最终操作的计数
        Map<Long, Integer> countMap = Maps.newHashMap();

        for (Map.Entry<Long, List<CountFollowUnfollowMqDTO>> entry : groupMap.entrySet()) {
            List<CountFollowUnfollowMqDTO> list = entry.getValue();

            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (CountFollowUnfollowMqDTO countFollowUnfollowMqDTO : list) {
                // 操作类型
                Integer type = countFollowUnfollowMqDTO.getType();
                FollowUnfollowTypeEnum followUnfollowTypeEnum = FollowUnfollowTypeEnum.valueOf(type);

                if (Objects.isNull(followUnfollowTypeEnum)) continue;

                switch (followUnfollowTypeEnum) {
                    case FOLLOW -> finalCount += 1; // 关注
                    case UNFOLLOW -> finalCount -= 1; // 取关
                }
            }
            // 将分组后统计出的最终计数，存入 countMap 中
            countMap.put(entry.getKey(), finalCount);
        }
        log.info("## 聚合后的计数数据: {}", JsonUtils.toJsonString(countMap));

        // 更新 Redis
        countMap.forEach((k, v) -> {
            // Redis Key
            String redisKey = RedisKeyConstants.buildCountUserKey(k);
            // 判断 Redis 中 Hash 是否存在
            Boolean isExisted = redisTemplate.hasKey(redisKey);
            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isExisted) {
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_FANS_TOTAL, v);
            }
        });

        // 发送 MQ, 计数数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countMap)).build();

        // 异步发送 MQ 消息，提升接口响应速度
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：粉丝数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：粉丝数入库】MQ 发送异常: ", throwable);
            }
        });
    }

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body);
    }
}
