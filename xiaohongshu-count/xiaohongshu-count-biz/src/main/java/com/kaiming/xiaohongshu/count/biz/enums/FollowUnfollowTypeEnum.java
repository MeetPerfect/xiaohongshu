package com.kaiming.xiaohongshu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: FollowUnfollowTypeEnum
 * Package: com.kaiming.xiaohongshu.count.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/4 10:22
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    // 关注
    FOLLOW(1),
    // 取关
    UNFOLLOW(0),
    ;
    
    private final Integer code;
    
    public static FollowUnfollowTypeEnum valueOf(Integer code) {
        for (FollowUnfollowTypeEnum followUnfollowTypeEnum : FollowUnfollowTypeEnum.values()) {
            if(Objects.equals(code, followUnfollowTypeEnum.getCode())) {
                return followUnfollowTypeEnum;
            }
        }
        return null;
    }
}
