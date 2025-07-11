package com.kaiming.xiaohongshu.count.biz.consumer;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.UserCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.model.dto.NoteOperateDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * ClassName: CountNotePublishConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/11 20:48
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_NOTE_OPERATE,
        topic = MQConstants.TOPIC_NOTE_OPERATE)
@Slf4j
public class CountNotePublishConsumer implements RocketMQListener<Message> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserCountDOMapper userCountDOMapper;
    @Override
    public void onMessage(Message message) {

        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();
        log.info("==> CountNotePublishConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        if (Objects.equals(tags, MQConstants.TAG_NOTE_PUBLISH)) {
            handleTagMessage(bodyJsonStr, 1);
        } else if (Objects.equals(tags, MQConstants.TAG_NOTE_DELETE)) {
            handleTagMessage(bodyJsonStr, -1);
        }

        
    }

    /**
     * 处理笔记消息
     *
     * @param bodyJsonStr
     */
    private void handleTagMessage(String bodyJsonStr, long count) {
        // 消息体 Json 转化为 DTO 对象
        NoteOperateDTO noteOperateDTO = JsonUtils.parseObject(bodyJsonStr, NoteOperateDTO.class);

        if (Objects.isNull(noteOperateDTO)) return;
        // 发布笔记作者 Id
        Long creatorId = noteOperateDTO.getCreatorId();

        // 更新 Redis 中用户维度的计数 Hash
        String countUserRedisKey = RedisKeyConstants.buildCountUserKey(creatorId);

        // 判断 Redis 中 Hash 是否存在
        Boolean isCountUserExisted = redisTemplate.hasKey(countUserRedisKey);
        // 若存在才会更新
        // (因为缓存设有过期时间，考虑到过期后，缓存会被删除，这里需要判断一下，存在才会去更新，而初始化工作放在查询计数来做)
        if (isCountUserExisted) {
            redisTemplate.opsForHash().increment(countUserRedisKey, RedisKeyConstants.FIELD_NOTE_TOTAL, count);
        }
        // 更新 t_user_count 表
        userCountDOMapper.insertOrUpdateNoteTotalByUserId(count, creatorId);
    }
}
