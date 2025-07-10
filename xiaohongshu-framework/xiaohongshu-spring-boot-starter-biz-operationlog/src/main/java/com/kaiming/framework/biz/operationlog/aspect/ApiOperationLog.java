package com.kaiming.framework.biz.operationlog.aspect;

import java.lang.annotation.*;

/**
 * ClassName: ApiOperationLog
 * Package: com.kaiming.framework.biz.operationlog.aspect
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/4/30 22:12
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface ApiOperationLog {
    
    String description() default "";
    
}
