package com.kaiming.xiaohongshu.count.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.biz.service.UserCountService;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindUserCountsByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: UserCountController
 * Package: com.kaiming.xiaohongshu.count.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 20:11
 * @Version 1.0
 */
@RestController
@RequestMapping("/count")
@Slf4j
public class UserCountController {
    
    @Resource
    private UserCountService userCountService;
    
    @PostMapping("/user/data")
    @ApiOperationLog(description = "查询用户计数")
    public Response<FindUserCountsByIdRespDTO> findUserCountData(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) {
        return userCountService.findUserCountData(findUserCountsByIdReqDTO);
    }
}
