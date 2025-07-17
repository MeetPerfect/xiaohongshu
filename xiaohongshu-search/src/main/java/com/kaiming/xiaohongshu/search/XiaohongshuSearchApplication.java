package com.kaiming.xiaohongshu.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ClassName: XiaohongshuSearchApplication
 * Package: com.kaiming.xiaohongshu.search
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 10:53
 * @Version 1.0
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.kaiming.xiaohongshu.search.domain.mapper")
public class XiaohongshuSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuSearchApplication.class, args);
    }
}
