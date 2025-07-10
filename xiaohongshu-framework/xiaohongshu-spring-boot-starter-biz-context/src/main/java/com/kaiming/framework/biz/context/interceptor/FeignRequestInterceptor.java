package com.kaiming.framework.biz.context.interceptor;

import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.constant.GlobalConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * ClassName: FeignRequestInterceptor
 * Package: com.kaiming.framework.biz.context.interceptor
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 15:00
 * @Version 1.0
 */
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 获取当前上下文的用户Id
        Long userId = LoginUserContextHolder.getUserId();
        // 若不为空，则添加到请求头中
        if (Objects.nonNull(userId)) {
            requestTemplate.header(GlobalConstants.USER_ID, String.valueOf(userId));
            log.info("FeignRequestInterceptor: 设置请求头 UserId: {}", userId);
        }
    }
}
