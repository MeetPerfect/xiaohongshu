package com.kaiming.xiaohongshu.user.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.biz.model.vo.UpdateUserInfoReqVO;
import com.kaiming.xiaohongshu.user.dto.req.*;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByPhoneRespDTO;

import java.util.List;

/**
 * ClassName: UserService
 * Package: com.kaiming.xiaohongshu.user.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 21:54
 * @Version 1.0
 */
public interface UserService {

    /**
     * 更新用户信息
     * @param updateUserInfoReqVO
     * @return
     */
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    /**
     * 用户注册
     * @param registerUserReqDTO
     * @return
     */
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    /**
     * 根据手机号查询用户信息
     * @param findUserByPhoneReqDTO
     * @return
     */
    Response<FindUserByPhoneRespDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO);

    /**
     * 更新用户密码
     * @param updateUserPasswordReqDTO
     * @return
     */
    Response<?> updateUserPassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    /**
     * 根据用户Id查询用户信息
     * @param findUserByIdReqDTO
     * @return
     */
    Response<FindUserByIdRespDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO);

    /**
     * 根据用户Id列表查询用户信息
     * @param findUsersByIdsReqDTO
     * @return
     */
    Response<List<FindUserByIdRespDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO);
}
