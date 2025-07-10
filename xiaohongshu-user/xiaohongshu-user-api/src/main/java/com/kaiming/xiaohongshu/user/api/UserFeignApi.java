package com.kaiming.xiaohongshu.user.api;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.constant.ApiConstants;
import com.kaiming.xiaohongshu.user.dto.req.*;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: UserFeignApi
 * Package: com.kaiming.xiaohongshu.user.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 16:49
 * @Version 1.0
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {

    String PREFIX = "/user";

    /**
     * 用户注册
     *
     * @param registerUserReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号查询用户信息
     *
     * @param findUserByPhoneReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/findByPhone")
    Response<FindUserByPhoneRespDTO> findByPhone(@RequestBody FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 更新用户密码
     *
     * @param updateUserPasswordReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    /**
     * 根据用户ID查询用户信息
     *
     * @param findUserByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/findById")
    Response<FindUserByIdRespDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);

    /**
     * 根据用户ID列表批量查询用户信息
     * @param findUsersByIdsReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/findByIds")
    Response<List<FindUserByIdRespDTO>> findByIds(@RequestBody FindUsersByIdsReqDTO findUsersByIdsReqDTO);
}
