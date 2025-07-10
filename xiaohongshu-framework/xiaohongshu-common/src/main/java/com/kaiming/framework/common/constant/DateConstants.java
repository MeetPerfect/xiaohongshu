package com.kaiming.framework.common.constant;

import java.time.format.DateTimeFormatter;

/**
 * ClassName: DateConstants
 * Package: com.kaiming.framework.common.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/1 10:31
 * @Version 1.0
 */
public interface DateConstants {

    DateTimeFormatter DATE_FORMAT_Y_M_D_H_M_S = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter DATE_FORMAT_Y_M_D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter DATE_FORMAT_H_M_S = DateTimeFormatter.ofPattern("HH:mm:ss");
    DateTimeFormatter DATE_FORMAT_Y_M = DateTimeFormatter.ofPattern("yyyy-MM");
}
