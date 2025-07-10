package com.kaiming.xiaohongshu.auth.exception;

import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.enums.ResponseCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * ClassName: GlobalExceptionHandler
 * Package: com.kaiming.xiaohongshu.auth.exception
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/3 17:20
 * @Version 1.0
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获自定义业务异常
     *
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({BizException.class})
    @ResponseBody
    public Response<Object> handlerBizException(HttpServletRequest request, BizException e) {
        log.warn("{} request fail, errorCode: {}, errorMessage: {}", request.getRequestURI(), e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e);
    }

    /**
     * 捕获参数校验异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public Response<Object> handlerMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
        // 参数错误异常码
        String errorCode = ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode();
        // 获取 BindingResult
        BindingResult bindingResult = e.getBindingResult();

        StringBuilder sb = new StringBuilder();
        // 获取校验不通过的字段，并组合错误信息，格式为： email 邮箱格式不正确, 当前值: '123124qq.com';
        Optional.ofNullable(bindingResult.getFieldErrors()).ifPresent(errors -> {
            errors.forEach(error ->
                    sb.append(error.getField())
                            .append(" ")
                            .append(error.getDefaultMessage())
                            .append(", 当前值: '")
                            .append(error.getRejectedValue())
                            .append("'; ")
            );
        });
        
        // 错误信息
        String errorMessage = sb.toString();
        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);

        return Response.fail(errorCode, errorMessage);
    }
    
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    public Response<Object> handlerIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        // 参数错误异常码
        String errorCode = ResponseCodeEnum.PARAM_NOT_VALID.getErrorCode();
        
        // 错误信息
        String errorMessage = e.getMessage();
        log.warn("{} request error, errorCode: {}, errorMessage: {}", request.getRequestURI(), errorCode, errorMessage);
        
        return Response.fail(errorCode, errorMessage);
    }

    /**
     * 捕获其他异常
     * @param request
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public Response<Object> handlerOtherException(HttpServletRequest request, Exception e) {
        log.error("{} request error, errorMessage: {}", request.getRequestURI(), e.getMessage());
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR);
    }

}
