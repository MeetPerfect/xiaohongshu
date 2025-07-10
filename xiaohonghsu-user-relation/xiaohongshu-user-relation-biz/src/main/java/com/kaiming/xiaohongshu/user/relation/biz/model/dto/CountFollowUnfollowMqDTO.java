package com.kaiming.xiaohongshu.user.relation.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: CountFollowUnfollowMqDTO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 21:54
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountFollowUnfollowMqDTO {

    /**
     * 原用户
     */
    private Long userId;

    /**
     * 目标用户
     */
    private Long targetUserId;

    /**
     * 1:关注 0:取关
     */
    private Integer type;
}
