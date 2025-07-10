package com.kaiming.xiaohongshu.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: LoginTypeEnum
 * Package: com.kaiming.xiaohongshu.auth.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 21:57
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum LoginTypeEnum {
    // 验证码
    VERIFICATION_CODE(1),
    // 密码
    PASSWORD(2);
    
    private final Integer value;
    
    public static LoginTypeEnum valueOf(Integer code) {
        for (LoginTypeEnum loginTypeEnum : LoginTypeEnum.values()) {
            if (Objects.equals(code, loginTypeEnum.getValue())) {
                return loginTypeEnum;
            }
        }
        return null;
    }
    
}
