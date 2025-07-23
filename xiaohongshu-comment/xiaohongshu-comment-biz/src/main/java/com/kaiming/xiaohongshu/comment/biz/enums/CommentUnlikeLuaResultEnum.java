package com.kaiming.xiaohongshu.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: CommentUnlikeLuaResultEnum
 * Package: com.kaiming.xiaohongshu.comment.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 10:50
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CommentUnlikeLuaResultEnum {

    // 布隆过滤器不存在
    NOT_EXIST(-1L),
    // 评论已点赞
    COMMENT_LIKED(1L),
    // 评论未点赞
    COMMENT_NOT_LIKED(0L),
    ;
    
    private final Long code;


    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static CommentUnlikeLuaResultEnum valueOf(Long code) {
        for (CommentUnlikeLuaResultEnum commentUnlikeLuaResultEnum : CommentUnlikeLuaResultEnum.values()) {
            if (Objects.equals(code, commentUnlikeLuaResultEnum.getCode())) {
                return commentUnlikeLuaResultEnum;
            }
        }
        return null;
    }
}
