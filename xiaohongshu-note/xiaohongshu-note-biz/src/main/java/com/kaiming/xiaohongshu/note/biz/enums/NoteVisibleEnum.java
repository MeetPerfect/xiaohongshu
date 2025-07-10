package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: NoteVisibleEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 19:39
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteVisibleEnum {
    
    PUBLIC(0),
    PRIVATE(1),
    
    ;
    
    private final Integer code; // 可见性状态码
}
