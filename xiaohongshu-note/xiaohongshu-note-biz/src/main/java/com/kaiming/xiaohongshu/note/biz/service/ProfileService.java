package com.kaiming.xiaohongshu.note.biz.service;

import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNoteRespVO;

/**
 * ClassName: ProfileService
 * Package: com.kaiming.xiaohongshu.note.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 16:39
 * @Version 1.0
 */
public interface ProfileService {
    
    PageResponse<FindProfileNoteRespVO> findNoteList(FindProfileNotePageListReqVO findProfileNotePageListReqVO);
}
