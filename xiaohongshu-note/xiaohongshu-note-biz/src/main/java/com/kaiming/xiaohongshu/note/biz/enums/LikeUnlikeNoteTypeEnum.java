package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: LikeUnlikeNoteTypeEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/6 15:37
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
}
