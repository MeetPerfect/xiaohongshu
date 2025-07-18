package com.kaiming.xiaohongshu.search.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.dto.RebuildNoteDocumentReqDTO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchNoteReqVO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchNoteRespVO;

/**
 * ClassName: NoteService
 * Package: com.kaiming.xiaohongshu.search.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 14:19
 * @Version 1.0
 */
public interface NoteService {

    /**
     * 搜索笔记
     * @param searchNoteReqVO
     * @return
     */
    PageResponse<SearchNoteRespVO> searchNote(SearchNoteReqVO searchNoteReqVO);

    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);
}
