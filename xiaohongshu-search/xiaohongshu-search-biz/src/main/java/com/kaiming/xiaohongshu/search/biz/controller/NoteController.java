package com.kaiming.xiaohongshu.search.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.dto.RebuildNoteDocumentReqDTO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchNoteReqVO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchNoteRespVO;
import com.kaiming.xiaohongshu.search.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: NoteController
 * Package: com.kaiming.xiaohongshu.search.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 19:14
 * @Version 1.0
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class NoteController {

    @Resource
    private NoteService noteService;

    @PostMapping("/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteRespVO> searchNote(@RequestBody @Validated SearchNoteReqVO searchNoteReqVO) {
        return noteService.searchNote(searchNoteReqVO);
    }
    
    @PostMapping("/note/document/rebuild")
    @ApiOperationLog(description = "笔记文档重建")
    public Response<Long> rebuildDocument(@RequestBody RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO) {
        return noteService.rebuildDocument(rebuildNoteDocumentReqDTO);
    }

}