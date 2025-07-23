package com.kaiming.xiaohongshu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.enums.LikeUnlikeCommentTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.AggregationCountLikeUnlikeCommentMqDTO;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountLikeUnlikeCommentMqDTO;
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
 * ClassName: CountCommentLikeConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 12:20
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_count_" + MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_COMMENT_LIKE_OR_UNLIKE)
@Slf4j
public class CountCommentLikeConsumer implements RocketMQListener<String> {
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    
    private BufferTrigger bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)      // 缓存队列的最大容量
            .batchSize(1000)        // 一批次最多聚合 1000 条
            .linger(Duration.ofMillis(1))   // 多久聚合一次
            .setConsumerEx(this::consumeMessage)    // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【评论点赞数】聚合消息, size: {}", bodys.size());
        log.info("==> 【评论点赞数】聚合消息, {}", JsonUtils.toJsonString(bodys));
        // List<String> 转 List<CountLikeUnlikeCommentMqDTO>
        List<CountLikeUnlikeCommentMqDTO> countLikeUnlikeCommentMqDTOS = bodys.stream()
                .map(body -> (CountLikeUnlikeCommentMqDTO) JsonUtils.parseObject(body, CountLikeUnlikeCommentMqDTO.class)).toList();

        // 按评论 ID 进行分组
        Map<Long, List<CountLikeUnlikeCommentMqDTO>> groupMap = countLikeUnlikeCommentMqDTOS.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeCommentMqDTO::getCommentId));

        // 按组汇总数据，统计出最终的计数
        // 最终操作的计数对象
        List<AggregationCountLikeUnlikeCommentMqDTO> countList = Lists.newArrayList();

        for (Map.Entry<Long, List<CountLikeUnlikeCommentMqDTO>> entry : groupMap.entrySet()) {
            // 评论Id
            Long commentId = entry.getKey();
            List<CountLikeUnlikeCommentMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (CountLikeUnlikeCommentMqDTO countLikeUnlikeCommentMqDTO : list) {
                // 操作类型
                Integer type = countLikeUnlikeCommentMqDTO.getType();
                // 根据操作类型，获取对应枚举
                LikeUnlikeCommentTypeEnum likeUnlikeCommentTypeEnum = LikeUnlikeCommentTypeEnum.valueOf(type);

                // 若枚举为空，跳到下一次循环
                if (Objects.isNull(likeUnlikeCommentTypeEnum)) continue;

                switch (likeUnlikeCommentTypeEnum) {
                    case LIKE -> finalCount += 1;
                    case UNLIKE -> finalCount -= 1;
                }
            }
            // 将分组后统计出的最终计数，存入 countList 中
            countList.add(AggregationCountLikeUnlikeCommentMqDTO.builder()
                    .commentId(commentId)
                    .count(finalCount)
                    .build());
        }
        log.info("## 【评论点赞数】聚合后的计数数据: {}", JsonUtils.toJsonString(countList));

        // 更新 Redis
        countList.forEach(item -> {
            // 评论 Id
            Long commentId = item.getCommentId();
            // 聚合数
            Integer count = item.getCount();
            // Redis 的 key
            String key = RedisKeyConstants.buildCountCommentKey(commentId);
            boolean hasKey = redisTemplate.hasKey(key);
            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (hasKey) {
                // 对目标用户 Hash 中的点赞数字段进行计数操作
                redisTemplate.opsForHash().increment(key, RedisKeyConstants.FIELD_LIKE_TOTAL, count);
            }
        });
        
        // 数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countList)).build();
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_COMMENT_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：评论点赞数写库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：评论点赞数写库】MQ 发送异常: ", throwable);
            }
        });
    }
}
