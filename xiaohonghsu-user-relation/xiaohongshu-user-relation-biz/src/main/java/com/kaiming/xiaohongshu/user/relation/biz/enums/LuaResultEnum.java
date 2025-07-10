package com.kaiming.xiaohongshu.user.relation.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: LuaResultEnum
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 14:43
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum LuaResultEnum {
    // ZSET 不存在
    ZSET_NOT_EXIST(-1L),
    // 关注达到上限
    FOLLOW_LIMIT(-2L),
    // 已经关注该用户
    ALREADY_FOLLOWED(-3L),
    // 关注成功
    FOLLOW_SUCCESS(0L),
    // 为关注该用户
    NOT_FOLLOWED(-4L),
    ;
    
    private final Long code;
    
    public static LuaResultEnum valueOf(Long code) {
        for(LuaResultEnum luaResultEnum: LuaResultEnum.values()) {
            if (Objects.equals(code, luaResultEnum.getCode())) {
                return luaResultEnum;
            }
        }
        return null;
    }
}
