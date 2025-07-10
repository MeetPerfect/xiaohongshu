package com.kaiming.framework.biz.context.config;

import com.kaiming.framework.biz.context.filter.HeaderUserId2ContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: ContextAutoConfiguration
 * Package: com.kaiming.framework.biz.context.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/18 22:43
 * @Version 1.0
 */
@AutoConfiguration
public class ContextAutoConfiguration {
    
    @Bean
    public FilterRegistrationBean<HeaderUserId2ContextFilter> filterFilterRegistrationBean() {
        HeaderUserId2ContextFilter filter = new HeaderUserId2ContextFilter();
        FilterRegistrationBean<HeaderUserId2ContextFilter> bean = new FilterRegistrationBean<>(filter);
        return bean;
    }
}
