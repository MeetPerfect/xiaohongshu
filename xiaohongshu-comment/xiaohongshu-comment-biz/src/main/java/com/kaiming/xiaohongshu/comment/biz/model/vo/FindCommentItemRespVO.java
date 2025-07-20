package com.kaiming.xiaohongshu.comment.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindCommentItemRespVO
 * Package: com.kaiming.xiaohongshu.comment.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 14:27
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentItemRespVO {

    /**
     * 评论 ID
     */
    private Long commentId;

    /**
     * 发布者用户 ID
     */
    private Long userId;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论内容
     */
    private String imageUrl;

    /**
     * 发布时间
     */
    private String createTime;

    /**
     * 被点赞数
     */
    private Long likeTotal;

    /**
     * 二级评论总数
     */
    private Long childCommentTotal;

    /**
     * 最早回复的评论
     */
    private FindCommentItemRespVO firstReplyComment;
}
