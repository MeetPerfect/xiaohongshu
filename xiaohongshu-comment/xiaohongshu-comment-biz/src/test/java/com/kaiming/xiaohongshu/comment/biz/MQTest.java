package com.kaiming.xiaohongshu.comment.biz;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * ClassName: MQTest
 * Package: com.kaiming.xiaohongshu.comment.biz
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/18 20:13
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
public class MQTest {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void test() {
        for (long i = 0; i < 1620; i++) {

            // 构建消息对象
            Message<String> message = MessageBuilder.withPayload("消息体数据")
                    .build();
            // 异步发送 MQ 消息
            rocketMQTemplate.asyncSend("PublishCommentTopic", message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("==> 【评论发布】MQ 发送成功，SendResult: {}", sendResult);
                }

                @Override
                public void onException(Throwable throwable) {
                    log.error("==> 【评论发布】MQ 发送异常: ", throwable);
                }
            });
        }
    }

}
