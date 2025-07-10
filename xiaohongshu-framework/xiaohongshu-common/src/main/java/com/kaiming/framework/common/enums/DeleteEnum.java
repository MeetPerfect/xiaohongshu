package com.kaiming.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: DeleteEnum
 * Package: com.kaiming.framework.common.enums
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 14:35
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum DeleteEnum {

    YES(true),
    NO(false);
    
    private final Boolean value;
}
