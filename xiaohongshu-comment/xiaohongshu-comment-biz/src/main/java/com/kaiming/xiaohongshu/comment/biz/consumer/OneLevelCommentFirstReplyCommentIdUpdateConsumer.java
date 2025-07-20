package com.kaiming.xiaohongshu.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.comment.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.comment.biz.constant.RedisKeyConstant;
import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentDO;
import com.kaiming.xiaohongshu.comment.biz.domain.mapper.CommentDOMapper;
import com.kaiming.xiaohongshu.comment.biz.enums.CommentLevelEnum;
import com.kaiming.xiaohongshu.comment.biz.model.dto.CountPublishCommentMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: OneLevelCommentFirstReplyCommentIdUpdateConsumer
 * Package: com.kaiming.xiaohongshu.comment.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 10:41
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_first_reply_comment_id" + MQConstants.TOPIC_COUNT_NOTE_COMMENT,
        topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT)
@Slf4j
public class OneLevelCommentFirstReplyCommentIdUpdateConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
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
        log.info("==> 【一级评论 first_reply_comment_id 更新】聚合消息, size: {}", bodys.size());
        log.info("==> 【一级评论 first_reply_comment_id 更新】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 List<CountPublishCommentMqDTO>
        List<CountPublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();

        bodys.forEach(body -> {
            try {
                List<CountPublishCommentMqDTO> list = JsonUtils.parseList(body, CountPublishCommentMqDTO.class);
                publishCommentMqDTOS.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        // 过滤出二级评论的 parent_id（即一级评论 ID），并去重，需要更新对应一级评论的 first_reply_comment_id
        List<Long> parentIds = publishCommentMqDTOS.stream()
                .filter(publishCommentMqDTO -> Objects.equals(publishCommentMqDTO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .map(CountPublishCommentMqDTO::getParentId)
                .distinct()
                .toList();

        if (CollUtil.isEmpty(parentIds)) return;

        // 构建 Redis key
        List<String> keys = parentIds.stream()
                .map(RedisKeyConstant::buildHaveFirstReplyCommentKey).toList();

        // 批量查询 Redis
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        // 提取 Redis 中不存在的评论 ID
        List<Long> missingCommentIds = Lists.newArrayList();
        for (int i = 0; i < values.size(); i++) {
            if (Objects.isNull(values.get(i))) {
                missingCommentIds.add(parentIds.get(i));
            }
        }

        // 存在的一级评论 ID，说明表中对应记录的 first_reply_comment_id 已经有值
        if (CollUtil.isNotEmpty(missingCommentIds)) {
            // 不存在的，则需要进一步查询数据库来确定，是否要更新记录对应的 first_reply_comment_id 值
            // 批量查询数据库
            List<CommentDO> commentDOS = commentDOMapper.selectByCommentIds(missingCommentIds);

            // 异步将 first_reply_comment_id 不为 0 的一级评论 ID, 同步到 redis 中
            threadPoolTaskExecutor.submit(() -> {
                List<Long> needSyncCommentIds = commentDOS.stream()
                        .filter(commentDO -> commentDO.getFirstReplyCommentId() != 0)
                        .map(CommentDO::getId)
                        .toList();
                sync2Redis(needSyncCommentIds);
            });
            
            // 过滤出值为 0 的，都需要更新其 first_reply_comment_id
            List<CommentDO> needUpdateCommentDOS  = commentDOS.stream()
                    .filter(commentDO -> commentDO.getFirstReplyCommentId() == 0)
                    .toList();
            
            needUpdateCommentDOS.forEach(needUpdateCommentDO -> {
                // 一级评论 Id
                Long needUpdateCommentId = needUpdateCommentDO.getId();

                // 查询数据库，拿到一级评论最早回复的那条评论
                CommentDO earliestCommentDO = commentDOMapper.selectEarliestByParentId(needUpdateCommentId);

                if (Objects.nonNull(earliestCommentDO)) {
                    // 最早回复的那条评论 Id
                    Long earliestCommentId  = earliestCommentDO.getId();
                    // 更新其一级评论的 first_reply_comment_id
                    commentDOMapper.updateFirstReplyCommentIdByPrimaryKey(earliestCommentId , needUpdateCommentId);
                    
                    // 异步同步到 Redis
                    threadPoolTaskExecutor.submit(() -> sync2Redis(Lists.newArrayList(needUpdateCommentId)));
                }
            });

        }
    }

    /**
     * 同步 Redis中
     * @param needSyncCommentIds
     */
    private void sync2Redis(List<Long> needSyncCommentIds) {
        // 获取 ValueOperations
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 使用 RedisTemplate 的管道模式，允许在一个操作中批量发送多个命令，防止频繁操作 Redis
        redisTemplate.executePipelined((RedisCallback<?>) (connection) ->{
            needSyncCommentIds.forEach(needSyncCommentId -> {
                // 构建 RedisKey
                String key = RedisKeyConstant.buildHaveFirstReplyCommentKey(needSyncCommentId);
                
                valueOperations.set(key, 1, RandomUtil.randomInt(5 * 60 * 60), TimeUnit.SECONDS );
            });
            return null;
        });
    }
}
