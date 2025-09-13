package com.kaiming.xiaohongshu.data.align.rpc;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.api.SearchFeignApi;
import com.kaiming.xiaohongshu.search.dto.RebuildNoteDocumentReqDTO;
import com.kaiming.xiaohongshu.search.dto.RebuildUserDocumentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * ClassName: SearchRpcService
 * Package: com.kaiming.xiaohongshu.data.align.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 10:32
 * @Version 1.0
 */
@Component
public class SearchRpcService {
    
    @Resource
    private SearchFeignApi searchFeignApi;

    /**
     * 重建笔记文档接口
     * @param noteId
     */
    public void rebuildNoteDocument(Long noteId) {
        RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO = RebuildNoteDocumentReqDTO.builder()
                .id(noteId)
                .build();
        searchFeignApi.rebuildNoteDocument(rebuildNoteDocumentReqDTO);
        
    }

    /**
     * 重建用户文档接口
     * @param userId
     */
    public void rebuildUserDocument(Long userId) {
        RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO = RebuildUserDocumentReqDTO.builder()
                .id(userId)
                .build();
        searchFeignApi.rebuildUserDocument(rebuildUserDocumentReqDTO);
    }
}
