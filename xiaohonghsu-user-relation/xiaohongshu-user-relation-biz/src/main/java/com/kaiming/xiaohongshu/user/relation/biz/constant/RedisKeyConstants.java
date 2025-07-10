package com.kaiming.xiaohongshu.user.relation.biz.constant;


/**
 * ClassName: RedisKeyConstant
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:45
 * @Version 1.0
 */
public class RedisKeyConstants {

    /**
     * 关注列表 KEY 前缀
     */
    private static final String USER_FOLLOWING_KEY_PREFIX = "following:";

    /**
     * 粉丝列表 KEY 前缀
     */
    private static final String USER_FANS_KEY_PREFIX = "fans:";

    /**
     * 构建关注列表完整的Key
     *
     * @param userId
     * @return
     */
    public static String buildUserFollowingKey(Long userId) {
        return USER_FOLLOWING_KEY_PREFIX + userId;
    }

    /**
     * 构建粉丝列表完整的Key
     * @param userId
     * @return
     */
    public static String buildUserFansKey(Long userId) {
        return USER_FANS_KEY_PREFIX + userId;
    }

}
