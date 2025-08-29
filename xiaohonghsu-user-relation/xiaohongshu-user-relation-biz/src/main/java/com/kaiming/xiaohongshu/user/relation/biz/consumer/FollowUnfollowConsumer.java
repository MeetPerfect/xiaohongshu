package com.kaiming.xiaohongshu.user.relation.biz.consumer;

import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.DateUtils;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.user.relation.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.user.relation.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.user.relation.biz.domain.dataobject.FansDO;
import com.kaiming.xiaohongshu.user.relation.biz.domain.dataobject.FollowingDO;
import com.kaiming.xiaohongshu.user.relation.biz.domain.mapper.FansDOMapper;
import com.kaiming.xiaohongshu.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.kaiming.xiaohongshu.user.relation.biz.enums.FollowUnfollowTypeEnum;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.CountFollowUnfollowMqDTO;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.FollowUserMqDTO;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.UnfollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * ClassName: FollowUnfollowConsumer
 * Package: com.kaiming.xiaohongshu.user.relation.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 17:13
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group" + MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,   // Group 组
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,   // 消费的Topic
        consumeMode = ConsumeMode.ORDERLY)              // 设置为顺序消费模式
@Slf4j
public class FollowUnfollowConsumer implements RocketMQListener<Message> {

    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private FansDOMapper fansDOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    
    @Override
    public void onMessage(Message message) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();
        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // Topic
        String tags = message.getTags();
        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);
        // 根据 MQ 标签，判断操作类型

        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) {     // 关注
            // 处理关注
            handleFollowTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNFOLLOW)) {     // 取关
            // 处理取关
            handlerUnfollowTagMessage(bodyJsonStr);
        }
    }

    /**
     * 取关
     * @param bodyJsonStr
     */
    private void handlerUnfollowTagMessage(String bodyJsonStr) {
        // 将消息题 Json 字符串转为 DTO 对象
        UnfollowUserMqDTO unfollowUserMqDTO = JsonUtils.parseObject(bodyJsonStr, UnfollowUserMqDTO.class);

        // 判空
        if (Objects.isNull(unfollowUserMqDTO)) return;

        Long userId = unfollowUserMqDTO.getUserId();
        Long unfollowUserId = unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime = unfollowUserMqDTO.getCreateTime();

        // 编程式提交事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 取关成功需要删除数据库两条记录
                // 关注表：一条记录
                int count = followingDOMapper.deleteByUserIdAndFollowingUserId(userId, unfollowUserId);
                if (count > 0) {
                    fansDOMapper.deleteByUserIdAndFansUserId(unfollowUserId, userId);
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("", e);
            }
            return false;
        }));

        // 若数据库操作成功，删除 Redis 中被关注用户的 ZSet 粉丝列表
        if (isSuccess) {
            // 被取关用户的粉丝列表 Redis key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(unfollowUserId);
            // 删除指定粉丝
            redisTemplate.opsForZSet().remove(fansRedisKey, userId);
            
            // 发送 MQ 消息，更新粉丝数计数
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(unfollowUserId)
                    .type(FollowUnfollowTypeEnum.UNFOLLOW.getCode())
                    .build();
            // 发送 MQ
            sendMQ(countFollowUnfollowMqDTO);
        }
    }

    /**
     * 关注
     *
     * @param bodyJsonStr
     */
    private void handleFollowTagMessage(String bodyJsonStr) {
        // 将消息体 Json 字符串转为 DTO 对象
        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);

        // 判空
        if (Objects.isNull(followUserMqDTO)) return;

        // 幂等性：通过联合唯一所以保证
        Long userId = followUserMqDTO.getUserId();
        Long followUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        //编程式提交事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 关注成功需往数据库添加两条记录
                // 关注表：一条记录
                int count = followingDOMapper.insert(FollowingDO.builder()
                        .userId(userId)
                        .followingUserId(followUserId)
                        .createTime(createTime)
                        .build());
                if (count > 0) {
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();   // 标记事务为回滚
                log.error("", e);
            }
            return false;
        }));

        // 若数据库操作成功，更新 Redis 中被关注用户的 ZSet 粉丝列表
        if (isSuccess) {
            // Lua 脚本
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);
        
            // 时间戳
            long timestamp = DateUtils.localDateTime2Timestamp(createTime);
            // 构建被关注用户的粉丝列表 Redis Key
            String fansRedisKey = RedisKeyConstants.buildUserFansKey(followUserId);
            // 执行 Lua 脚本
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timestamp);
            
            // 发送 MQ 消息，更新粉丝数计数
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(followUserId)
                    .type(FollowUnfollowTypeEnum.FOLLOW.getCode())
                    .build();
            
            // 发送 MQ
            sendMQ(countFollowUnfollowMqDTO);
        }

    }

    /**
     * 发送 MQ 到 计数服务
     * @param countFollowUnfollowMqDTO
     */
    private void sendMQ(CountFollowUnfollowMqDTO countFollowUnfollowMqDTO) {
        // 构建消息体对象，并将 DTO 转成 Json 字符串设置到消息体中
        org.springframework.messaging.Message<CountFollowUnfollowMqDTO> message = 
                MessageBuilder.withPayload(countFollowUnfollowMqDTO).build();
        
        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数】MQ 发送异常: ", throwable);
            }
        });

        // 发送 MQ 通知计数服务：统计粉丝数
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：粉丝数】MQ 发送成功，SendResult: {}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：粉丝数】MQ 发送异常: ", throwable);
            }
        });
    }
}
