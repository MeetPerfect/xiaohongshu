package com.kaiming.xiaohongshu.auth.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.model.vo.user.UpdatePasswordReqVO;
import com.kaiming.xiaohongshu.auth.model.vo.user.UserLoginReqVO;
import com.kaiming.xiaohongshu.auth.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: UserController
 * Package: com.kaiming.xiaohongshu.auth.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 14:27
 * @Version 1.0
 */
@RestController
@Slf4j
public class AuthController {
    @Resource
    private AuthService userService;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录和注册")
    public Response<String> loginAndRegister(@RequestBody UserLoginReqVO userLoginReqVO) {
        return userService.loginAndRegister(userLoginReqVO);
    }
    
    @PostMapping("/logout")
    @ApiOperationLog(description = "用户退出")
    public Response<?> logout() {
        return userService.logout();
    }
    
    @PostMapping("/password/update")
    @ApiOperationLog(description = "更新密码")
    public Response<?> updatePassword(@RequestBody UpdatePasswordReqVO updatePasswordReqVO) {
        return userService.updatePassword(updatePasswordReqVO);
    }
  
    
}
