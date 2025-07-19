package com.kaiming.xiaohongshu.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: CommentLevelEnum
 * Package: com.kaiming.xiaohongshu.comment.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/19 13:56
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum CommentLevelEnum {

    // 一级评论
    ONE(1),
    // 二级评论
    TWO(2),
    ;

    private final Integer code;
}
