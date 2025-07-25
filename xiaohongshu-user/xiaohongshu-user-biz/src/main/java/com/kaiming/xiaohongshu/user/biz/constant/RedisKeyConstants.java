package com.kaiming.xiaohongshu.user.biz.constant;

/**
 * ClassName: RedisKeyConstants
 * Package: com.kaiming.xiaohongshu.user.biz.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 16:06
 * @Version 1.0
 */
public class RedisKeyConstants {
    
    // 小红书全局ID生成器key
    public static final String XIAOHONGSHU_ID_GENERATOR_KEY = "xiaohongshu.id.generator";
    // 用户角色数据KEY前缀
    private static final String USER_ROLES_KEY_PREFIX = "user:roles:";
    // 角色对应的权限集合 KEY 前缀
    private static final String ROLE_PERMISSIONS_KEY_PREFIX = "role:permissions:";

    /**
     * 用户信息数据 KEY 前缀
     */
    private static final String USER_INFO_KEY_PREFIX = "user:info:";

    /**
     * 用户主页信息数据 KEY 前缀
     */
    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";
    
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
    
    public static String buildUserInfoKey(Long userId) {
        return USER_INFO_KEY_PREFIX + userId;
    }

    /**
     * 构建角色主页信息对应的 KEY
     * @param userId
     * @return
     */
    public static String buildUserProfileKey(Long userId) {
        return USER_PROFILE_KEY_PREFIX + userId;
    }
}
