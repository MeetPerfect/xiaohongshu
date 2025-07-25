package com.kaiming.xiaohongshu.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AggregationCountLikeUnlikeCommentMqDTO
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
public class AggregationCountLikeUnlikeCommentMqDTO {

    /**
     * 评论 ID
     */
    private Long commentId;

    /**
     * 聚合后的计数
     */
    private Integer count;
}
