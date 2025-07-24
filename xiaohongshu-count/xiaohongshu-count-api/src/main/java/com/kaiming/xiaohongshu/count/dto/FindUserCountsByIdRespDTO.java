package com.kaiming.xiaohongshu.count.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserCountsByIdRespDTO
 * Package: com.kaiming.xiaohongshu.count.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 19:57
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserCountsByIdRespDTO {
    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 粉丝数
     */
    private Long fansTotal;

    /**
     * 关注数
     */
    private Long followingTotal;

    /**
     * 当前发布笔记数
     */
    private Long noteTotal;

    /**
     * 当前获得点赞数
     */
    private Long likeTotal;

    /**
     * 当前获得收藏数
     */
    private Long collectTotal;
}
