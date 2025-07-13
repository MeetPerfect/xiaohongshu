package com.kaiming.xiaohongshu.data.align.consumer;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.data.align.constant.MQConstants;
import com.kaiming.xiaohongshu.data.align.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.data.align.constant.TableConstants;
import com.kaiming.xiaohongshu.data.align.domain.mapper.InsertMapper;
import com.kaiming.xiaohongshu.data.align.model.dto.CollectUnCollectNoteMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

/**
 * ClassName: TodayNoteCollectIncrementData2DBConsumer
 * Package: com.kaiming.xiaohongshu.data.align.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 14:36
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_data_align_" + MQConstants.TOPIC_COUNT_NOTE_COLLECT,
        topic = MQConstants.TOPIC_COUNT_NOTE_COLLECT)
@Slf4j
public class TodayNoteCollectIncrementData2DBConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private InsertMapper insertMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Value("${table.shards}")
    private int tableShards;
    
    @Override
    public void onMessage(String body) {
        log.info("## TodayNoteCollectIncrementData2DBConsumer 消费到了 MQ: {}", body);
        // 1. 布隆过滤器判断该日增量数据是否已经记录
        // 消息体 JSON 字符串转 DTO
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO = JsonUtils.parseObject(body, CollectUnCollectNoteMqDTO.class);

        if (Objects.isNull(collectUnCollectNoteMqDTO)) return;

        // 被收藏、取消收藏的笔记 ID
        Long noteId = collectUnCollectNoteMqDTO.getNoteId();
        // 笔记的发布者 ID
        Long noteCreatorId = collectUnCollectNoteMqDTO.getNoteCreatorId();
        // 今日日期
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String rbitmapKey = RedisKeyConstants.buildRBitmapUserNoteCollectListKey(date);
        // 1. rbitmap判断该日增量数据是否已经记录
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/rbitmap_today_note_collect_check.lua")));
        // 返回值类型
        script.setResultType(Long.class);

        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(rbitmapKey), noteId);
        // 2. 若无，才会落库，减轻数据库压力
        if (Objects.equals(result, 0L)) {
            // 根据分片总数，取模，分别获取对应的分片序号
            long userIdHashKey = noteCreatorId % tableShards;
            long noteIdHashKey = noteId % tableShards;
            // 将日增量变更数据，分别写入两张表
            // - t_data_align_note_collect_count_temp_日期_分片序号
            // - t_data_align_user_collect_count_temp_日期_分片序号
            transactionTemplate.execute(status -> {
                try {
                    insertMapper.insert2DataAlignNoteCollectCountTempTable(TableConstants.buildTableNameSuffix(date, noteIdHashKey), noteId);
                    insertMapper.insert2DataAlignUserCollectCountTempTable(TableConstants.buildTableNameSuffix(date, userIdHashKey), noteCreatorId);
                    
                    return true;
                } catch (Exception ex) {
                    status.setRollbackOnly(); // 标记事务为回滚
                    log.error("", ex);
                }
                return false;
            });
        }

        // 3. 数据库写入成功后，再添加布隆过滤器中
        RedisScript<Long> bloomAddScript = RedisScript.of("return redis.call('R.SETBIT', KEYS[1], ARGV[1])", Long.class);
        redisTemplate.execute(bloomAddScript, Collections.singletonList(rbitmapKey), noteId);
    }
}
