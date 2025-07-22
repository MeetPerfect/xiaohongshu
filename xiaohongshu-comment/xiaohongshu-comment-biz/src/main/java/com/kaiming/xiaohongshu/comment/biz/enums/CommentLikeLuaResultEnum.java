package com.kaiming.xiaohongshu.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: CommentLikeLuaResultEnum
 * Package: com.kaiming.xiaohongshu.comment.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/22 18:39
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CommentLikeLuaResultEnum {

    // 不存在
    NOT_EXIST(-1L),
    // 评论已点赞
    COMMENT_LIKED(1L),
    // 评论点赞成功
    COMMENT_LIKE_SUCCESS(0L),
    ;
    
    private final Long code;
    
    public static CommentLikeLuaResultEnum valueOf(Long code) {
        for (CommentLikeLuaResultEnum commentLikeLuaResultEnum : CommentLikeLuaResultEnum.values()) {
            if (Objects.equals(commentLikeLuaResultEnum.getCode(), code)) {
                return commentLikeLuaResultEnum;
            }
        }
        return null;
    }
}
