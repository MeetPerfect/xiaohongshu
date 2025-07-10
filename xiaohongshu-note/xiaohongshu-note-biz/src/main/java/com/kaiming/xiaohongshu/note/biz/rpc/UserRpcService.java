package com.kaiming.xiaohongshu.note.biz.rpc;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.api.UserFeignApi;
import com.kaiming.xiaohongshu.user.dto.req.FindUserByIdReqDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName: UserRpcService
 * Package: com.kaiming.xiaohongshu.note.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/15 14:45
 * @Version 1.0
 */
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 根据用户ID查询用户信息
     * @param userId
     * @return
     */
    public FindUserByIdRespDTO findById(Long userId) {
        FindUserByIdReqDTO findUserByIdReqDTO = new FindUserByIdReqDTO();
        findUserByIdReqDTO.setId(userId);
        
        Response<FindUserByIdRespDTO> response = userFeignApi.findById(findUserByIdReqDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }
        return response.getData();
    }
}
