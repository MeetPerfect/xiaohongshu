package com.kaiming.xiaohongshu.auth.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.model.vo.user.UpdatePasswordReqVO;
import com.kaiming.xiaohongshu.auth.model.vo.user.UserLoginReqVO;

/**
 * ClassName: UserService
 * Package: com.kaiming.xiaohongshu.auth.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 22:10
 * @Version 1.0
 */
public interface AuthService {

    /**
     * 用户登录和注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 用户退出
     * @param 
     * @return
     */
    Response<?> logout();

    /**
     * 更新密码
     * @param updatePasswordReqVO
     * @return
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);
}
