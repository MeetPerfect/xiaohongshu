package com.kaiming.xiaohongshu.user.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.biz.model.vo.FindUserProfileReqVO;
import com.kaiming.xiaohongshu.user.biz.model.vo.FindUserProfileRespVO;
import com.kaiming.xiaohongshu.user.biz.model.vo.UpdateUserInfoReqVO;
import com.kaiming.xiaohongshu.user.biz.service.UserService;
import com.kaiming.xiaohongshu.user.dto.req.*;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: UserController
 * Package: com.kaiming.xiaohongshu.user.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 21:53
 * @Version 1.0
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    
    @Resource
    private UserService userService;

    /**
     * 用户信息修改
     * @param updateUserInfoReqVO
     * @return
     */
    @RequestMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        return userService.updateUserInfo(updateUserInfoReqVO);
    }

    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public Response<Long> register(@RequestBody RegisterUserReqDTO registerUserReqDTO) {
        return userService.register(registerUserReqDTO);
    }
    
    @PostMapping("findByPhone")
    @ApiOperationLog(description = "根据手机号查询用户信息")
    public Response<FindUserByPhoneRespDTO> findByPhone(@RequestBody FindUserByPhoneReqDTO findUserByPhoneReqDTO) {
        return userService.findByPhone(findUserByPhoneReqDTO);
    }

    /**
     * 修改用户密码
     * @param updateUserPasswordReqDTO
     * @return
     */
    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改用户密码")
    public Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        return userService.updateUserPassword(updateUserPasswordReqDTO);
    }

    /**
     * 根据用户Id查询用户信息
     * @param findUserByIdReqDTO
     * @return
     */
    @PostMapping("/findById")
    @ApiOperationLog(description = "根据用户Id查询用户信息")
    public Response<FindUserByIdRespDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO) {
        return userService.findById(findUserByIdReqDTO);
    }
    
    @PostMapping("/findByIds")
    @ApiOperationLog(description = "批量查询用户信息")
    public Response<List<FindUserByIdRespDTO>> findByIds(@RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        return userService.findByIds(findUsersByIdsReqDTO);
    }
    
    @PostMapping("/profile")
    @ApiOperationLog(description = "查询用户简介")
    public Response<FindUserProfileRespVO> findUserProfile(@RequestBody FindUserProfileReqVO findUserProfileReqVO) {
        return userService.findUserProfile(findUserProfileReqVO);
    }
}
