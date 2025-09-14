package com.kaiming.xiaohongshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.TopicDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.TopicDOMapper;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindTopicRespVO;
import com.kaiming.xiaohongshu.note.biz.service.TopicService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: TopicServiceImpl
 * Package: com.kaiming.xiaohongshu.note.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 18:26
 * @Version 1.0
 */
@Service
@Slf4j
public class TopicServiceImpl implements TopicService {

    @Resource
    private TopicDOMapper topicDOMapper;

    @Override
    public Response<List<FindTopicRespVO>> findTopicList(FindTopicListReqVO findTopicListReqVO) {
        String keyword = findTopicListReqVO.getKeyword();

        List<TopicDO> topicDOS = topicDOMapper.selectByLikeName(keyword);

        List<FindTopicRespVO> findTopicRspVOS = null;
        if (CollUtil.isNotEmpty(topicDOS)) {
            findTopicRspVOS = topicDOS.stream()
                    .map(topicDO -> FindTopicRespVO.builder()
                            .id(topicDO.getId())
                            .name(topicDO.getName())
                            .build())
                    .toList();
        }

        return Response.success(findTopicRspVOS);
    
    }
}
