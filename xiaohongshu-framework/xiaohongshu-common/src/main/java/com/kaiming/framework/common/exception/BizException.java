package com.kaiming.framework.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * ClassName: BizException
 * Package: com.kaiming.framework.common.exception
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/4/30 21:25
 * @Version 1.0
 */
@Getter
@Setter
public class BizException extends RuntimeException {

    private String errorCode;

    private String errorMessage;
    public BizException(BaseExceptionInterface baseExceptionInterface) {
        this.errorCode = baseExceptionInterface.getErrorCode();
        this.errorMessage = baseExceptionInterface.getErrorMessage();
    }
}
