package com.kaiming.xiaohongshu.search.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteReqVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteRespVO;

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
}
