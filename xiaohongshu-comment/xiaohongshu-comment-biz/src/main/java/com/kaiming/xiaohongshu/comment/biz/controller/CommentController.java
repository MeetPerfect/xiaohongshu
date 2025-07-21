package com.kaiming.xiaohongshu.comment.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.comment.biz.model.vo.*;
import com.kaiming.xiaohongshu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: CommentController
 * Package: com.kaiming.xiaohongshu.comment.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:55
 * @Version 1.0
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {
    
    @Resource
    private CommentService commentService;
    
    @PostMapping("/publish")
    @ApiOperationLog(description = "发布评论")
    public Response<?> publishComment(@RequestBody PublishCommentReqVO publishCommentReqVO) {
        return commentService.publishComment(publishCommentReqVO);
    }
    
    @PostMapping("/list")
    @ApiOperationLog(description = "分页查询")
    public PageResponse<FindCommentItemRespVO> findCommentPageList(@RequestBody FindCommentPageListReqVO findCommentPageListReqVO) {
        return commentService.findCommentPageList(findCommentPageListReqVO);
    }
    
    @PostMapping("/child/list")
    @ApiOperationLog(description = "二级评论分页查询")
    public PageResponse<FindChildCommentItemRespVO> findChildCommentPageList(@RequestBody FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        return commentService.findChildCommentPageList(findChildCommentPageListReqVO);
    }
}
