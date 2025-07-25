package com.kaiming.xiaohongshu.count.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;

import java.util.List;

/**
 * ClassName: NoteCountService
 * Package: com.kaiming.xiaohongshu.count.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 19:44
 * @Version 1.0
 */
public interface NoteCountService {

    /**
     * 批量查询笔记计数
     * @param findNoteCountByIdReqDTO
     * @return
     */
    Response<List<FindNoteCountByIdRespDTO>> findNoteCountData(FindNoteCountByIdReqDTO findNoteCountByIdReqDTO);
}
