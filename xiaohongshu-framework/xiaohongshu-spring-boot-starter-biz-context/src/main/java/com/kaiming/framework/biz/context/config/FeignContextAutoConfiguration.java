package com.kaiming.framework.biz.context.config;

import com.kaiming.framework.biz.context.interceptor.FeignRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: FeignContextAutoConfiguration
 * Package: com.kaiming.framework.biz.context.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 15:05
 * @Version 1.0
 */
@AutoConfiguration 
public class FeignContextAutoConfiguration {
    
    @Bean
    public FeignRequestInterceptor feignRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
    
}
