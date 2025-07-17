package com.kaiming.xiaohongshu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: NoteVisibleEnum
 * Package: com.kaiming.xiaohongshu.search.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 14:58
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteVisibleEnum {

    PUBLIC(0), // 公开，所有人可见
    PRIVATE(1); // 仅自己可见
    
    private final Integer code;
}
