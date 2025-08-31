package com.kaiming.xiaohongshu.count.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.enums.CollectUnCollectNoteTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.AggregationCountCollectUnCollectNoteMqDTO;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountCollectUnCollectNoteMqDTO;
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
 * ClassName: CountNoteCollectConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 19:25
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_NOTE_COLLECT,
        topic = MQConstants.TOPIC_COUNT_NOTE_COLLECT)
@Slf4j
public class CountNoteCollectConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【笔记收藏数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记收藏数】聚合消息, {}", JsonUtils.toJsonString(bodys));
        // List<String> 转 List<CountCollectUnCollectNoteMqDTO>
        List<CountCollectUnCollectNoteMqDTO> countCollectUnCollectNoteMqDTOS = bodys.stream()
                .map(body -> (CountCollectUnCollectNoteMqDTO) JsonUtils.parseObject(body, CountCollectUnCollectNoteMqDTO.class)).toList();
        // 按笔记 Id 分组
        Map<Long, List<CountCollectUnCollectNoteMqDTO>> groupMap = countCollectUnCollectNoteMqDTOS.stream()
                .collect(Collectors.groupingBy(CountCollectUnCollectNoteMqDTO::getNoteId));


        // 按组汇总数据，统计出最终的计数
        // key 为笔记 ID, value 为最终操作的计数
//        Map<Long, Integer> countMap = Maps.newHashMap();
        List<AggregationCountCollectUnCollectNoteMqDTO> countList = Lists.newArrayList();
        for (Map.Entry<Long, List<CountCollectUnCollectNoteMqDTO>> entry : groupMap.entrySet()) {
            // 笔记Id
            Long noteId = entry.getKey();
            // 笔记作者Id
            Long creatorId = null;
            List<CountCollectUnCollectNoteMqDTO> list = entry.getValue();
            // 最终的计数值，默认为 0
            int finalCount = 0;

            for (CountCollectUnCollectNoteMqDTO countCollectUnCollectNoteMqDTO : list) {
                creatorId = countCollectUnCollectNoteMqDTO.getNoteCreatorId();
                Integer type = countCollectUnCollectNoteMqDTO.getType();

                CollectUnCollectNoteTypeEnum collectUnCollectNoteTypeEnum = CollectUnCollectNoteTypeEnum.valueOf(type);
                // 若枚举为空，跳到下一次循环
                if (Objects.isNull(collectUnCollectNoteTypeEnum)) continue;

                switch (collectUnCollectNoteTypeEnum) {
                    case COLLECT -> finalCount += 1;
                    case UN_COLLECT -> finalCount -= 1;
                }
            }

            countList.add(AggregationCountCollectUnCollectNoteMqDTO.builder()
                    .creatorId(creatorId)
                    .noteId(noteId)
                    .count(finalCount)
                    .build());
        }
        log.info("## 【笔记收藏数】聚合后的计数数据: {}", JsonUtils.toJsonString(countList));

        // 更新 Redis
        countList.forEach(item -> {
            // 笔记作者Id
            Long creatorId = item.getCreatorId();
            // 笔记Id
            Long noteId = item.getNoteId();
            // 聚合之后的数
            Integer count = item.getCount();

            // Redis Hash Key
            String redisKey = RedisKeyConstants.buildCountNoteKey(noteId);
            // 判断是否存在
            Boolean isExisted = redisTemplate.hasKey(redisKey);
            // 若存在才会更新
            // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
            if (isExisted) {
                redisTemplate.opsForHash().increment(redisKey, RedisKeyConstants.FIELD_COLLECT_TOTAL, count);
            }
            
            // 更新用户维度的收藏数
            String countUserRedisKey = RedisKeyConstants.buildCountUserKey(creatorId);
            Boolean hasKey = redisTemplate.hasKey(countUserRedisKey);
            if (hasKey) {
                redisTemplate.opsForHash().increment(countUserRedisKey, RedisKeyConstants.FIELD_COLLECT_TOTAL, count);
            }
        });
        
        // 发送消息到 RocketMQ，通知其他服务更新缓存
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countList)).build();
        
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：笔记收藏数入库】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：笔记收藏数入库】MQ 发送异常: ", throwable);
            }
        });
    }

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }
}
