package com.kaiming.xiaohongshu.search.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: ResponseCodeEnum
 * Package: com.kaiming.xiaohongshu.search.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 11:47
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("SEARCH-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("SEARCH-10001", "参数错误"),
    
    ;
    private final String errorCode;
    private final String errorMessage;
}
