package com.kaiming.xiaohongshu.comment.biz.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.comment.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 13:59
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("COMMENT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("COMMENT-10001", "参数错误"),

    COMMENT_NOT_FOUND("COMMENT-20001", "此评论不存在"),
    
    ;
    
    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;
}
