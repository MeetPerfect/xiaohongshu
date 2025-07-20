package com.kaiming.xiaohongshu.kv.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.biz.service.CommentContentService;
import com.kaiming.xiaohongshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.BatchFindCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: CommentContentController
 * Package: com.kaiming.xiaohongshu.kv.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 22:14
 * @Version 1.0
 */
@RestController
@RequestMapping("/kv")
@Slf4j
public class CommentContentController {
    
    @Resource
    private CommentContentService commentContentService;
    
    @PostMapping("/comment/content/batchAdd")
    @ApiOperationLog(description = "批量存储评论内容")
    public Response<?> batchAddCommentContent(@RequestBody BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {
        return commentContentService.batchAddCommentContent(batchAddCommentContentReqDTO);
    }
    
    @PostMapping("/comment/content/batchFind")
    public Response<?> batchFindCommentContent(@RequestBody BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        return commentContentService.batchFindCommentContent(batchFindCommentContentReqDTO);
    }
}
