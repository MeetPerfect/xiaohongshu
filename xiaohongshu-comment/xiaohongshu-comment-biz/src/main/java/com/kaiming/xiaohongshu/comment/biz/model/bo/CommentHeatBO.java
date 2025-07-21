package com.kaiming.xiaohongshu.comment.biz.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: CommentHeatBO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.bo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/19 20:04
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentHeatBO {

    /**
     * 评论 ID
     */
    private Long id;

    /**
     * 热度值
     */
    private Double heat;

    /**
     * 笔记 ID
     */
    private Long noteId;
}
