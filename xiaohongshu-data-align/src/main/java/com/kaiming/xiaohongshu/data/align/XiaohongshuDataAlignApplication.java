package com.kaiming.xiaohongshu.data.align;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ClassName: XiaohongshuDataAlignApplication
 * Package: com.kaiming.xiaohongshu.data.align.domain
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/11 22:01
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.data.align.domain.mapper")
@EnableFeignClients(basePackages = "com.kaiming.xiaohongshu")
public class XiaohongshuDataAlignApplication {

    
    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuDataAlignApplication.class, args);
    }
    
}
