package com.kaiming.xiaohongshu.search.api;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.constant.ApiConstants;
import com.kaiming.xiaohongshu.search.dto.RebuildNoteDocumentReqDTO;
import com.kaiming.xiaohongshu.search.dto.RebuildUserDocumentReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ClassName: SearchFeignApi
 * Package: com.kaiming.xiaohongshu.search.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 22:30
 * @Version 1.0
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface SearchFeignApi {
    
    String PREFIX = "/search";

    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/note/document/rebuild")
    Response<?> rebuildNoteDocument(@RequestBody RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);

    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/user/document/rebuild")
    Response<?> rebuildUserDocument(@RequestBody RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);
}
