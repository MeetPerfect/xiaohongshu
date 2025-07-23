package com.kaiming.xiaohongshu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: LikeUnlikeCommentTypeEnum
 * Package: com.kaiming.xiaohongshu.count.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 12:26
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum LikeUnlikeCommentTypeEnum {
    
    LIKE(1),
    UNLIKE(0),
    ;
    
    private final Integer code;

    public static LikeUnlikeCommentTypeEnum valueOf(Integer code) {
        for (LikeUnlikeCommentTypeEnum likeUnlikeCommentTypeEnum : LikeUnlikeCommentTypeEnum.values()) {
            if (Objects.equals(code, likeUnlikeCommentTypeEnum.getCode())) {
                return likeUnlikeCommentTypeEnum;
            }
        }
        return null;
    }
}
