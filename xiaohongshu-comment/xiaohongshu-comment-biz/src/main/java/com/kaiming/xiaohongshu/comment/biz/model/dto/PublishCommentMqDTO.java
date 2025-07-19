package com.kaiming.xiaohongshu.comment.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: PublishCommentMqDTO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:46
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishCommentMqDTO {

    /**
     * 笔记 Id
     */
    private Long noteId;

    /**
     * 评论 Id
     */
    private Long commentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片链接
     */
    private String imageUrl;

    /**
     * 回复的哪个评论（评论 Id）
     */
    private Long replyCommentId;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 发布者 Id
     */
    private Long creatorId;
}
