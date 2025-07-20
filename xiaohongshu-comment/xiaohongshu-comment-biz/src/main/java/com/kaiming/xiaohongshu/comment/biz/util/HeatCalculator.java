package com.kaiming.xiaohongshu.comment.biz.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ClassName: HeatCalculator
 * Package: com.kaiming.xiaohongshu.comment.biz.util
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/19 20:10
 * @Version 1.0
 */
public class HeatCalculator {

    // 热度计算的权重配置
    private static final double LIKE_WEIGHT = 0.7;  // 点赞权重 70%
    private static final double REPLY_WEIGHT = 0.3; // 回复权重 30%
    
    public static BigDecimal calculateHeat(long likeCount, long replyCount) {
        // 点赞数权重 70%，被回复数权重 30%
        BigDecimal likeWeight = new BigDecimal(LIKE_WEIGHT);
        BigDecimal replyWeight = new BigDecimal(REPLY_WEIGHT);

        // 转换点赞数和回复数为 BigDecimal
        BigDecimal likeCountBD = new BigDecimal(likeCount);
        BigDecimal replyCountBD = new BigDecimal(replyCount);

        // 计算热度 (点赞数*点赞权重 + 回复数*回复权重)
        BigDecimal heat = likeCountBD.multiply(likeWeight).add(replyCountBD.multiply(replyWeight));
        // 四舍五入保留两位小数
        return heat.setScale(2, RoundingMode.HALF_UP);
    }

//    public static void main(String[] args) {
//        int likeCount = 150;    // 点赞数
//        int replyCount = 10;    // 被回复数
//
//        // 计算热度
//        BigDecimal heat = calculateHeat(likeCount, replyCount);
//
//        // 输出热度值
//        System.out.println("Calculated Heat: " + heat);
//    }
}
