package com.kaiming.xiaohongshu.count.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdsReqDTO;

import java.util.List;

/**
 * ClassName: CountService
 * Package: com.kaiming.xiaohongshu.count.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 20:42
 * @Version 1.0
 */
public interface UserCountService {

    /**
     * 查询用户相关计数
     * @param findUserCountsByIdReqDTO
     * @return
     */
    Response<FindUserCountsByIdRespDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

    /**
     * 根据用户Id批量查询用户计数
     * @param findUserCountsByIdsReqDTO
     * @return
     */
    Response<List<FindUserCountsByIdRespDTO>> findUserCountsList (FindUserCountsByIdsReqDTO findUserCountsByIdsReqDTO);
}
