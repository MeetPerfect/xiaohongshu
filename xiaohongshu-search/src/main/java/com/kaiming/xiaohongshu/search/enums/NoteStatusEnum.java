package com.kaiming.xiaohongshu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: NoteStatusEnum
 * Package: com.kaiming.xiaohongshu.search.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 14:56
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteStatusEnum  {

    BE_EXAMINE(0), // 待审核
    NORMAL(1), // 正常展示
    DELETED(2), // 被删除
    DOWNED(3), // 被下架
    ;
    
    private final Integer code;
}
