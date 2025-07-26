package com.kaiming.xiaohongshu.kv.biz.consumer;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.kv.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.kv.biz.model.dto.PublishNoteDTO;
import com.kaiming.xiaohongshu.kv.biz.service.NoteContentService;
import com.kaiming.xiaohongshu.kv.dto.req.AddNoteContentReqDTO;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * ClassName: SaveNoteContentConsumer
 * Package: com.kaiming.xiaohongshu.kv.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/26 17:52
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION,
        topic = MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION)
@Slf4j
public class SaveNoteContentConsumer implements RocketMQListener<Message> {
    @Resource
    private NoteContentService noteContentService;
    
    @Override
    public void onMessage(Message message) {
        String bodyJsonStr = new String(message.getBody());
        log.info("## SaveNoteContentConsumer 消费了事务消息 {}", bodyJsonStr);
        // 笔记正文保存到 Cassandra 中
        
        if (StringUtils.isNotBlank(bodyJsonStr)) {
            PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(bodyJsonStr, PublishNoteDTO.class);

            String content = publishNoteDTO.getContent();
            String uuid = publishNoteDTO.getContentUuid();

            noteContentService.addNoteContent(AddNoteContentReqDTO.builder()
                    .uuid(uuid)
                    .content(content)
                    .build());
        }
    }
}
