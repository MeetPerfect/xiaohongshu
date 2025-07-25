package com.kaiming.framework.common.util;

import com.kaiming.framework.common.constant.DateConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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
     *
     * @param localDateTime
     * @return
     */
    public static long localDateTime2Timestamp(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    /**
     * LocalDateTime 转 字符串
     *
     * @param time
     * @return
     */
    public static String localDateTime2String(LocalDateTime time) {
        return time.format(DateConstants.DATE_FORMAT_Y_M_D_H_M_S);
    }

    public static String formatRelativeTime(LocalDateTime dateTime) {
        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算与当前时间的差距
        long daysDiff = ChronoUnit.DAYS.between(dateTime, now);
        long hoursDiff = ChronoUnit.HOURS.between(dateTime, now);
        long minuteDiff = ChronoUnit.MINUTES.between(dateTime, now);

        if (daysDiff < 1) {
            if (hoursDiff < 1) {
                return minuteDiff + "分钟前";
            } else {
                return hoursDiff + "小时前";
            }
        } else if (daysDiff == 1) {
            return "昨天" + dateTime.format(DateConstants.DATE_FORMAT_H_M);
        } else if (daysDiff < 7) {
            return daysDiff + "天前";
        } else if (dateTime.getYear() == now.getYear()) {
            return dateTime.format(DateConstants.DATE_FORMAT_M_D);
        } else {
            return dateTime.format(DateConstants.DATE_FORMAT_Y_M);
        }
    }

    /**
     * 计算年龄
     * @param birthday
     * @return
     */
    public static int calculateAge(LocalDate birthday) {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 计算出生日期到当前日期的 Period 对象
        Period period = Period.between(birthday, currentDate);
        // 返回完整的年份（即年龄）
        return period.getYears();
    }

//    public static void main(String[] args) {
//        // 测试示例
//        LocalDateTime dateTime1 = LocalDateTime.now().minusMinutes(10); // 10分钟前
//        LocalDateTime dateTime2 = LocalDateTime.now().minusHours(3); // 3小时前
//        LocalDateTime dateTime3 = LocalDateTime.now().minusDays(1).minusHours(5); // 昨天 20:12
//        LocalDateTime dateTime4 = LocalDateTime.now().minusDays(2); // 2天前
//        LocalDateTime dateTime5 = LocalDateTime.now().minusDays(10); // 11-06
//        LocalDateTime dateTime6 = LocalDateTime.of(2025, 12, 1, 12, 30, 0); // 2023-12-01
//
//        System.out.println(formatRelativeTime(dateTime1)); // 输出 "10分钟前"
//        System.out.println(formatRelativeTime(dateTime2)); // 输出 "3小时前"
//        System.out.println(formatRelativeTime(dateTime3)); // 输出 "昨天 20:12"
//        System.out.println(formatRelativeTime(dateTime4)); // 输出 "2天前"
//        System.out.println(formatRelativeTime(dateTime5)); // 输出 "07-06"
//        System.out.println(formatRelativeTime(dateTime6)); // 输出 "2025-12-01"
//    }
}
