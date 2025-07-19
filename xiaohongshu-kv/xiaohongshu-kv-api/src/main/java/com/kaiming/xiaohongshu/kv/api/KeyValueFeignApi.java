package com.kaiming.xiaohongshu.kv.api;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.constant.ApiConstants;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.BatchAddCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
