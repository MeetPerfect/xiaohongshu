package com.kaiming.xiaohongshu.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.constant.DateConstants;
import com.kaiming.framework.common.response.PageResponse;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentDO;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentDOMapper;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.NoteCountDOMapper;
import com.kaiming.xiaohongshu.comment.biz.model.dto.PublishCommentMqDTO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.FindCommentItemRespVO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.FindCommentPageListReqVO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.PublishCommentReqVO;
import com.kaiming.xiaohongshu.comment.biz.retry.SendMqRetryHelper;
import com.kaiming.xiaohongshu.comment.biz.rpc.DistributedIdGeneratorRpcService;
import com.kaiming.xiaohongshu.comment.biz.rpc.KeyValueRpcService;
import com.kaiming.xiaohongshu.comment.biz.rpc.UserRpcService;
import com.kaiming.xiaohongshu.comment.biz.service.CommentService;
import com.kaiming.xiaohongshu.kv.dto.req.FindCommentContentReqDTO;
import com.kaiming.xiaohongshu.kv.dto.resp.FindCommentContentRespDTO;
import com.kaiming.xiaohongshu.user.dto.resp.FindUserByIdRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: CommentServiceImpl
 * Package: com.kaiming.xiaohongshu.comment.biz.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Autowired
    private UserRpcService userRpcService;

    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {

        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图像
        String imageUrl = publishCommentReqVO.getImageUrl();
        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 当前登录用户Id, 发布者Id
        Long creatorId = LoginUserContextHolder.getUserId();
        // RPC: 调用分布式 ID 生成服务, 评论内容Id
        String commentId = distributedIdGeneratorRpcService.generateCommentId();
        // 发布 MQ 消息
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .commentId(Long.valueOf(commentId))
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        sendMqRetryHelper.send(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));
        return Response.success();
    }

    /**
     * 评论内容分页查询
     *
     * @param findCommentPageListReqVO
     * @return
     */
    @Override
    public PageResponse<FindCommentItemRespVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        // 笔记 Id
        Long noteId = findCommentPageListReqVO.getNoteId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        // 每页展示的评论数为10
        long pageSize = 10;

        // TODO: 先从缓存中查（后续小节补充）
        
        // 查询评论总数 (从 t_note_count 笔记计数表查，提升查询性能, 避免 count(*))
        Long count = noteCountDOMapper.selectCommentTotalByNoteId(noteId);
        if (Objects.isNull(count)) {
            return PageResponse.success(null, pageNo, pageSize);
        }
        // 分页返回参数
        List<FindCommentItemRespVO> commentRespVOS = null;

        if (count > 0) {
            commentRespVOS = Lists.newArrayList();

            // 计算分页查询的偏移量 offset
            long offset = PageResponse.getOffset(pageNo, pageSize);

            // 查询一级评论
            List<CommentDO> oneLevelCommentDOS = commentDOMapper.selectPageList(noteId, offset, pageSize);

            // 过滤出所有最早回复的二级评论 Id
            List<Long> twoLevelCommentIds = oneLevelCommentDOS.stream()
                    .map(CommentDO::getFirstReplyCommentId)
                    .filter(firstReplyCommentId -> firstReplyCommentId != 0)
                    .toList();

            // 查询二级评论
            Map<Long, CommentDO> commentIdAndDOMap = null;
            List<CommentDO> twoLevelCommonDOS = null;
            if (CollUtil.isNotEmpty(twoLevelCommentIds)) {
                twoLevelCommonDOS = commentDOMapper.selectTwoLevelCommentById(twoLevelCommentIds);
                //  转 Map 集合，方便后续拼装数据
                commentIdAndDOMap = twoLevelCommonDOS.stream()
                        .collect(Collectors.toMap(CommentDO::getId, commentDO -> commentDO));
            }

            // 调用 KV 服务需要的入参
            List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
            // 调用用户服务的入参
            List<Long> userIds = Lists.newArrayList();

            // 将一级评论和二级评论合并到一起
            List<CommentDO> allCommentDOS = Lists.newArrayList();
            CollUtil.addAll(allCommentDOS, oneLevelCommentDOS);
            CollUtil.addAll(allCommentDOS, twoLevelCommonDOS);
            // 循环提取 RPC 调用需要的入参数据
            allCommentDOS.forEach(commentDO -> {
                // 构建调用 KV 服务批量查询评论内容的入参
                boolean isContentEmpty = commentDO.getIsContentEmpty();
                if (!isContentEmpty) {
                    FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                            .contentId(commentDO.getContentUuid())
                            .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentDO.getCreateTime()))
                            .build();
                    findCommentContentReqDTOS.add(findCommentContentReqDTO);
                }
                // 构建调用用户服务批量查询用户信息的入参
                userIds.add(commentDO.getUserId());
            });

            // RPC: 调用 KV 服务，批量获取评论内容
            List<FindCommentContentRespDTO> findCommentContentRespDTOS =
                    keyValueRpcService.batchFindCommentContent(noteId, findCommentContentReqDTOS);
            // DTO 集合转 Map, 方便后续拼装数据
            Map<String, String> commentUuidAndContentMap = null;
            if (CollUtil.isNotEmpty(findCommentContentRespDTOS)) {
                commentUuidAndContentMap = findCommentContentRespDTOS.stream()
                        .collect(Collectors.toMap(FindCommentContentRespDTO::getContentId, FindCommentContentRespDTO::getContent));
            }

            // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）
            List<FindUserByIdRespDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);
            // DTO 集合转 Map, 方便后续拼装数据
            Map<Long, FindUserByIdRespDTO> userIdAndDTOMap = null;
            if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
                userIdAndDTOMap = findUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRespDTO::getId, dto -> dto));
            }

            // DO 转 VO, 组合拼装一二级评论数据
            for (CommentDO commentDO : oneLevelCommentDOS) {
                // 一级评论
                Long userId = commentDO.getUserId();
                FindCommentItemRespVO oneLevelCommentRespVO = FindCommentItemRespVO.builder()
                        .userId(userId)
                        .commentId(commentDO.getId())
                        .imageUrl(commentDO.getImageUrl())
                        .createTime(DateUtils.formatRelativeTime(commentDO.getCreateTime()))
                        .likeTotal(commentDO.getLikeTotal())
                        .childCommentTotal(commentDO.getChildCommentTotal())
                        .build();

                // 用户信息
                setUserInfo(commentIdAndDOMap, userIdAndDTOMap, userId, oneLevelCommentRespVO);
                // 笔记内容
                setCommentContent(commentUuidAndContentMap, commentDO, oneLevelCommentRespVO);

                // 二级评论
                Long firstReplyCommentId = commentDO.getFirstReplyCommentId();
                if (CollUtil.isNotEmpty(commentIdAndDOMap)) {
                    CommentDO firstReplyCommentDO = commentIdAndDOMap.get(firstReplyCommentId);

                    if (Objects.nonNull(firstReplyCommentDO)) {
                        Long firstReplyCommentUserId = firstReplyCommentDO.getUserId();

                        FindCommentItemRespVO firstReplyCommentRespVO = FindCommentItemRespVO.builder()
                                .userId(firstReplyCommentDO.getUserId())
                                .commentId(firstReplyCommentDO.getId())
                                .imageUrl(firstReplyCommentDO.getImageUrl())
                                .createTime(DateUtils.formatRelativeTime(firstReplyCommentDO.getCreateTime()))
                                .likeTotal(firstReplyCommentDO.getLikeTotal())
                                .build();
                        // 用户信息
                        setUserInfo(commentIdAndDOMap, userIdAndDTOMap, firstReplyCommentUserId, firstReplyCommentRespVO);
                        // 笔记内容
                        oneLevelCommentRespVO.setFirstReplyComment(firstReplyCommentRespVO);
                        setCommentContent(commentUuidAndContentMap, firstReplyCommentDO, firstReplyCommentRespVO);
                    }
                }
                commentRespVOS.add(oneLevelCommentRespVO);
            }

            // TODO 后续逻辑
        }
        return PageResponse.success(commentRespVOS, pageNo, count, pageSize);
    }

    /**
     * 用户信息
     *
     * @param commentIdAndDOMap
     * @param userIdAndDTOMap
     * @param userId
     * @param oneLevelCommentRespVO
     */
    private static void setUserInfo(Map<Long, CommentDO> commentIdAndDOMap, Map<Long, FindUserByIdRespDTO> userIdAndDTOMap, Long userId, FindCommentItemRespVO oneLevelCommentRespVO) {
        FindUserByIdRespDTO findUserByIdRespDTO = userIdAndDTOMap.get(userId);
        if (Objects.nonNull(findUserByIdRespDTO)) {
            oneLevelCommentRespVO.setAvatar(findUserByIdRespDTO.getAvatar());
            oneLevelCommentRespVO.setNickname(findUserByIdRespDTO.getNickName());
        }
    }

    /**
     * 笔记内容
     *
     * @param commentUuidAndContentMap
     * @param commentDO1
     * @param firstReplyCommentRespVO
     */
    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentDO commentDO1, FindCommentItemRespVO firstReplyCommentRespVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentDO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRespVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }
}
