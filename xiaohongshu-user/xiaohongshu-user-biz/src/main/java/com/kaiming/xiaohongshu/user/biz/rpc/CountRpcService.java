package com.kaiming.xiaohongshu.user.biz.rpc;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.api.CountFeignApi;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName: CountRpcService
 * Package: com.kaiming.xiaohongshu.user.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 23:04
 * @Version 1.0
 */
@Component
public class CountRpcService {
    
    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 批量查询用户计数
     * @param userId
     * @return
     */
    public FindUserCountsByIdRespDTO  findUserCountById(Long userId) {
        FindUserCountsByIdReqDTO findUserCountsByIdReqDTO = new FindUserCountsByIdReqDTO();
        findUserCountsByIdReqDTO.setUserId(userId);

        Response<FindUserCountsByIdRespDTO> response = countFeignApi.findUserCountData(findUserCountsByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();

    }
}
