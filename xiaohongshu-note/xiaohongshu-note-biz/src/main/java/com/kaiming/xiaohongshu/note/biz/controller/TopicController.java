package com.kaiming.xiaohongshu.note.biz.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicRespVO;
import com.kaiming.xiaohongshu.note.biz.service.TopicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ClassName: TopicController
 * Package: com.kaiming.xiaohongshu.note.biz.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 18:28
 * @Version 1.0
 */
@RestController
@RequestMapping("/topic")
public class TopicController {
    
    @Resource
    private TopicService topicService;
    
    @PostMapping("/list")
    @ApiOperationLog(description = "模糊查询话题列表")
    public Response<List<FindTopicRespVO>> findTopicList(@RequestBody FindTopicListReqVO findTopicListReqVO) {
        return topicService.findTopicList(findTopicListReqVO);
    }
}
