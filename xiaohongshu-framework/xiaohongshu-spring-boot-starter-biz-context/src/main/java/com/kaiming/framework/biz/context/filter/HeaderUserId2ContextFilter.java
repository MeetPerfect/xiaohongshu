package com.kaiming.framework.biz.context.filter;

import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.constant.GlobalConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ClassName: HeaderUserId2ContextFilter
 * Package: com.kaiming.framework.biz.context.filter
 * Description:提取请求头中的用户 ID 保存到上下文中，以方便后续使用
 *
 * @Auther gongkaiming
 * @Create 2025/5/18 22:43
 * @Version 1.0
 */
@Slf4j
public class HeaderUserId2ContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 从请求头中获取用户 ID
        String userId = request.getHeader(GlobalConstants.USER_ID);
        
        // 判断请求头中是否包含用户 ID
        if (StringUtils.isBlank(userId)) {
            // 为空
            chain.doFilter(request, response);
            return;
        }

        log.info("===== 设置 userId 到 ThreadLocal 中， 用户 ID: {}", userId);
        LoginUserContextHolder.setUserId(userId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            // 一定要删除 ThreadLocal ，防止内存泄露
            LoginUserContextHolder.remove();
            log.info("===== 删除 ThreadLocal， userId: {}", userId);
        }
    }
}
