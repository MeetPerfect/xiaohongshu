package com.kaiming.xiaohongshu.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: NoteLikeLuaResultEnum
 * Package: com.kaiming.xiaohongshu.note.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/5 20:17
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteLikeLuaResultEnum {
    
//    BLOOM_NOT_EXIST(-1L),
    NOT_EXIST(-1L),
    // 笔记点赞成功
    NOTE_LIKE_SUCCESS(0L),
    NOTE_LIKED(1L),
    
    ;
    
    private final Long code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static NoteLikeLuaResultEnum valueOf(Long code) {
        for (NoteLikeLuaResultEnum noteLikeLuaResultEnum : NoteLikeLuaResultEnum.values()) {
            if (Objects.equals(code, noteLikeLuaResultEnum.getCode())) {
                return noteLikeLuaResultEnum;
            }
        }
        return null;
    }
}
