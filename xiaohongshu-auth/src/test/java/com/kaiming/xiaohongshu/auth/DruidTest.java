package com.kaiming.xiaohongshu.auth;

import com.alibaba.druid.filter.config.ConfigTools;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * ClassName: DruidTest
 * Package: com.kaiming.xiaohongshu.auth
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/1 21:15
 * @Version 1.0
 */
@SpringBootTest
@Slf4j  
public class DruidTest {
    
    @Test
    @SneakyThrows
    public void test() {
        String password = "123456";
        String[] arr = ConfigTools.genKeyPair(512);
        
        log.info("privateKey: {}", arr[0]);
        log.info("publicKey: {}", arr[1]);

        String encodePassword = ConfigTools.encrypt(arr[0], password);
        log.info("encodePassword: {}", encodePassword);
    }
    
}
