package com.kaiming.xiaohongshu.count.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;

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
}
