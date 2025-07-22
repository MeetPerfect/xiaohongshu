package com.kaiming.xiaohongshu.comment.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: LikeUnlikeCommentMqDTO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/22 18:51
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeUnlikeCommentMqDTO {
    private Long userId;

    private Long commentId;

    /**
     * 0: 取消点赞， 1：点赞
     */
    private Integer type;

    private LocalDateTime createTime;
}
