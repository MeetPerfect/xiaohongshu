package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: ProfileNoteTypeEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 16:41
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ProfileNoteTypeEnum {

    ALL(1),
    COLLECTED(2),
    LIKED(3);

    private final Integer code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static ProfileNoteTypeEnum valueOf(Integer code) {
        for (ProfileNoteTypeEnum profileNoteTypeEnum : ProfileNoteTypeEnum.values()) {
            if (Objects.equals(profileNoteTypeEnum.code, code)) {
                return profileNoteTypeEnum;
            }
        }
        throw new IllegalArgumentException("错误的笔记列表查询类型");
    }
}
