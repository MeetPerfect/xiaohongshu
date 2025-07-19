package com.kaiming.xiaohongshu.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/**
 * ClassName: XiaohongshuCommentBizApplication
 * Package: com.kaiming.xiaohongshu.comment.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 13:46
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.comment.biz.domain.mapper")
@EnableRetry
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuCommentBizApplication.class, args);
    }
}
