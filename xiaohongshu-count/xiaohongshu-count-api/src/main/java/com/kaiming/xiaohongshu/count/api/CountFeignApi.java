package com.kaiming.xiaohongshu.count.api;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.constant.ApiConstants;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: CountFeignApi
 * Package: com.kaiming.xiaohongshu.count.api
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 22:04
 * @Version 1.0
 */
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeignApi {

    String PREFIX = "/count";

    /**
     * 查询用户计数
     *
     * @param findUserCountsByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/user/data")
    Response<FindUserCountsByIdRespDTO> findUserCountData(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

    /**
     * 查询笔记计数数据
     *
     * @param findNoteCountByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/note/data")
    Response<List<FindNoteCountByIdRespDTO>> findNoteCountData(@RequestBody FindNoteCountByIdReqDTO findNoteCountByIdReqDTO);
}
