package com.kaiming.xiaohongshu.auth;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ClassName: ThreadLocalTest
 * Package: com.kaiming.xiaohongshu.auth
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/18 22:25
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
public class ThreadLocalTest {

    @Test
    public void test() {
        ThreadLocal<Long> threadLocal = new InheritableThreadLocal<>();

        Long userId = 1L;
        threadLocal.set(userId);
        System.out.println("主线程中的 userId: " + threadLocal.get());

        new Thread(() ->{
            System.out.println("子线程中的 userId: " + threadLocal.get()); 
        }).start();
    }
}
