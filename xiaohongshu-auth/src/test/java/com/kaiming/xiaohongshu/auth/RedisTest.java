package com.kaiming.xiaohongshu.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * ClassName: RedisTest
 * Package: com.kaiming.xiaohongshu.auth
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 15:32
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
public class RedisTest {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    public void test() {
        redisTemplate.opsForValue().set("name", "zhangsan");
    }
    
    @Test
    public void testHashKey() {
        log.info("key 是否存在: {}", Boolean.TRUE.equals(redisTemplate.hasKey("name")));    
    }
    
    @Test
    public void testGetKey() {
        log.info("key: {}", redisTemplate.opsForValue().get("name"));
    }
    
    @Test
    public void testDeleteKey() {
        redisTemplate.delete("name");
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
