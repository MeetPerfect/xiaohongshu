package com.kaiming.xiaohongshu.search.biz;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
* ClassName: XiaohongshuSearchBizApplication
* Package: com.kaiming.xiaohongshu.search.biz
* Description:
* @Auther gongkaiming
* @Create 2025/7/17 21:00
* @Version 1.0
*/
@SpringBootApplication
@EnableScheduling
@MapperScan("com.kaiming.xiaohongshu.search.biz.domain.mapper")
public class XiaohongshuSearchBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuSearchBizApplication.class, args);
    }
}
