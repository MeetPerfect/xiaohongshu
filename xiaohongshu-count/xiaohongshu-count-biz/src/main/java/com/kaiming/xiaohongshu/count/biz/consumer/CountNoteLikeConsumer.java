package com.kaiming.xiaohongshu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.enums.LikeUnlikeNoteTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.AggregationCountLikeUnlikeNoteMqDTO;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountLikeUnlikeNoteMqDTO;
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
 * ClassName: CountNoteLikeConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/8 21:10
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_NOTE_LIKE,
        topic = MQConstants.TOPIC_COUNT_NOTE_LIKE)
@Slf4j
public class CountNoteLikeConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    private final BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    /**
     * 消费者方法
     *
     * @param bodys
     */
    private void consumeMessage(List<String> bodys) {
        log.info("==> 【笔记点赞数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记点赞数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // List<String> 转 List<CountLikeUnlikeNoteMqDTO>
        List<CountLikeUnlikeNoteMqDTO> countLikeUnlikeNoteMqDTOS = bodys.stream()
                .map(body -> (CountLikeUnlikeNoteMqDTO) JsonUtils.parseObject(body, CountLikeUnlikeNoteMqDTO.class)).toList();

        // 按笔记 Id 进行分组
        Map<Long, List<CountLikeUnlikeNoteMqDTO>> groupMap = countLikeUnlikeNoteMqDTOS.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeNoteMqDTO::getNoteId));

        // 按组汇总数据，统计出最终的计数
        // key 为笔记 ID, value 为最终操作的计数
//        Map<Long, Integer> countMap = Maps.newHashMap();
        List<AggregationCountLikeUnlikeNoteMqDTO> countList = Lists.newArrayList();
        for (Map.Entry<Long, List<CountLikeUnlikeNoteMqDTO>> entry : groupMap.entrySet()) {
            // 笔记Id
            Long noteId = entry.getKey();
            // 发布笔记作者 Id
            Long creatorId = null;
            List<CountLikeUnlikeNoteMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;
            for (CountLikeUnlikeNoteMqDTO countLikeUnlikeNoteMqDTO : list) {
                // 设置笔记发布者用户 ID
                creatorId = countLikeUnlikeNoteMqDTO.getNoteCreatorId();
                // 获取笔记操作类型
                Integer type = countLikeUnlikeNoteMqDTO.getType();

                // 根据操作类型，获取对应枚举
                LikeUnlikeNoteTypeEnum likeUnlikeNoteTypeEnum = LikeUnlikeNoteTypeEnum.valueOf(type);
                // 若枚举为空，跳到下一次循环
                if (Objects.isNull(likeUnlikeNoteTypeEnum)) continue;

                switch (likeUnlikeNoteTypeEnum) {
                    case LIKE -> finalCount += 1;
                    case UNLIKE -> finalCount -= 1;
                }
            }
            // 将分组后统计出的最终计数，存入 countMap 中
//            countMap.put(entry.getKey(), finalCount);
            countList.add(AggregationCountLikeUnlikeNoteMqDTO.builder()
                    .noteId(noteId)
                    .creatorId(creatorId)
                    .count(finalCount)
                    .build());
            
        }
        log.info("## 【笔记点赞数】聚合后的计数数据: {}", JsonUtils.toJsonString(countList));


        // 更新 Redis
        countList.forEach(item -> {
            
            // 笔记发布者 Id
            Long creatorId = item.getCreatorId();
            // 笔记 Id
            Long noteId = item.getNoteId();
            // 聚合计数
            Integer count = item.getCount();
            
            // Redis key
            String redisKey = RedisKeyConstants.buildCountNoteKey(noteId);
            // 判断 Redis 中是否存在
            boolean isExisted = redisTemplate.hasKey(redisKey);

            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isExisted) {
                // 对目标用户 Hash 中的点赞数字段进行计数操作
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_LIKE_TOTAL, count);
            }
            
            // 更新用户维度的点赞数
            String countUserRedisKey = RedisKeyConstants.buildCountUserKey(creatorId);
            Boolean isCountUserExisted = redisTemplate.hasKey(countUserRedisKey);
            if (isCountUserExisted) {
                redisTemplate.opsForHash().increment(countUserRedisKey, RedisKeyConstants.FIELD_LIKE_TOTAL, count);
            }
        });

        // 发送 MQ, 笔记点赞数据落库
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countList)).build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：笔记点赞数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：笔记点赞数入库】MQ 发送异常: ", throwable);
            }
        });
    }
}
