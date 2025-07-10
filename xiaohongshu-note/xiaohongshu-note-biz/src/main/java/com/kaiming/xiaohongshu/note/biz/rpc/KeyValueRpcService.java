package com.kaiming.xiaohongshu.note.biz.rpc;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.api.KeyValueFeignApi;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName: KeyValueRpcService
 * Package: com.kaiming.xiaohongshu.note.biz.rpc
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/13 20:34
 * @Version 1.0
 */
@Component
public class KeyValueRpcService {

    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 保存笔记内容
     * @param uuid
     * @param content
     * @return
     */
    public boolean saveNoteContent(String uuid, String content) {
        AddNoteContentReqDTO addNoteContentReqDTO = new AddNoteContentReqDTO();
        addNoteContentReqDTO.setUuid(uuid);
        addNoteContentReqDTO.setContent(content);

        Response<?> response = keyValueFeignApi.addNoteContent(addNoteContentReqDTO);

        return !Objects.isNull(response) && response.isSuccess();
    }

    /**
     * 删除笔记内容
     * @param uuid
     * @return
     */
    public boolean deleteNoteContent(String uuid) {
        DeleteNoteContentReqDTO deleteNoteContentReqDTO  = new DeleteNoteContentReqDTO(uuid);
        Response<?> response = keyValueFeignApi.deleteNoteContent(deleteNoteContentReqDTO);

        return !Objects.isNull(response) && response.isSuccess();
    }

    /**
     * 根据UUID查询笔记内容
     * @param uuid
     * @return
     */
    public String  findNoteContent(String uuid) {
        FindNoteContentReqDTO findNoteContentReqDTO = new FindNoteContentReqDTO();
        findNoteContentReqDTO.setUuid(uuid);

        Response<FindNoteContentRespDTO> response = keyValueFeignApi.findNoteContent(findNoteContentReqDTO);
        
        if (Objects.isNull(response) || !response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }
        return response.getData().getContent();
    }
}
