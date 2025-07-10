package com.kaiming.xiaohongshu.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuAuthApplication.class, args);
    }

}
