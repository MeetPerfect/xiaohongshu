package com.kaiming.xiaohongshu.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.count.biz.constant.MQConstants;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.NoteCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.domain.mapper.UserCountDOMapper;
import com.kaiming.xiaohongshu.count.biz.model.dto.AggregationCountCollectUnCollectNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

/**
 * ClassName: CountNoteCollect2DBConsumer
 * Package: com.kaiming.xiaohongshu.count.biz.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 19:52
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_" + MQConstants.TOPIC_COUNT_NOTE_COLLECT_2_DB,
        topic = MQConstants.TOPIC_COUNT_NOTE_COLLECT_2_DB)
@Slf4j
public class CountNoteCollect2DBConsumer implements RocketMQListener<String> {

    @Resource
    private NoteCountDOMapper noteCountDOMapper;
    private RateLimiter rateLimiter = RateLimiter.create(5000);
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(String body) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();

        log.info("## 消费到了 MQ 【计数: 笔记收藏数入库】, {}...", body);

//        Map<Long, Integer> countMap = null;
//        try {
//            countMap = JsonUtils.parseMap(body, Long.class, Integer.class);
//        } catch (Exception e) {
//            log.error("## 解析 JSON 字符串异常", e);
//        }
//        
        List<AggregationCountCollectUnCollectNoteMqDTO> countList = Lists.newArrayList();
        try {
            countList = JsonUtils.parseList(body, AggregationCountCollectUnCollectNoteMqDTO.class);
        } catch (Exception e) {
            log.error("解析字符串失败", e);
        }

        if (CollUtil.isNotEmpty(countList)) {
            // 判断数据库中 t_note_count 表，若笔记计数记录不存在，则插入；若记录已存在，则直接更新
            countList.forEach(item -> {
                // 笔记作者Id
                Long creatorId = item.getCreatorId();
                // 笔记Id
                Long noteId = item.getNoteId();
                // count
                Integer count = item.getCount();
                transactionTemplate.execute(status -> {
                    try {
                        noteCountDOMapper.insertOrUpdateCollectTotalByNoteId(count, noteId);
                        userCountDOMapper.insertOrUpdateCollectTotalByUserId(count, creatorId);
                    } catch (Exception ex) {
                        status.setRollbackOnly();
                        log.error("", ex);
                    }
                    return false;
                });
            });
        }
    }
}
