package com.kaiming.xiaohongshu.comment.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.comment.biz.model.vo.PublishCommentReqVO;

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
}
