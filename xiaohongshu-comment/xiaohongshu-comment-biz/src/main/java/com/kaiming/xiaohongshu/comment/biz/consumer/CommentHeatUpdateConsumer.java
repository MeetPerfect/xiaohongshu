package com.kaiming.xiaohongshu.comment.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentDO;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentDOMapper;
import com.kaiming.xiaohongshu.comment.biz.model.bo.CommentBO;
import com.kaiming.xiaohongshu.comment.biz.model.bo.CommentHeatBO;
import com.kaiming.xiaohongshu.comment.biz.util.HeatCalculator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * ClassName: CommentHeatUpdateConsumer
 * Package: com.kaiming.xiaohongshu.comment.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/19 19:51
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COMMENT_HEAT_UPDATE,
        topic = MQConstants.TOPIC_COMMENT_HEAT_UPDATE)
@Slf4j
public class CommentHeatUpdateConsumer implements RocketMQListener<String> {

    @Resource
    private CommentDOMapper commentDOMapper;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofMillis(1))
            .setConsumerEx(this::consumeMessage)
            .build();


    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【评论热度值计算】聚合消息, size: {}", bodys.size());
        log.info("==> 【评论热度值计算】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 Set<Long>, 去重相同的评论 ID, 防止重复计算

        Set<Long> commentIds = Sets.newHashSet();
        bodys.forEach(body -> {
            try {
                Set<Long> list = JsonUtils.parseSet(body, Long.class);
                commentIds.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        log.info("==> 去重后的评论 ID: {}", commentIds);

        // 批量查询评论
        List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(commentIds.stream().toList());
        // 评论 Id
        List<Long> ids = Lists.newArrayList();
        // 热度值 BO
        List<CommentHeatBO> commentBOS = Lists.newArrayList();

        commentDOS.forEach(commentDO -> {
            // 评论 Id
            Long commentDOId = commentDO.getId();
            // 点赞数
            Long likeTotal = commentDO.getLikeTotal();
            // 被回复数
            Long childCommentTotal = commentDO.getChildCommentTotal();
            // 计算热度值
            BigDecimal heatNum = HeatCalculator.calculateHeat(likeTotal, childCommentTotal);

            ids.add(commentDOId);
            CommentHeatBO commentHeatBO = CommentHeatBO.builder()
                    .id(commentDOId)
                    .heat(heatNum.doubleValue())
                    .build();

            commentBOS.add(commentHeatBO);
        });

        // 批量更新评论热度值
        commentDOMapper.batchUpdateHeatByCommentIds(ids, commentBOS);
    }
}
