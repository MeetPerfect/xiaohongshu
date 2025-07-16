package com.kaiming.xiaohongshu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: NotePublishTimeRangeEnum
 * Package: com.kaiming.xiaohongshu.search.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 16:47
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NotePublishTimeRangeEnum {
    // 一天内
    DAY(0),
    // 一周内
    WEEK(1),
    // 半年内
    HALF_YEAR(2),
    ;
    
    private final Integer code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static NotePublishTimeRangeEnum valueOf(Integer code) {
        for (NotePublishTimeRangeEnum notePublishTimeRangeEnum : NotePublishTimeRangeEnum.values()) {
            if (Objects.equals(code, notePublishTimeRangeEnum.getCode())) {
                return notePublishTimeRangeEnum;
            }
        }
        return null;
    }
}
