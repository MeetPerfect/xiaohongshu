package com.kaiming.xiaohongshu.kv.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;

/**
 * ClassName: NoteContentService
 * Package: com.kaiming.xiaohongshu.kv.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 18:23
 * @Version 1.0
 */
public interface NoteContentService {

    /**
     * 新增笔记内容
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    /**
     * 查询笔记内容
     * @param findNoteContentReqDTO
     * @return
     */
    Response<FindNoteContentRespDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    /**
     * 删除笔记内容
     * @param deleteNoteContentReqDTO
     * @return
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}
