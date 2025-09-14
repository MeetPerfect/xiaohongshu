package com.kaiming.xiaohongshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.ChannelDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.ChannelDOMapper;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindChannelRespVO;
import com.kaiming.xiaohongshu.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: ChannelServiceImpl
 * Package: com.kaiming.xiaohongshu.note.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/13 22:33
 * @Version 1.0
 */
@Service
public class ChannelServiceImpl implements ChannelService {

    @Resource
    private ChannelDOMapper channelDOMapper;
    
    
    @Override
    public Response<List<FindChannelRespVO>> findChannelList() {
        
        // TODO 添加二级缓存
        List<ChannelDO> channelDOS = channelDOMapper.selectAll();
        
        List<FindChannelRespVO> channelRespVOS = Lists.newArrayList();
        
        if (CollUtil.isNotEmpty(channelDOS)) {
            CollUtil.addAll(channelRespVOS, channelDOS.stream()
                    .map(channelDO -> FindChannelRespVO.builder()
                            .id(channelDO.getId())
                            .name(channelDO.getName())
                            .build())
                    .toList()
            );
        }

        return Response.success(channelRespVOS);
    }
}
