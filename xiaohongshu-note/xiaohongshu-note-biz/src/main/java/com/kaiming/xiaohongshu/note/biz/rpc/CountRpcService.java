package com.kaiming.xiaohongshu.note.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.count.api.CountFeignApi;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdReqDTO;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * ClassName: CountRpcService
 * Package: com.kaiming.xiaohongshu.note.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/25 20:28
 * @Version 1.0
 */
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;
    
    /**
     * 查询笔记计数数据
     *
     * @param ids
     * @return
     */
    public List<FindNoteCountByIdRespDTO> findNoteCountByIds(List<Long> ids) {
        FindNoteCountByIdReqDTO findNoteCountByIdReqDTO = new FindNoteCountByIdReqDTO();
        findNoteCountByIdReqDTO.setNoteIds(ids);

        Response<List<FindNoteCountByIdRespDTO>> response = countFeignApi.findNoteCountData(findNoteCountByIdReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }
}
