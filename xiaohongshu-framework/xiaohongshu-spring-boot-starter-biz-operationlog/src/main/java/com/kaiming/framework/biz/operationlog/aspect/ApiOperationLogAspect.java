package com.kaiming.framework.biz.operationlog.aspect;

import com.kaiming.framework.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClassName: ApiOperationLogAspect
 * Package: com.kaiming.framework.biz.operationlog.aspect
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/4/30 22:12
 * @Version 1.0
 */
@Aspect
@Slf4j
public class ApiOperationLogAspect {

    // 以自定义 @ApiOperationLog 注解为切点，凡是添加 @ApiOperationLog 的方法，都会执行环绕中的代码
    @Pointcut("@annotation(com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog)")
    public void apiOperationLog() {
    }


    @Around("apiOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 请求开始时间
        long startTime = System.currentTimeMillis();

        // 获取请求类和方法
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        // 转为字符串
        String argsJsonStr = Arrays.stream(args).map(toJsonStr()).collect(Collectors.joining(", "));

        // 功能描述信息
        String description = getApiOperationLogDescription(joinPoint);

        log.info("====== 请求开始: [{}], 入参: {}, 请求类: {}, 请求方法: {} =================================== ",
                description, argsJsonStr, className, methodName);

        // 执行切点方法
        Object result = joinPoint.proceed();

        // 执行耗时时间
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("====== 请求结束: [{}], 耗时: {}ms, 出参: {} =================================== ",
                description, executionTime, result);
        return result;
    }

    private String getApiOperationLogDescription(ProceedingJoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 获取被注解的方法
        Method method = signature.getMethod();
        // 从method中获取LogExecution 的注解
        ApiOperationLog apiOperationLog = method.getAnnotation(ApiOperationLog.class);
        // 获取description属性
        return apiOperationLog.description();
    }

    /**
     * 将对象转换为json字符串
     *
     * @return
     */
    public Function<Object, String> toJsonStr() {
        return JsonUtils::toJsonString;
    }
}
