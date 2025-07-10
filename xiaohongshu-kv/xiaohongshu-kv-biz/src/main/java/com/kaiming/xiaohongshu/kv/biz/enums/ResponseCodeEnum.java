package com.kaiming.xiaohongshu.kv.biz.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.kv.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 21:25
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("KV-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("KV-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    NOTE_CONTENT_NOT_FOUND("KV-20000", "该笔记内容不存在"),
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
