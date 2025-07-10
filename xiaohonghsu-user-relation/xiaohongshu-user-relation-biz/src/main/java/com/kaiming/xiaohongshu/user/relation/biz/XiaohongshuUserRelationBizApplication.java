package com.kaiming.xiaohongshu.user.relation.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ClassName: XiaohongshuUserRelat9848
 * 9999ionBizApplication
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/27 15:21
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.user.relation.biz.domain.mapper")
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuUserRelationBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuUserRelationBizApplication.class, args);
    }
}
