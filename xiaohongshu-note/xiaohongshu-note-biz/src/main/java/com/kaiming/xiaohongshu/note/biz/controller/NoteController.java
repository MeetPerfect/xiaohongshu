package com.kaiming.xiaohongshu.note.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;
import com.kaiming.xiaohongshu.note.biz.model.vo.*;
import com.kaiming.xiaohongshu.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: NoteController
 * Package: com.kaiming.xiaohongshu.note.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/14 10:54
 * @Version 1.0
 */
@RestController
@RequestMapping("/note")
@Slf4j
public class NoteController {

    @Resource
    private NoteService noteService;

    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "发布笔记")
    public Response<?> publishNote(@RequestBody PublishNoteReqVO publishNoteReqVO) {
        return noteService.publishNote(publishNoteReqVO);
    }

    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "查询笔记详情")
    public Response<FindNoteDetailRespVO> findNoteDetail(@RequestBody FindNoteDetailReqVO findNoteDetailReqVO) {
        return noteService.findNoteDetail(findNoteDetailReqVO);
    }
    
    @PostMapping(value = "/update")
    @ApiOperationLog(description = "更新笔记")
    public Response<?> updateNote(@RequestBody UpdateNoteReqVO updateNoteReqVO) {
        return noteService.updateNote(updateNoteReqVO);
    }
    
    @PostMapping("/delete")
    @ApiOperationLog(description = "删除笔记")
    public Response<?> deleteNote(@RequestBody DeleteNoteReqVO deleteNoteReqVO) {
        return noteService.deleteNote(deleteNoteReqVO);
    }
    
    @PostMapping("/visible/onlyme")
    @ApiOperationLog(description = "更新笔记可见性为仅自己可见")
    public Response<?> visibleOnlyMe(@RequestBody UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO) {
        return noteService.visibleOnlyMe(updateNoteVisibleOnlyMeReqVO);
    }
    
    @PostMapping("/top")
    @ApiOperationLog(description = "置顶笔记")
    public Response<?> topNote(@RequestBody TopNoteReqVO topNoteReqVO) {
        return noteService.topNote(topNoteReqVO);
    }
    
    @PostMapping("/like")
    @ApiOperationLog(description = "点赞笔记")
    public Response<?> noteLike(@RequestBody LikeNoteReqVO likeNoteReqVO) {
        return noteService.likeNote(likeNoteReqVO);
    }
    
    @PostMapping("/unlike")
    @ApiOperationLog(description = "取消点赞笔记")
    public Response<?> unlike(@RequestBody UnlikeNoteReqVO unlikeNoteReqVO) {
        return noteService.unlikeNote(unlikeNoteReqVO);
    }
    @PostMapping("/collect")
    @ApiOperationLog(description = "收藏笔记")
    public Response<?> collectNote(@RequestBody CollectNoteReqVO collectNoteReqVO) {
        return noteService.collectNote(collectNoteReqVO);
    }
    @PostMapping("/uncollect")
    @ApiOperationLog(description = "取消收藏笔记")
    public Response<?> unCollectNote(@RequestBody UnCollectNoteReqVO unCollectNoteReqVO) {
        return noteService.unCollectNote(unCollectNoteReqVO);
    }
}
