package com.kaiming.xiaohongshu.user.relation.biz;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.user.relation.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.user.relation.biz.enums.FollowUnfollowTypeEnum;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.CountFollowUnfollowMqDTO;
import com.kaiming.xiaohongshu.user.relation.biz.model.dto.FollowUserMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;

/**
 * ClassName: MQTest
 * Package: com.kaiming.xiaohongshu.user.relation.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/28 18:28
 * @Version 1.0
 */
//@SpringBootTest
//@Slf4j
//public class MQTest {
//    @Resource
//    private RocketMQTemplate rocketMQTemplate;
//
//    @Test
//    public void test1() {
//        for (long i = 0; i < 10000; i++) {
//            // 构建消息体
//            FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
//                    .userId(i)
//                    .followUserId(i)
//                    .createTime(LocalDateTime.now())
//                    .build();
//            // 构建消息对象，并将 DTO 转为 JSON 字符串 设置到消息题中
//            Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO)).build();
//
//            // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
//            String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;
//            log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);
//            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    log.info("==> MQ 发送成功，SendResult: {}", sendResult);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    log.error("==> MQ 发送异常: ", throwable);
//                }
//            });
//        }
//    }
//
//    @Test
//    public void testSendCountFollowUnfollowMQ() {
//        for (long i = 0; i < 3200; i++) {
//            // 构建消息体
//            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO = CountFollowUnfollowMqDTO.builder()
//                    .userId(i + 1)
//                    .targetUserId(3L)
//                    .type(FollowUnfollowTypeEnum.FOLLOW.getCode())
//                    .build();
//
//            Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countFollowUnfollowMqDTO)).build();
//
//            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS, message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    log.info("==> 【计数服务：粉丝数】MQ 发送成功，SendResult: {}", sendResult);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    log.error("==> 【计数服务：粉丝数】MQ 发送异常: ", throwable);
//                }
//            });
//            
//            rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING, message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    log.info("==> 【计数服务：关注数】MQ 发送成功，SendResult: {}", sendResult);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    log.error("==> 【计数服务：关注数】MQ 发送异常: ", throwable);
//                }
//            });
//        }
//    }
//}
