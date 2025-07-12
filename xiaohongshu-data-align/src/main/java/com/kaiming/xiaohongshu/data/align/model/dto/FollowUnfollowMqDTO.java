package com.kaiming.xiaohongshu.data.align.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FollowUnfollowMqDTO
 * Package: com.kaiming.xiaohongshu.data.align.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 19:58
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowUnfollowMqDTO {

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
