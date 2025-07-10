package com.kaiming.xiaohongshu.note.biz.consumer;

import com.kaiming.xiaohongshu.note.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * ClassName: DeleteNoteLocalCacheConsumer
 * Package: com.kaiming.xiaohongshu.note.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/26 21:47
 * @Version 1.0
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group" + MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE,   // Group
        topic = MQConstants.TOPIC_DELETE_NOTE_LOCAL_CACHE,      // 主题
        messageModel = MessageModel.BROADCASTING)               // 广播模式
public class DeleteNoteLocalCacheConsumer implements RocketMQListener<String> {
    
    @Resource
    private NoteService noteService;
    @Override
    public void onMessage(String body) {
        Long noteId = Long.valueOf(body);
        log.info("## 消费者消费成功, noteId: {}", noteId);
        noteService.deleteNoteLocalCache(noteId);
    }
}
