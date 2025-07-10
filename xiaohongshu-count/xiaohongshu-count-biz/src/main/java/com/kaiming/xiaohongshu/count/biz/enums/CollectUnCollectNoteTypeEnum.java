package com.kaiming.xiaohongshu.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: CollectUnCollectNoteTypeEnum
 * Package: com.kaiming.xiaohongshu.count.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 19:34
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CollectUnCollectNoteTypeEnum {

    COLLECT(1),
    
    UN_COLLECT(0),
    ;

    private final Integer code;

    public static CollectUnCollectNoteTypeEnum valueOf(Integer code) {
        for (CollectUnCollectNoteTypeEnum collectUnCollectNoteTypeEnum : CollectUnCollectNoteTypeEnum.values()) {
            if (Objects.equals(code, collectUnCollectNoteTypeEnum.getCode())) {
                return collectUnCollectNoteTypeEnum;
            }
        }
        return null;
    }
}
