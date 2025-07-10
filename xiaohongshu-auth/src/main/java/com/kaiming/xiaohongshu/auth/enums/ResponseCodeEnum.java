package com.kaiming.xiaohongshu.auth.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.auth.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/3 17:20
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("AUTH-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("AUTH-10001", "参数错误"),
    
    // 业务异常状态码
    VERIFICATION_CODE_KEY_FREQUENTLY("AUTH-20000", "请求太频繁，请3分钟后再试"),
    VERIFICATION_CODE_ERROR("AUTH-20001", "验证码错误"),

    LOGIN_TYPE_ERROR("AUTH-20002", "登录类型错误"),
    USER_NOT_FOUND("AUTH-20003", "该用户不存在"),
    PHONE_OR_PASSWORD_ERROR("AUTH-20004", "手机号或密码错误"),

    LOGIN_FAIL("AUTH-20005", "登录失败"),
    ;
    
    
    private final String errorCode;
    private final String errorMessage;
    
}
