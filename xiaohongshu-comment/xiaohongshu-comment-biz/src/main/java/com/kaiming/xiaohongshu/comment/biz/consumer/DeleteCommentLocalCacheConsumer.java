package com.kaiming.xiaohongshu.comment.biz.consumer;

import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * ClassName: DeleteCommentLocalCacheConsumer
 * Package: com.kaiming.xiaohongshu.comment.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/23 15:13
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE,
        topic = MQConstants.TOPIC_DELETE_COMMENT_LOCAL_CACHE)
@Slf4j
public class DeleteCommentLocalCacheConsumer implements RocketMQListener<String> {
    
    @Resource
    private CommentService commentService;
    
    @Override
    public void onMessage(String body) {
        Long commentId = Long.valueOf(body);
        log.info("## 消费者消费成功, commentId: {}", commentId);
        commentService.deleteCommentLocalCache(commentId);
    }
}
