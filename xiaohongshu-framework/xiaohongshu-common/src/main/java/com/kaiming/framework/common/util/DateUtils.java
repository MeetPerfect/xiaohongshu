package com.kaiming.framework.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * ClassName: DateUtils
 * Package: com.kaiming.framework.common.util
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:47
 * @Version 1.0
 */
public class DateUtils {

    /**
     * LocalDateTime转时间戳
     * @param localDateTime
     * @return
     */
    public static long localDateTime2Timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    
}
