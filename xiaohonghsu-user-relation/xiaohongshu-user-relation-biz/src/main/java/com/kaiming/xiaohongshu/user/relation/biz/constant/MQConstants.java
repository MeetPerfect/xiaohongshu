package com.kaiming.xiaohongshu.user.relation.biz.constant;

/**
 * ClassName: MQConstants
 * Package: com.kaiming.xiaohongshu.user.relation.biz.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 17:02
 * @Version 1.0
 */
public interface MQConstants {
    
    String TOPIC_FOLLOW_OR_UNFOLLOW = "FollowUnfollowTopic";
    String TAG_FOLLOW = "Follow";
    String TAG_UNFOLLOW = "Unfollow";

    /**
     * Topic: 关注数计数
     */
    String TOPIC_COUNT_FOLLOWING = "CountFollowingTopic";

    /**
     * Topic: 粉丝数计数
     */
    String TOPIC_COUNT_FANS = "CountFansTopic";
}
