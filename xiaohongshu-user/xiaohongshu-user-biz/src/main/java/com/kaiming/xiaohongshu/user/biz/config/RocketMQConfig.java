package com.kaiming.xiaohongshu.user.biz.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ClassName: RocketMQConfig
 * Package: com.kaiming.xiaohongshu.user.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 15:57
 * @Version 1.0
 */
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
    
}
