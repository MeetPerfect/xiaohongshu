package com.kaiming.xiaohongshu.note.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNoteRespVO;
import com.kaiming.xiaohongshu.note.biz.service.ProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ProfileController
 * Package: com.kaiming.xiaohongshu.note.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 18:02
 * @Version 1.0
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {
    
    @Resource
    private ProfileService profileService;
    
    @PostMapping("/note/list")
    @ApiOperationLog(description = "个人主页-查询笔记列表")
    public PageResponse<FindProfileNoteRespVO> findNoteList(@RequestBody FindProfileNotePageListReqVO findProfileNotePageListReqVO) {
        return profileService.findNoteList(findProfileNotePageListReqVO);
    }
}
