package com.kaiming.xiaohongshu.search.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: NoteSortTypeEnum
 * Package: com.kaiming.xiaohongshu.search.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 16:14
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum NoteSortTypeEnum {
    // 最新
    LATEST(0),
    // 最新点赞
    MOST_LIKE(1),
    // 最多评论
    MOST_COMMENT(2),
    // 最多收藏
    MOST_COLLECT(3),
    ;

    private final Integer code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static NoteSortTypeEnum valueOf(Integer code) {
        for (NoteSortTypeEnum noteSortTypeEnum : NoteSortTypeEnum.values()) {
            if (Objects.equals(code, noteSortTypeEnum.getCode())) {
                return noteSortTypeEnum;
            }
        }
        return null;
    }
}
