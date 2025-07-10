package com.kaiming.xiaohongshu.auth.rpc;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.api.UserFeignApi;
import com.kaiming.xiaohongshu.user.dto.req.FindUserByPhoneReqDTO;
import com.kaiming.xiaohongshu.user.dto.req.RegisterUserReqDTO;
import com.kaiming.xiaohongshu.user.dto.req.UpdateUserPasswordReqDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * ClassName: UserRpcService
 * Package: com.kaiming.xiaohongshu.auth.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 16:54
 * @Version 1.0
 */
@Component
public class UserRpcService {
    
    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 用户注册
     * @param phone
     * @return
     */
    public Long registerUser(String phone) {
        RegisterUserReqDTO registerUserReqDTO = new RegisterUserReqDTO();
        
        registerUserReqDTO.setPhone(phone);
        Response<Long> response = userFeignApi.registerUser(registerUserReqDTO);
        
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }

    /**
     * 根据手机号查询用户信息
     * @return
     */
    public FindUserByPhoneRespDTO findUserByPhoneRespDTO(String phone) {
        FindUserByPhoneReqDTO findUserByPhoneReqDTO = new FindUserByPhoneReqDTO();
        findUserByPhoneReqDTO.setPhone(phone);
        Response<FindUserByPhoneRespDTO> response = userFeignApi.findByPhone(findUserByPhoneReqDTO);
        if (!response.isSuccess()) {
            return null;
        }
        return response.getData();
    }
    
    public void updatePassword(String encodePassword) {
        UpdateUserPasswordReqDTO updateUserPasswordReqDTO = new UpdateUserPasswordReqDTO();
        updateUserPasswordReqDTO.setEncodePassword(encodePassword);
        userFeignApi.updatePassword(updateUserPasswordReqDTO);
    }
}
