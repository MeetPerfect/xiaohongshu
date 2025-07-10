package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: CollectUncollectNoteTypeEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 14:44
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CollectUnCollectNoteTypeEnum {
    
    // 收藏
    COLLECT(1),
    // 取消收藏
    UN_COLLECT(0),
    ;
    
    
    private final Integer code;
}
