package com.kaiming.xiaohongshu.kv.api;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.constant.ApiConstants;
import com.kaiming.xiaohongshu.kv.dto.req.*;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: KeyValueFeignApi
 * Package: com.kaiming.xiaohongshu.kv.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 21:02
 * @Version 1.0
 */
@FeignClient(name = ApiConstants.SERVER_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";
    
    @PostMapping(PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);
    
    @PostMapping(PREFIX + "/note/content/find")
    Response<FindNoteContentRespDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);
    
    @PostMapping(PREFIX + "/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
    
    @PostMapping(PREFIX + "/comment/content/batchAdd")
    Response<?> batchAddCommentContent(@RequestBody BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);
    
    @PostMapping(PREFIX + "/comment/content/batchFind")
    Response<List<FindCommentContentRespDTO>> batchFindCommentContent(@RequestBody BatchFindCommentContentReqDTO  batchFindCommentContentReqDTO);
}
