package com.kaiming.xiaohongshu.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ClassName: XiaohongshuNoteBizApplication
 * Package: com.kaiming.xiaohongshu.note.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/8 22:38
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.note.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuNoteBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuNoteBizApplication.class, args);
    }
}
