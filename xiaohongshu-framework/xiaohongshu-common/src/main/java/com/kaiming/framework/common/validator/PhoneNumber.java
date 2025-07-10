package com.kaiming.framework.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * ClassName: PhoneNumber
 * Package: com.kaiming.framework.common.validator
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 20:28
 * @Version 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
    String message() default "手机号格式不正确， 请检查后重新输入！";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
}
