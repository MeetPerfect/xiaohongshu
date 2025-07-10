package com.kaiming.framework.common.response;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import com.kaiming.framework.common.exception.BizException;
import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: Response
 * Package: com.kaiming.framework.common.response
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/4/30 21:24
 * @Version 1.0
 */
@Data
public class Response<T> implements Serializable {

    private Boolean success = true;

    private String message;

    private String errorCode;

    private T data;

    // 成功响应
    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        return response;
    }

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    // 失败响应
    public static <T> Response<T> fail() {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        return response;
    }
    
    public static <T> Response<T> fail(String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }
    
    public static <T> Response<T> fail (String errorCode, String errorMessage) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(errorMessage);
        return response;
    }
    
    public static <T> Response<T> fail(BizException bizException) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(bizException.getErrorCode());
        response.setMessage(bizException.getErrorMessage());
        return response;
    }
    
    public static <T> Response<T> fail(BaseExceptionInterface baseExceptionInterface) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(baseExceptionInterface.getErrorCode());
        response.setMessage(baseExceptionInterface.getErrorMessage());
        return response;
    }

    public boolean isSuccess() {
        return success != null && success;
    }
}
