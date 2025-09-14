package com.kaiming.xiaohongshu.note.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindChannelRespVO;
import com.kaiming.xiaohongshu.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: ChannelController
 * Package: com.kaiming.xiaohongshu.note.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/13 22:43
 * @Version 1.0
 */
@RestController
@RequestMapping("/channel")
@Slf4j
public class ChannelController {
    
    @Resource
    private ChannelService channelService;
    
    @PostMapping("/list")
    @ApiOperationLog(description = "获取所有频道")
    public Response<List<FindChannelRespVO>> findChannelList() {
        return channelService.findChannelList();
    }
}
