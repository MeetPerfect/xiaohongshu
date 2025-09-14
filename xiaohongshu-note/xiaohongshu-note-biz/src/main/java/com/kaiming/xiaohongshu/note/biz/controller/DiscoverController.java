package com.kaiming.xiaohongshu.note.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNoteRespVO;
import com.kaiming.xiaohongshu.note.biz.service.DiscoverService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: DiscoverController
 * Package: com.kaiming.xiaohongshu.note.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 15:24
 * @Version 1.0
 */
@RestController
@RequestMapping("/discover")
public class DiscoverController {
    
    @Resource
    private DiscoverService discoverService;
    
    @PostMapping("/note/list")
    @ApiOperationLog(description = "发现页-查询笔记列表")
    public PageResponse<FindDiscoverNoteRespVO> findNoteList(@RequestBody FindDiscoverNotePageListReqVO findDiscoverNotePageListReqVO){
        return discoverService.findNoteList(findDiscoverNotePageListReqVO);
    }
}
