package com.kaiming.xiaohongshu.kv.biz.controller;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.biz.service.NoteContentService;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: NoteContentController
 * Package: com.kaiming.xiaohongshu.kv.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 18:30
 * @Version 1.0
 */
@RestController
@RequestMapping("/kv")
@Slf4j
public class NoteContentController {
    @Resource
    private NoteContentService noteContentService;
    /**
     * 添加笔记内容
     * @return
     */
    @PostMapping("/note/content/add")
    public Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO) {
        
        return noteContentService.addNoteContent(addNoteContentReqDTO);
    }
    @PostMapping("/note/content/find")
    public Response<FindNoteContentRespDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO) {
        return noteContentService.findNoteContent(findNoteContentReqDTO);
    }
    @PostMapping("/note/content/delete")
    public Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }
}
