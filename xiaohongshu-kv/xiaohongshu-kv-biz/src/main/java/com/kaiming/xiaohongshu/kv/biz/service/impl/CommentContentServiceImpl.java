package com.kaiming.xiaohongshu.kv.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.CommentContentDO;
import com.kaiming.xiaohongshu.kv.biz.domain.dataobject.CommentContentPrimaryKey;
import com.kaiming.xiaohongshu.kv.biz.domain.repository.CommentContentRepository;
import com.kaiming.xiaohongshu.kv.biz.service.CommentContentService;
import com.kaiming.xiaohongshu.kv.dto.req.*;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ClassName: CommentContentServiceImpl
 * Package: com.kaiming.xiaohongshu.kv.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 22:03
 * @Version 1.0
 */
@Service
@Slf4j
public class CommentContentServiceImpl implements CommentContentService {

    @Resource
    private CassandraTemplate cassandraTemplate;
    @Resource
    private CommentContentRepository commentContentRepository;

    /**
     * 批量添加评论内容
     *
     * @param batchAddCommentContentReqDTO
     * @return
     */
    @Override
    public Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {
        // 获取评论集合对象
        List<CommentContentReqDTO> comments = batchAddCommentContentReqDTO.getComments();

        // DTO 转 DO
        List<CommentContentDO> contentDOS = comments.stream()
                .map(commentContentReqDTO -> {
                    // 构建主键类
                    CommentContentPrimaryKey commentContentPrimaryKey = CommentContentPrimaryKey.builder()
                            .noteId(commentContentReqDTO.getNoteId())
                            .contentId(UUID.fromString(commentContentReqDTO.getContentId()))
                            .yearMonth(commentContentReqDTO.getYearMonth())
                            .build();
                    // 构建 DO 实体类
                    CommentContentDO commentContentDO = CommentContentDO.builder()
                            .primaryKey(commentContentPrimaryKey)
                            .content(commentContentReqDTO.getContent())
                            .build();
                    return commentContentDO;
                }).toList();
        // 实现批量插入
        cassandraTemplate.batchOps()
                .insert(contentDOS)
                .execute();
        return Response.success();
    }

    @Override
    public Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {

        // 笔记Id
        Long noteId = batchFindCommentContentReqDTO.getNoteId();
        // 查询评论的发布年月、内容 UUID
        List<FindCommentContentReqDTO> commentContentKeys = batchFindCommentContentReqDTO.getCommentContentKeys();
        // 过滤出年月
        List<String> yearMonths = commentContentKeys.stream()
                .map(FindCommentContentReqDTO::getYearMonth)
                .distinct()
                .collect(Collectors.toList());
        // 过滤出评论内容 UUID
        List<UUID> contentIds = commentContentKeys.stream()
                .map(commentContentKey -> UUID.fromString(commentContentKey.getContentId()))
                .distinct()
                .collect(Collectors.toList());
        // 批量查询 Cassandra
        List<CommentContentDO> commentContentDOS = commentContentRepository
                .findByPrimaryKeyNoteIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(noteId, yearMonths, contentIds);

        // DO 转 DTO
        List<FindCommentContentRespDTO> findCommentContentRspDTOS = Lists.newArrayList();

        if (CollUtil.isNotEmpty(commentContentDOS)) {
            findCommentContentRspDTOS = commentContentDOS.stream()
                    .map(commentContentDO -> FindCommentContentRespDTO.builder()
                            .contentId(String.valueOf(commentContentDO.getPrimaryKey().getContentId()))
                            .content(commentContentDO.getContent())
                            .build())
                    .toList();
        }
        return Response.success(findCommentContentRspDTOS);
    }

    /**
     * 删除评论
     * @param deleteCommentContentReqDTO
     * @return
     */
    @Override
    public Response<?> deleteCommentContent(DeleteCommentContentReqDTO deleteCommentContentReqDTO) {
        // 笔记 Id
        Long noteId = deleteCommentContentReqDTO.getNoteId();
        // 内容 Id
        String contentId = deleteCommentContentReqDTO.getContentId();
        // 日期
        String yearMonth = deleteCommentContentReqDTO.getYearMonth();
        // 删除评论正文
        commentContentRepository.deleteByPrimaryKeyNoteIdAndPrimaryKeyYearMonthAndPrimaryKeyContentId(
                noteId, yearMonth, UUID.fromString(contentId));
        
        return Response.success();
    }
}
