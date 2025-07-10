package com.kaiming.framework.biz.operationlog.config;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: ApiOperationLogAutoConfiguration
 * Package: com.kaiming.framework.biz.operationlog.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/1 10:15
 * @Version 1.0
 */
@AutoConfiguration
public class ApiOperationLogAutoConfiguration {
    
    @Bean
    public ApiOperationLogAspect apiOperationLogAspect() {
        return new ApiOperationLogAspect();
    }
}
