package com.kaiming.xiaohongshu.search.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteReqVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchNoteRespVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchUserReqVO;
import com.kaiming.xiaohongshu.search.model.vo.SearchUserRespVO;
import com.kaiming.xiaohongshu.search.service.NoteService;
import com.kaiming.xiaohongshu.search.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: userController
 * Package: com.kaiming.xiaohongshu.search.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 13:21
 * @Version 1.0
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class userController {
    
    @Resource
    private UserService userService;
    @Resource
    private NoteService noteService;
    
    @RequestMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserRespVO> searchUser(@RequestBody SearchUserReqVO searchUserReqVO) {
        return userService.searchUser(searchUserReqVO);
    }
    
    @PostMapping("/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteRespVO> searchNote(@RequestBody SearchNoteReqVO searchNoteReqVO) {
        return noteService.searchNote(searchNoteReqVO);
    }
}
