package com.kaiming.xiaohongshu.user.relation.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: FollowUnfollowTypeEnum
 * Package: com.kaiming.xiaohongshu.user.relation.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 21:55
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    // 关注
    FOLLOW(1),
    // 取关
    UNFOLLOW(0)
    ;
    
    private final Integer code;
}
