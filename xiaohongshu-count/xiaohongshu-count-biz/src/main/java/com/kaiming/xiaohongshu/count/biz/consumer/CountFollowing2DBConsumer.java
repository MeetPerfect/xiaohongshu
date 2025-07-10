package com.kaiming.xiaohongshu.count.biz.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.UserCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.enums.FollowUnfollowTypeEnum;
import com.kaiming.xiaohongshu.count.biz.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName: CountFollowing2DBConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/4 20:11
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_FOLLOWING_2_DB,
        topic = MQConstants.TOPIC_COUNT_FOLLOWING_2_DB)
@Slf4j
public class CountFollowing2DBConsumer implements RocketMQListener<String> {
    @Resource
    private UserCountDOMapper userCountDOMapper;

    private RateLimiter rateLimiter = RateLimiter.create(5000);

    @Override
    public void onMessage(String body) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();

        log.info("## 消费到了 MQ 【计数: 关注数入库】, {}...", body);

        if (StringUtils.isBlank(body)) return;

        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class);

        // 操作类型
        Integer type = countFollowUnfollowMqDTO.getType();
        // 原用户 Id
        Long userId = countFollowUnfollowMqDTO.getUserId();

        // 关注数
        int count = Objects.equals(type, FollowUnfollowTypeEnum.FOLLOW.getCode()) ? 1 : -1;

        // 判断数据库中，若原用户的记录不存在，则插入；若记录已存在，则直接更新
        userCountDOMapper.insertOrUpdateFollowingTotalByUserId(count, userId);
    }
}
