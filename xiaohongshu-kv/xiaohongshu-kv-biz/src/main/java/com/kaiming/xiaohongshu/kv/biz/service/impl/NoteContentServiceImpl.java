package com.kaiming.xiaohongshu.kv.biz.service.impl;

import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.NoteContentDO;
import com.kaiming.xiaohongshu.kv.biz.domain.repository.NoteContentRepository;
import com.kaiming.xiaohongshu.kv.biz.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.kv.biz.service.NoteContentService;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.DeleteNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.req.FindNoteContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindNoteContentRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * ClassName: NoteContentServiceImpl
 * Package: com.kaiming.xiaohongshu.kv.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 18:23
 * @Version 1.0
 */
@Service
@Slf4j
public class NoteContentServiceImpl implements NoteContentService {
    @Resource
    private NoteContentRepository noteContentRepository;

    /**
     * 新增笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {

        String uuid = addNoteContentReqDTO.getUuid();
        String content = addNoteContentReqDTO.getContent();

        // 构建 NoteContentDO 对象
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();
        // 插入数据
        noteContentRepository.save(noteContentDO);
        return Response.success();
    }

    /**
     * 查询笔记内容
     *
     * @param findNoteContentReqDTO
     * @return
     */
    @Override
    public Response<FindNoteContentRespDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO) {
        String uuid = findNoteContentReqDTO.getUuid();
        // 根据笔记Id查询笔记内容
        Optional<NoteContentDO> optional = noteContentRepository.findById(UUID.fromString(uuid));

        if (!optional.isPresent()) {
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        NoteContentDO noteContentDO = optional.get();

        // 构建DTO对象
        FindNoteContentRespDTO findNoteContentRespDTO = FindNoteContentRespDTO.builder()
                .uuid(noteContentDO.getId())
                .content(noteContentDO.getContent())
                .build();
        return Response.success(findNoteContentRespDTO);
    }

    /**
     * 删除笔记内容
     *
     * @param deleteNoteContentReqDTO
     * @return
     */
    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        String uuid = deleteNoteContentReqDTO.getUuid();
        noteContentRepository.deleteById(UUID.fromString(uuid));
        return Response.success();
    }
}
