package com.kaiming.xiaohongshu.count.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.biz.service.NoteCountService;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: NoteCountController
 * Package: com.kaiming.xiaohongshu.count.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 20:04
 * @Version 1.0
 */
@RestController
@RequestMapping("count")
@Slf4j
public class NoteCountController {
    
    @Resource
    private NoteCountService noteCountService;
    
    @PostMapping("/note/data")
    @ApiOperationLog(description = "批量查询笔记计数数据")
    public Response<List<FindNoteCountByIdRespDTO>> findNotesCountData(@RequestBody FindNoteCountByIdReqDTO findNoteCountByIdReqDTO){
        return noteCountService.findNoteCountData(findNoteCountByIdReqDTO);
    }
}
