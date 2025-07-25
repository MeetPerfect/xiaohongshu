package com.kaiming.xiaohongshu.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: CountLikeUnlikeCommentMqDTO
 * Package: com.kaiming.xiaohongshu.count.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 12:25
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountLikeUnlikeCommentMqDTO {
    private Long userId;

    private Long commentId;

    /**
     * 0: 取消点赞， 1：点赞
     */
    private Integer type;
}
