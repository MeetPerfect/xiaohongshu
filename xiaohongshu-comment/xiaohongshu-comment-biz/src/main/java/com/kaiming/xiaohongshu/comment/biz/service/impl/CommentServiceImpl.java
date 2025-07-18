package com.kaiming.xiaohongshu.comment.biz.service.impl;

import com.google.common.base.Preconditions;
import com.kaiming.framework.biz.context.holder.LoginUserContextHolder;
import com.kaiming.framework.common.response.Response;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.model.dto.PublishCommentMqDTO;
import com.kaiming.xiaohongshu.comment.biz.model.vo.PublishCommentReqVO;
import com.kaiming.xiaohongshu.comment.biz.retry.SendMqRetryHelper;
import com.kaiming.xiaohongshu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;


import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

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
    private RocketMQTemplate  rocketMQTemplate;
    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    /**
     * 发布评论
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
        
        // 当前登录用户Id
        Long creatorId = LoginUserContextHolder.getUserId();
        
        // 发布 MQ 消息
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        sendMqRetryHelper.send(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));
        return Response.success();
    }
}
