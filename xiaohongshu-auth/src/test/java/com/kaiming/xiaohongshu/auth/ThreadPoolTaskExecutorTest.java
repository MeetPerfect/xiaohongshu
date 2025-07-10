package com.kaiming.xiaohongshu.auth;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ClassName: ThreadPoolTaskExcutorTest
 * Package: com.kaiming.xiaohongshu.auth
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 17:22
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
public class ThreadPoolTaskExecutorTest {
    
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    public void test() {

        threadPoolTaskExecutor.submit(() -> log.info("异步线程测试，test"));

    }
}
