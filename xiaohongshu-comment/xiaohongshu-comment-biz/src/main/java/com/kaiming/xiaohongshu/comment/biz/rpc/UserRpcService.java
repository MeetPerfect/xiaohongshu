package com.kaiming.xiaohongshu.comment.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.api.UserFeignApi;
import com.kaiming.xiaohongshu.user.dto.req.FindUsersByIdsReqDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: UserRpcService
 * Package: com.kaiming.xiaohongshu.comment.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 14:38
 * @Version 1.0
 */
@Component
public class UserRpcService {
    
    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 批量查询用户信息
     * @param userIds
     * @return
     */
    public List<FindUserByIdRespDTO> findByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }

        FindUsersByIdsReqDTO findUsersByIdsReqDTO = new FindUsersByIdsReqDTO();
        findUsersByIdsReqDTO.setIds(userIds.stream().distinct().collect(Collectors.toList()));

        Response<List<FindUserByIdRespDTO>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }
}
