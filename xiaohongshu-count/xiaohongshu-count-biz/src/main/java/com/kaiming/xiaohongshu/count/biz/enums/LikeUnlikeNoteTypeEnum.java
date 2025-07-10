package com.kaiming.xiaohongshu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: LikeUnlikeNoteTypeEnum
 * Package: com.kaiming.xiaohongshu.count.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/8 21:24
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum LikeUnlikeNoteTypeEnum {

    // 点赞
    LIKE(1),
    // 取消点赞
    UNLIKE(0),
    ;
    
    private final Integer code;

    public static LikeUnlikeNoteTypeEnum valueOf(Integer code) {
        for (LikeUnlikeNoteTypeEnum likeUnlikeNoteTypeEnum : LikeUnlikeNoteTypeEnum.values()) {
            if (Objects.equals(code, likeUnlikeNoteTypeEnum.getCode())) {
                return likeUnlikeNoteTypeEnum;
            }
        }
        return null;
    }
}
