package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

import java.util.Objects;

/**
 * ClassName: NoteUnCollectLuaResultEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 16:46
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteUnCollectLuaResultEnum {
    // 布隆过滤器不存在
    NOT_EXIST(-1L),
    // 笔记已收藏
    NOTE_COLLECTED(1L),
    // 笔记未收藏
    NOTE_NOT_COLLECTED(0L),
    ;


    private final Long code;

    /**
     * 根据类型 code 获取对应的枚举
     * @param code
     * @return
     */
    public static NoteUnCollectLuaResultEnum valueOf(Long code) {
        for (NoteUnCollectLuaResultEnum noteUnCollectLuaResultEnum : NoteUnCollectLuaResultEnum.values()) {
            if (Objects.equals(code, noteUnCollectLuaResultEnum.getCode())) {
                return noteUnCollectLuaResultEnum;
            }
        }
        return null;
    }

}
