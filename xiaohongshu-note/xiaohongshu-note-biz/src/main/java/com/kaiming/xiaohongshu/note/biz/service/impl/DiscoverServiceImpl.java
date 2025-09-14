package com.kaiming.xiaohongshu.note.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.xiaohongshu.count.dto.FindNoteCountByIdRespDTO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import com.kaiming.xiaohongshu.note.biz.domain.mapper.NoteDOMapper;
import com.kaiming.xiaohongshu.note.biz.enums.NoteTypeEnum;
import com.kaiming.xiaohongshu.note.biz.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNotePageListReqVO;
import com.kaiming.xiaohongshu.note.biz.model.vo.FindDiscoverNoteRespVO;
import com.kaiming.xiaohongshu.note.biz.rpc.CountRpcService;
import com.kaiming.xiaohongshu.note.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.note.biz.service.DiscoverService;
import com.kaiming.xiaohongshu.user.dto.req.FindUsersByIdsReqDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ClassName: DiscoverServiceImpl
 * Package: com.kaiming.xiaohongshu.note.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/9/14 15:30
 * @Version 1.0
 */
@Service
public class DiscoverServiceImpl implements DiscoverService {

    @Resource
    private NoteDOMapper noteDOMapper;
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private CountRpcService countRpcService;

    @Override
    public PageResponse<FindDiscoverNoteRespVO> findNoteList(FindDiscoverNotePageListReqVO findDiscoverNotePageListReqVO) {
        // 频道Id
        Long channelId = findDiscoverNotePageListReqVO.getChannelId();
        // 获取页码
        Integer pageNo = findDiscoverNotePageListReqVO.getPageNo();

        // 每页展示10条数据
        long pageSize = 10;

        int count = noteDOMapper.selectTotalCount(channelId);

        if (count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }
        // 偏移量
        long offset = PageResponse.getOffset(pageNo, pageSize);
        // 总页数
        long totalPage = PageResponse.getTotalPage(count, pageSize);
        // 若请求的页码大于总页数，直接响应
        if (pageNo > totalPage) {
            return PageResponse.success(null, pageNo, totalPage);
        }
        List<NoteDO> noteDOS = noteDOMapper.selectPageList(channelId, offset, pageSize);
        List<Long> creatorIds = noteDOS.stream().map(NoteDO::getCreatorId).toList();

        // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
        List<FindUserByIdRespDTO> findUserByIdRespDTOS = userRpcService.findByIds(creatorIds);

        Map<Long, FindUserByIdRespDTO> userIdAndMap = findUserByIdRespDTOS.stream()
                .collect(Collectors.toMap(FindUserByIdRespDTO::getId, dto -> dto));

        // 批量查询笔记计数 TODO: 快速完成前后端联调，后续需要改成走 RPC 查询
        List<Long> noteIds = noteDOS.stream().map(NoteDO::getId).toList();
        List<FindNoteCountByIdRespDTO> noteCountByIds = countRpcService.findNoteCountByIds(noteIds);

        Map<Long, FindNoteCountByIdRespDTO> noteCountAndMap = noteCountByIds.stream()
                .collect(Collectors.toMap(FindNoteCountByIdRespDTO::getNoteId, dto -> dto));

        List<FindDiscoverNoteRespVO> noteRspVOS = Lists.newArrayList();

        noteDOS.forEach(noteDO -> {
            Integer type = noteDO.getType();
            FindDiscoverNoteRespVO findDiscoverNoteRespVO = FindDiscoverNoteRespVO.builder()
                    .id(String.valueOf(noteDO.getId()))
                    .title(noteDO.getTitle())
                    .type(type)
                    .build();
            NoteTypeEnum noteTypeEnum = NoteTypeEnum.valueOf(type);

            if (Objects.isNull(noteTypeEnum)) {
                throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
            }

            switch (noteTypeEnum) {
                case IMAGE_TEXT -> {
                    String cover = Optional.ofNullable(noteDO.getImgUris())
                            .map(uris -> StringUtils.split(uris, ",")[0])
                            .orElse(null);
                    findDiscoverNoteRespVO.setCover(cover);
                }
                case VIDEO -> findDiscoverNoteRespVO.setVideoUri(noteDO.getVideoUri());
            }
            // 作者信息
            Long creatorId = noteDO.getCreatorId();
            FindUserByIdRespDTO findUserByIdRespDTO = userIdAndMap.get(creatorId);

            if (Objects.nonNull(findUserByIdRespDTO)) {
                findDiscoverNoteRespVO.setCreatorId(creatorId);
                findDiscoverNoteRespVO.setNickname(findUserByIdRespDTO.getNickName());
                findDiscoverNoteRespVO.setAvatar(findUserByIdRespDTO.getAvatar());
            }

            // 点赞数据
            FindNoteCountByIdRespDTO noteCountDO = noteCountAndMap.get(noteDO.getId());
            findDiscoverNoteRespVO.setLikeTotal(Objects.nonNull(noteCountDO.getLikeTotal())
                    ? String.valueOf(noteCountDO.getLikeTotal()) : "0");
            
            noteRspVOS.add(findDiscoverNoteRespVO);
        });

        return PageResponse.success(noteRspVOS, pageNo, count, pageSize);
    }
}
