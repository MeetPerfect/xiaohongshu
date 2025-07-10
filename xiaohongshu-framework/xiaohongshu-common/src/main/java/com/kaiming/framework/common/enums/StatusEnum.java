package com.kaiming.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

/**
 * ClassName: StatusEnum
 * Package: com.kaiming.framework.common.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 14:36
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    // 启用
    ENABLE(0),
    // 禁用
    DISABLE(1);

    private final Integer value;
}
