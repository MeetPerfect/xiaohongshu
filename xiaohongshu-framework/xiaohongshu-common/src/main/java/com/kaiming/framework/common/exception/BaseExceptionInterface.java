package com.kaiming.framework.common.exception;

/**
 * ClassName: BaseExceptionInterface
 * Package: com.kaiming.framework.common.exception
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/4/30 21:25
 * @Version 1.0
 */
public interface BaseExceptionInterface {
    
    // 获取异常码
    String getErrorCode();
    // 获取异常信息
    String getErrorMessage();
    
}
