package com.kaiming.xiaohongshu.note.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicRespVO;

import java.util.List;

/**
 * ClassName: TopicService
 * Package: com.kaiming.xiaohongshu.note.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 18:25
 * @Version 1.0
 */
public interface TopicService {

    Response<List<FindTopicRespVO>> findTopicList(FindTopicListReqVO findTopicListReqVO);
}
