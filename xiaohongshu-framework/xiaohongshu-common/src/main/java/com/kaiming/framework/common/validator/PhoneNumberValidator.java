package com.kaiming.framework.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ClassName: PhoneNumberValidator
 * Package: com.kaiming.framework.common.validator
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 20:28
 * @Version 1.0
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {


    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // 校验逻辑：正则表达式判断手机号是否为 11 位数字
        return phoneNumber != null && phoneNumber.matches("\\d{11}");
    }
}
