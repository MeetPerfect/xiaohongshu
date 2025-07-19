package com.kaiming.xiaohongshu.comment.biz.rpc;

import com.google.common.collect.Lists;
import com.kaiming.framework.common.constant.DateConstants;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentDO;
import com.kaiming.xiaohongshu.comment.biz.model.bo.CommentBO;
import com.kaiming.xiaohongshu.kv.api.KeyValueFeignApi;
import com.kaiming.xiaohongshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.CommentContentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: KeyValueRpcService
 * Package: com.kaiming.xiaohongshu.comment.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/19 14:31
 * @Version 1.0
 */
@Component
public class KeyValueRpcService {
    
    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 批量存储评论内容
     * @param commentBOS
     * @return
     */
    public boolean batchSaveCommentContent(List<CommentBO> commentBOS) {
        
        List<CommentContentReqDTO> comments = Lists.newArrayList();

        commentBOS.forEach(commentBO -> {
            CommentContentReqDTO commentContentReqDTO = CommentContentReqDTO.builder()
                    .noteId(commentBO.getNoteId())
                    .yearMonth(commentBO.getCreateTime().format(DateConstants.DATE_FORMAT_Y_M))
                    .contentId(commentBO.getContentUuid())
                    .content(commentBO.getContent())
                    .build();
            comments.add(commentContentReqDTO);
        });
        
        // 创建接口参数实体类
        BatchAddCommentContentReqDTO batchAddCommentContentReqDTO = BatchAddCommentContentReqDTO.builder()
                .comments(comments)
                .build();
        // 调用 KV 存储服务
        Response<?> response = keyValueFeignApi.batchAddCommentContent(batchAddCommentContentReqDTO);

        // 若返参中 success 为 false, 则主动抛出异常，以便调用层回滚事务
        if (!response.isSuccess()) {
            throw new RuntimeException("批量保存评论内容失败");
        }

        return true;
    }
}
