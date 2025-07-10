package com.kaiming.xiaohongshu.user.relation.biz.config;


import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FollowUnfollowMqConsumerRateLimitConfig
 * Package: com.kaiming.xiaohongshu.user.relation.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 18:38
 * @Version 1.0
 */
@Configuration
@RefreshScope
public class FollowUnfollowMqConsumerRateLimitConfig {
    @Value("${mq-consumer.follow-unfollow.rate-limit}")
    private double rateLimit;

    @Bean
    @RefreshScope
    public RateLimiter rateLimiter() {
        return RateLimiter.create(rateLimit);
    }
}
