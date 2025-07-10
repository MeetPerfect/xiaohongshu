package com.kaiming.xiaohongshu.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.user.api.UserFeignApi;
import com.kaiming.xiaohongshu.user.dto.req.FindUserByIdReqDTO;
import com.kaiming.xiaohongshu.user.dto.req.FindUsersByIdsReqDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * ClassName: UserRpcService
 * Package: com.kaiming.xiaohongshu.user.relation.biz.domain.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 12:19
 * @Version 1.0
 */
@Component
public class UserRpcService {
    
    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 根据用户Id查询
     * @param id
     * @return
     */
    public FindUserByIdRespDTO findById(Long id) {
        FindUserByIdReqDTO findUserByIdReqDTO = new FindUserByIdReqDTO(id);
        Response<FindUserByIdRespDTO> response = userFeignApi.findById(findUserByIdReqDTO);
        
        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData();
    }

    /**
     * 批量查询用户信息
     * @param userIds
     * @return
     */
    public List<FindUserByIdRespDTO> findByIds(List<Long> userIds) {
        FindUsersByIdsReqDTO findUsersByIdsReqDTO = new FindUsersByIdsReqDTO(userIds);
        findUsersByIdsReqDTO.setIds(userIds);

        Response<List<FindUserByIdRespDTO>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);
        
        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }
        return response.getData();
    }
}
