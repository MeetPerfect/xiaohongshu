package com.kaiming.xiaohongshu.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.UserCountDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ClassName: CountFans2DBConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/4 11:54
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_FANS_2_DB,
        topic = MQConstants.TOPIC_COUNT_FANS_2_DB)
@Slf4j
public class CountFans2DBConsumer implements RocketMQListener<String> {
    @Resource
    private UserCountDOMapper userCountDOMapper;
    // 限流器，限制每秒最多处理 5000 条消息
    private RateLimiter rateLimiter = RateLimiter.create(5000);

    @Override
    public void onMessage(String body) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();
        log.info("## 消费到了 MQ 【计数: 粉丝数入库】, {}...", body);

        Map<Long, Integer> countMap = null;
        try {
            countMap = JsonUtils.parseMap(body, Long.class, Integer.class);
        } catch (Exception e) {
            log.error("## 解析 JSON 字符串异常", e);
        }

        if (CollUtil.isNotEmpty(countMap)) {
            // 判断数据库中，若目标用户的记录不存在，则插入；若记录已存在，则直接更新
            countMap.forEach((k, v) -> userCountDOMapper.insertOrUpdateFansTotalByUserId(v, k));
        }
    }
}
