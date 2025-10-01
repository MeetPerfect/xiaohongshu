package com.kaiming.xiaohongshu.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.api.CountFeignApi;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdsReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * ClassName: CountRpcService
 * Package: com.kaiming.xiaohongshu.user.relation.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/27 21:16
 * @Version 1.0
 */
@Component
public class CountRpcService {
    
    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 根据用户ids集合批量查询用户计数
     * @param userIds
     * @return
     */
    public List<FindUserCountsByIdRespDTO> findUserCountsByIds(List<Long> userIds) {
        FindUserCountsByIdsReqDTO findUserCountByIdsReqDTO = FindUserCountsByIdsReqDTO.builder()
                .userIds(userIds)
                .build();
        Response<List<FindUserCountsByIdRespDTO>> response = countFeignApi.findUserCountsList(findUserCountByIdsReqDTO);
        
        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }
        
        return response.getData();
    }
}
