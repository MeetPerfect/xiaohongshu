package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: NoteTypeEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 19:39
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteTypeEnum {
    
    IMAGE_TEXT(0, "图文"),
    VIDEO(1, "视频"),
    
    ;
    private final Integer code;
    private final String description;

    /**
     * 类型是否有效
     * @param code
     * @return
     */
    private static boolean isValid(Integer code) {
        for (NoteTypeEnum noteTypeEnum : NoteTypeEnum.values()) {
            if (Objects.equals(code, noteTypeEnum.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据code获取NoteTypeEnum
     * @param code
     * @return
     */
    public static NoteTypeEnum valueOf(Integer code) {
        for (NoteTypeEnum noteTypeEnum : NoteTypeEnum.values()) {
            if (Objects.equals(code, noteTypeEnum.getCode())) {
                return noteTypeEnum;
            }
        }
        return null;
    }
}
