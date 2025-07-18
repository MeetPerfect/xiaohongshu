package com.kaiming.xiaohongshu.comment.biz.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * ClassName: RetryConfig
 * Package: com.kaiming.xiaohongshu.comment.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 19:33
 * @Version 1.0
 */
@Configuration
public class RetryConfig {
    
    @Resource
    private RetryProperties retryProperties;
    
    @Bean
    public RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 定义重试策略（最多重试 3 次）
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryProperties.getMaxAttempts());    // 最大重试次数

        // 定义间隔策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryProperties.getInitInterval());    // 初始间隔 2000ms
        backOffPolicy.setMultiplier(retryProperties.getMultiplier());           // 每次乘以 2
        
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }
}
