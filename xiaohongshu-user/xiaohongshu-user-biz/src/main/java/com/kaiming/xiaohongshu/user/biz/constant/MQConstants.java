package com.kaiming.xiaohongshu.user.biz.constant;

/**
 * ClassName: MQConstants
 * Package: com.kaiming.xiaohongshu.user.biz.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 16:03
 * @Version 1.0
 */
public interface MQConstants {
    /**
     * Topic 主题：延迟双删 Redis 用户缓存
     */
    String TOPIC_DELAY_DELETE_USER_REDIS_CACHE = "DelayDeleteUserRedisCacheTopic";
}
