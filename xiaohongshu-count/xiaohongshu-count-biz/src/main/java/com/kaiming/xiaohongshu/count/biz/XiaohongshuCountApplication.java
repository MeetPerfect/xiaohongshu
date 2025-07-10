package com.kaiming.xiaohongshu.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ClassName: XiaohongshuCountApplication
 * Package: com.kaiming.xiaohongshu.count.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 20:41
 * @Version 1.0
 */
@SpringBootApplication
@MapperScan("com.kaiming.xiaohongshu.count.biz.domain.mapper")
public class XiaohongshuCountApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(XiaohongshuCountApplication.class, args);
    }
}
