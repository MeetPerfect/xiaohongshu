package com.kaiming.xiaohongshu.search.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.search.dto.RebuildUserDocumentReqDTO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserReqVO;
import com.kaiming.xiaohongshu.search.biz.model.vo.SearchUserRespVO;
import com.kaiming.xiaohongshu.search.biz.service.UserService;
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
public class UserController {
    
    @Resource
    private UserService userService;
    
    @RequestMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserRespVO> searchUser(@RequestBody SearchUserReqVO searchUserReqVO) {
        return userService.searchUser(searchUserReqVO);
    }
    
    @PostMapping("/user/document/rebuild")
    @ApiOperationLog(description = "用户文档重建")
    public Response<Long> rebuildDocument(@RequestBody RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO) {
        return userService.RebuildDocument(rebuildUserDocumentReqDTO);
    }
}
