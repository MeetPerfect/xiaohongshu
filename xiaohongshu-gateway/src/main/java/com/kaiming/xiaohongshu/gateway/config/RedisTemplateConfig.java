package com.kaiming.xiaohongshu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * ClassName: RedisTemplateConfig
 * Package: com.kaiming.xiaohongshu.gateway.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/11 21:25
 * @Version 1.0
 */
@Configuration
public class RedisTemplateConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置连接工厂
        redisTemplate.setConnectionFactory(connectionFactory);
        
        // 设置序列化和反序列化redis的key, 确保key是可读的
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 使用 Jackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值, 确保存储的是 JSON 格式
        Jackson2JsonRedisSerializer<Object> serializer = new  Jackson2JsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
