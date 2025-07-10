package com.kaiming.xiaohongshu.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: ThreadPoolConfig
 * Package: com.kaiming.xiaohongshu.auth.config
 * Description: 自定义线程池
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 17:12
 * @Version 1.0
 */
@Configuration
public class ThreadPoolConfig {
    
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        // 队列容量
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(30);
        
        executor.setThreadNamePrefix("AuthExecutor-");
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        return executor;
    }
}
