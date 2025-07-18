package com.kaiming.xiaohongshu.comment.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: PublishCommentReqVO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:35
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishCommentReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片链接
     */
    private String imageUrl;

    /**
     * 回复的哪个评论（评论 ID）
     */
    private Long replyCommentId;
}
