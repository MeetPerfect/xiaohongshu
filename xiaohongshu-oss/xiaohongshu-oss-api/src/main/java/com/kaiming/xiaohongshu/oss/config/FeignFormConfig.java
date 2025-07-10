package com.kaiming.xiaohongshu.oss.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FeignFormConfig
 * Package: com.kaiming.xiaohongshu.oss.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 13:16
 * @Version 1.0
 */
@Configuration
public class FeignFormConfig {

    @Bean
    public Encoder feignFormEncoder() {
        //Feign 提供的一个编码器，用于处理表单提交。
        return new SpringFormEncoder();
    }
}
