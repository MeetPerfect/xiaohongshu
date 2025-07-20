package com.kaiming.xiaohongshu.kv.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.BatchFindCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;

/**
 * ClassName: CommentContentService
 * Package: com.kaiming.xiaohongshu.kv.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 22:02
 * @Version 1.0
 */
public interface CommentContentService {

    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);
}
