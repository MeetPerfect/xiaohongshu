package com.kaiming.xiaohongshu.auth.constant;

/**
 * ClassName: RedisKeyConstants
 * Package: com.kaiming.xiaohongshu.auth.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 15:50
 * @Version 1.0
 */
public class RedisKeyConstants {

    // 验证码Redis Key前缀
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification_code:";
    // 小红书全局ID生成器key
    public static final String XIAOHONGSHU_ID_GENERATOR_KEY = "xiaohongshu.id.generator";
    // 用户角色数据KEY前缀
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";
    // 角色对应的权限集合 KEY 前缀
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    // 构建验证码的Redis Key
    public static String buildVerificationCodeKey(String phone) {
        return VERIFICATION_CODE_KEY_PREFIX + phone;
    }

    /**
     * 构建角色对应的权限集合 KEY
     * @param roleKey
     * @return
     */
    public static String buildRolePermissionsKey(String roleKey) {
        return ROLE_PERMISSIONS_KEY_PREFIX + roleKey;
    }

    /**
     * 用户对应的角色集合 KEY
     * @param userId
     * @return
     */
    public static String buildUserRoleKey(Long userId) {
        return USER_ROLES_KEY_PREFIX + userId;
    }

}
