package com.kaiming.xiaohongshu.data.align.constant;

/**
 * ClassName: TableConstants
 * Package: com.kaiming.xiaohongshu.data.align.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 10:43
 * @Version 1.0
 */
public class TableConstants {

    /**
     * 表名中的分隔符
     */
    private static final String TABLE_NAME_SEPARATE = "_";

    /**
     * 拼接表名后缀
     * @param hashKey
     * @return
     */
    public static String buildTableNameSuffix(String date, long hashKey) {
        // 拼接完整的表名
        return date + TABLE_NAME_SEPARATE + hashKey;
    }
}
