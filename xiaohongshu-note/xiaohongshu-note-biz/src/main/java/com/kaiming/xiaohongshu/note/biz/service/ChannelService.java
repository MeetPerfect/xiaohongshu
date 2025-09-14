package com.kaiming.xiaohongshu.note.biz.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindChannelRespVO;

import java.util.List;

/**
 * ClassName: ChannelService
 * Package: com.kaiming.xiaohongshu.note.biz.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/13 22:19
 * @Version 1.0
 */
public interface ChannelService {
    
    
    Response<List<FindChannelRespVO>> findChannelList();
}
