package com.kaiming.xiaohongshu.note.biz.config;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * ClassName: RocketMQConfig
 * Package: com.kaiming.xiaohongshu.note.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/26 21:43
 * @Version 1.0
 */
@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
    
    
}
