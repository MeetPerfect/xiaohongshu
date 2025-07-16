package com.kaiming.framework.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * ClassName: NumberUtils
 * Package: com.kaiming.framework.common.util
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 14:04
 * @Version 1.0
 */
public class NumberUtils {

    /**
     * 数字转字符串
     * @param number
     * @return
     */
    public static String formatNumberString(long number) {
        if(number < 10000) {
            return String.valueOf(number);
        } else if (number < 100000000) {
            // 小于 1 亿，显示万单位
            double result = number / 10000.0;
            DecimalFormat decimalFormat = new DecimalFormat("#.#"); // 保留 1 位小数
            decimalFormat.setRoundingMode(RoundingMode.DOWN);
            String formatted = decimalFormat.format(result);
            return formatted + "万";
        } else {
            return "9999万";
        }
    }

//    public static void main(String[] args) {
//        // 测试
//        System.out.println(formatNumberString(1000));         // 1000
//        System.out.println(formatNumberString(11130));        // 1.1万
//        System.out.println(formatNumberString(26719300));     // 2671.9万
//        System.out.println(formatNumberString(10000000));    // 1000万
//        System.out.println(formatNumberString(999999));       // 99.9万
//        System.out.println(formatNumberString(150000000));    // 超过一亿，展示9999万
//        System.out.println(formatNumberString(99999));        // 9.9万
//    }
}
