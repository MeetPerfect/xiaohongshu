package com.kaiming.xiaohongshu.gateway.auth;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: SaTokenConfigure
 * Package: com.kaiming.xiaohongshu.gateway.auth
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/11 17:09
 * @Version 1.0
 */
@Configuration
@Slf4j
public class SaTokenConfigure {
    
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .setAuth(obj -> {
                    log.info("==================> SaReactorFilter, Path: {}", SaHolder.getRequest().getRequestPath());
                    // 登录校验 -- 拦截所有路由
                    SaRouter.match("/**")
                            .notMatch("/auth/login")
                            .notMatch("/auth/verification/code/send")
                            .notMatch("/note/channel/list")
                            .notMatch("/note/discover/note/list")
                            .notMatch("/note/profile/note/list")
                            .notMatch("/note/note/detail")
                            .notMatch("/note/note/isLikedAndCollectedData")
                            .notMatch("/comment/comment/list")
                            .notMatch("/comment/comment/child/list")
                            .notMatch("/user/user/profile")
                            .notMatch("/search/search/note")
                            .check(r -> StpUtil.checkLogin());

                    // 权限校验
//                    SaRouter.match("/auth/logout", r -> StpUtil.checkPermission("app:note:publish"));
//                    SaRouter.match("/auth/user/logout", r -> StpUtil.checkRole("user"));
//                    SaRouter.match("/goods/**", r -> StpUtil.checkRole("goods"));
//                    SaRouter.match("/order/**", r -> StpUtil.checkRole("order"));
                    // 需要拦截的请求路径;
                }).setError(e -> {
                    // 异常处理方法：每次setAuth函数出现异常时进入
                    // 手动抛出
                    if (e instanceof NotLoginException) {
                        throw new NotLoginException(e.getMessage(), null, null);
                    } else if (e instanceof NotPermissionException || e instanceof NotRoleException) {
                        // 权限不足，或不具备角色，统一抛出权限不足异常
                        throw new NotPermissionException(e.getMessage());
                    } else {
                        throw new RuntimeException(e.getMessage());
                    }
                });
    }

}
