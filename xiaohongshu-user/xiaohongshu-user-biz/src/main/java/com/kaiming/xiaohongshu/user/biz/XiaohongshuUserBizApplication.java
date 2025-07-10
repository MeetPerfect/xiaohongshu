package com.kaiming.xiaohongshu.user.biz;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ClassName: XiaohongshuUserBizApplication
 * Package: com.kaiming.xiaohongshu.user.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 20:02
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.user.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuUserBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuUserBizApplication.class, args);
    }
}
