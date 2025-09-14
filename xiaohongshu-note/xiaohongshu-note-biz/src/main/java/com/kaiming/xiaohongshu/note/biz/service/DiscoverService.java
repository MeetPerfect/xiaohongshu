package com.kaiming.xiaohongshu.note.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNoteRespVO;
import org.springframework.data.domain.Page;

/**
 * ClassName: DiscoverService
 * Package: com.kaiming.xiaohongshu.note.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 15:28
 * @Version 1.0
 */
public interface DiscoverService {

    /**
     * 根据频道Id分页查询
     * @param findDiscoverNotePageListReqVO
     * @return
     */
    PageResponse<FindDiscoverNoteRespVO> findNoteList(FindDiscoverNotePageListReqVO findDiscoverNotePageListReqVO);
}
