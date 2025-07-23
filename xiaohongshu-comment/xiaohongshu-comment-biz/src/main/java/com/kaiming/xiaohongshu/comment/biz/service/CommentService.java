package com.kaiming.xiaohongshu.comment.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.comment.biz.model.vo.*;

/**
 * ClassName: CommentService
 * Package: com.kaiming.xiaohongshu.comment.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:36
 * @Version 1.0
 */
public interface CommentService {

    /**
     * 发布评论
     * @param publishCommentReqVO
     * @return
     */
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);

    /**
     * 评论内容分页查询
     * @param findCommentPageListReqVO
     * @return
     */
    PageResponse<FindCommentItemRespVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);

    /**
     * 二级评论内容分页查询
     * @param findChildCommentPageListReqVO
     * @return
     */
    PageResponse<FindChildCommentItemRespVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO);

    /**
     * 点赞评论
     * @param likeCommentReqVO
     * @return
     */
    Response<?> LikeComment(LikeCommentReqVO likeCommentReqVO);

    /**
     * 取消点赞评论
     * @param unlikeCommentReqVO
     * @return
     */
    Response<?> UnlikeComment(UnlikeCommentReqVO unlikeCommentReqVO);

    /**
     * 删除评论
     * @param deleteCommentReqVO
     * @return
     */
    Response<?> deleteComment(DeleteCommentReqVO deleteCommentReqVO);

    /**
     * 删除本地评论缓存
     * @param commentId
     */
    void deleteCommentLocalCache(Long commentId);
}
