package com.kaiming.xiaohongshu.oss.biz.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.oss.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 15:38
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("OSS-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("OSS-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    ;
    // 异常码
    public final String errorCode;

    // 错误信息
    private final String errorMessage;
}
