package com.kaiming.xiaohongshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteDOMapper;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteLikeDOMapper;
import com.kaiming.xiaohongshu.note.biz.enums.NoteTypeEnum;
import com.kaiming.xiaohongshu.note.biz.enums.ProfileNoteTypeEnum;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindProfileNoteRespVO;
import com.kaiming.xiaohongshu.note.biz.rpc.CountRpcService;
import com.kaiming.xiaohongshu.note.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.note.biz.service.ProfileService;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ClassName: ProfileServiceImpl
 * Package: com.kaiming.xiaohongshu.note.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 16:40
 * @Version 1.0
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;
    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private CountRpcService countRpcService;

    @Override
    public PageResponse<FindProfileNoteRespVO> findNoteList(FindProfileNotePageListReqVO findProfileNotePageListReqVO) {
        Integer queryType = findProfileNotePageListReqVO.getType();
        Integer pageNo = findProfileNotePageListReqVO.getPageNo();
        Long userId = findProfileNotePageListReqVO.getUserId();

        // 每页展示的数据量
        long pageSize = 10;

        // TODO: 为快速完成前后端联调，目前走数据库查询

        ProfileNoteTypeEnum profileNoteTypeEnum = ProfileNoteTypeEnum.valueOf(queryType);

        List<FindProfileNoteRespVO> noteRspVOS = Lists.newArrayList();
        List<NoteDO> noteDOS = null;
        int count = 0;

        switch (profileNoteTypeEnum) {
            case ALL -> {
                // 查询所有笔记
                count = noteDOMapper.selectTotalCountByCreatorId(userId);
                // 计算分页查询的偏移量 offset
                long offset = PageResponse.getOffset(pageNo, pageSize);

                PageResponse<FindProfileNoteRespVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                noteDOS = noteDOMapper.selectPageListByCreatorId(userId, offset, pageSize);
            }
            case COLLECTED -> {
                count = noteCollectionDOMapper.selectTotalCountByUserId(userId);

                // 计算分页查询的偏移量 offset
                long offset = PageResponse.getOffset(pageNo, pageSize);

                PageResponse<FindProfileNoteRespVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                List<Long> noteIds = noteCollectionDOMapper.selectPageListByUserId(userId, offset, pageSize);

                if (CollUtil.isNotEmpty(noteIds)) {
                    List<NoteDO> notes = noteDOMapper.selectByNoteIds(noteIds);
                    Map<Long, NoteDO> noteIdAndDOMap = notes.stream().collect(Collectors.toMap(NoteDO::getId, noteDO -> noteDO));
                    noteDOS = noteIds.stream()
                            .map(noteIdAndDOMap::get).collect(Collectors.toList());
                }
            }
            case LIKED -> {
                count = noteLikeDOMapper.selectTotalCountByUserId(userId);

                // 计算分页查询的偏移量 offset
                long offset = PageResponse.getOffset(pageNo, pageSize);

                PageResponse<FindProfileNoteRespVO> checkResponse = checkCountAndPageNo(count, pageNo, pageSize);
                if (Objects.nonNull(checkResponse)) return checkResponse;

                List<Long> noteIds = noteLikeDOMapper.selectPageListByUserId(userId, offset, pageSize);

                if (CollUtil.isNotEmpty(noteIds)) {
                    List<NoteDO> notes = noteDOMapper.selectByNoteIds(noteIds);
                    Map<Long, NoteDO> noteIdAndDOMap = notes.stream().collect(Collectors.toMap(NoteDO::getId, noteDO -> noteDO));
                    noteDOS = noteIds.stream()
                            .map(noteIdAndDOMap::get).collect(Collectors.toList());
                }
            }
        }

        if (CollUtil.isNotEmpty(noteDOS)) {
            List<Long> creatorIds = noteDOS.stream().map(NoteDO::getCreatorId).toList();

            // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
            List<FindUserByIdRespDTO> findUserByIdRspDTOS = userRpcService.findByIds(creatorIds);
            Map<Long, FindUserByIdRespDTO> userIdAndDTOMap = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRespDTO::getId, dto -> dto));

            // 批量查询笔记计数 TODO: 快速完成前后端联调，后续需要改成走 RPC 查询
            List<Long> noteIds = noteDOS.stream().map(NoteDO::getId).toList();
            List<FindNoteCountByIdRespDTO> noteCountByIds = countRpcService.findNoteCountByIds(noteIds);
            Map<Long, FindNoteCountByIdRespDTO> noteIdAndDOMap = noteCountByIds.stream()
                        .collect(Collectors.toMap(FindNoteCountByIdRespDTO::getNoteId, noteCountDO -> noteCountDO));

            // 分页返参
            for (NoteDO noteDO : noteDOS) {
                Integer type = noteDO.getType();
                FindProfileNoteRespVO findProfileNoteRespVO = FindProfileNoteRespVO.builder()
                        .id(String.valueOf(noteDO.getId()))
                        .title(noteDO.getTitle())
                        .type(type)
                        .build();

                NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

                switch (noteTypeEnum) {
                    case IMAGE_TEXT -> {
                        // 提取第一张图片作为封面图
                        String cover = Optional.ofNullable(noteDO.getImgUris())
                                .map(uris -> {
                                    String[] arr = StringUtils.split(uris, ",");
                                    return (arr != null && arr.length > 0) ? arr[0] : null;
                                })
                                .orElse(null);
                        findProfileNoteRespVO.setCover(cover);
                    }
                    case VIDEO -> findProfileNoteRespVO.setVideoUri(noteDO.getVideoUri());
                }

                // 设置发布者信息
                Long noteCreatorId = noteDO.getCreatorId();
                FindUserByIdRespDTO findUserByIdRspDTO = userIdAndDTOMap.get(noteCreatorId);
                if (Objects.nonNull(findUserByIdRspDTO)) {
                    findProfileNoteRespVO.setCreatorId(noteCreatorId);
                    findProfileNoteRespVO.setNickname(findUserByIdRspDTO.getNickName());
                    findProfileNoteRespVO.setAvatar(findUserByIdRspDTO.getAvatar());
                }

                // 设置点赞数据
                FindNoteCountByIdRespDTO noteCountDO = noteIdAndDOMap.get(noteDO.getId());
                findProfileNoteRespVO.setLikeTotal(Objects.nonNull(noteCountDO)
                        ? String.valueOf(noteCountDO.getLikeTotal()) : "0");

                noteRspVOS.add(findProfileNoteRespVO);
            }
        }
        return PageResponse.success(noteRspVOS, pageNo, pageSize);
    }

    /**
     * @param count
     * @param pageNo
     * @param pageSize
     * @return
     */
    private PageResponse<FindProfileNoteRespVO> checkCountAndPageNo(int count, Integer pageNo, long pageSize) {
        // 若评论总数为 0，则直接响应
        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        long totalPage = PageResponse.getTotalPage(count, pageSize);

        // 若请求的页码大于总页数，直接响应
        if (pageNo > totalPage) {
            return PageResponse.success(null, pageNo, totalPage);
        }
        return null;
    }
}
