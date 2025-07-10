package com.kaiming.xiaohongshu.user.biz.enums;

import com.kaiming.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * ClassName: SexEnum
 * Package: com.kaiming.xiaohongshu.user.biz.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 21:53
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum SexEnum {

    WOMAN(0),
    MAN(1),
    ;

    private final Integer value;

    public static boolean isValid(Integer value) {
        for (SexEnum loginTypeEnum : SexEnum.values()) {
            if (Objects.equals(value, loginTypeEnum.getValue())) {
                return true;
            }
        }
        return false;
    }
}
