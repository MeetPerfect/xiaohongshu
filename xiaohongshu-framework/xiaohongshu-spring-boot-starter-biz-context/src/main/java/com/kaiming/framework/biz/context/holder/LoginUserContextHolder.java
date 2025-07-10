package com.kaiming.framework.biz.context.holder;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: LoginUserContextHolder
 * Package: com.kaiming.framework.biz.context.holder
 * Description:登录用户上下文
 *
 * @Auther gongkaiming
 * @Create 2025/5/18 22:44
 * @Version 1.0
 */
public class LoginUserContextHolder {
    
    private static final ThreadLocal<Map<String, Object>> LOGIN_USER_CONTEXT_THREAD_LOCAL 
            = TransmittableThreadLocal.withInitial(HashMap::new);

    /**
     * 设置用户Id
     * @param userId
     */
    public static void setUserId(String userId) {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.get().put("userId", userId);
    }

    /**
     * 获取用户Id
     * @return
     */
    public static Long getUserId() {
        Object value = LOGIN_USER_CONTEXT_THREAD_LOCAL.get().get("userId");
        if (Objects.isNull(value)) {
            return null;
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 删除 ThreadLocal
     */
    public static void remove() {
        LOGIN_USER_CONTEXT_THREAD_LOCAL.remove();
    }
}
