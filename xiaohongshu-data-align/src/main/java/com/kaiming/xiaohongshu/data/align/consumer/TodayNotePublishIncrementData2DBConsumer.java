package com.kaiming.xiaohongshu.data.align.consumer;

import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.data.align.constant.MQConstants;
import com.kaiming.xiaohongshu.data.align.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.data.align.constant.TableConstants;
import com.kaiming.xiaohongshu.data.align.domain.mapper.InsertRecordMapper;
import com.kaiming.xiaohongshu.data.align.model.dto.NoteOperateMqDTO;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

/**
 * ClassName: TodayNotePublishIncrementData2DBConsumer
 * Package: com.kaiming.xiaohongshu.data.align.consumer
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 19:13
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(consumerGroup = "xiaohongshu_group_data_align_" + MQConstants.TOPIC_NOTE_OPERATE,
        topic = MQConstants.TOPIC_NOTE_OPERATE)
@Slf4j
public class TodayNotePublishIncrementData2DBConsumer implements RocketMQListener<String> {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${table.shards}")
    private int tableShards;
    @Resource
    private InsertRecordMapper insertRecordMapper;
    
    @Override
    public void onMessage(String body) {
        log.info("## TodayNotePublishIncrementData2DBConsumer 消费到了 MQ: {}", body);
        // 1. 布隆过滤器判断该日增量数据是否已经记录

        NoteOperateMqDTO noteOperateMqDTO = JsonUtils.parseObject(body, NoteOperateMqDTO.class);

        if (Objects.isNull(noteOperateMqDTO)) return;
        
        // 发布笔记作者Id
        Long noteCreatorId = noteOperateMqDTO.getCreatorId();
        // 笔记Id
        Long noteId = noteOperateMqDTO.getNoteId();
        // 今日日期
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String bloomKey = RedisKeyConstants.buildBloomUserNoteOperateListKey(date);
        // 脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_today_user_note_publish_check.lua")));
        script.setResultType(Long.class);
        // 执行 Lua 脚本，拿到返回结果
        Long result = redisTemplate.execute(script, Collections.singletonList(bloomKey), noteId);
        
        if (Objects.equals(result, 0L)) {
            // 2. 若无，才会落库，减轻数据库压力
            // 将日增量变更数据，写入日增量表中
            // - t_data_align_note_publish_count_temp_日期_分片序号
            // 根据分片总数，取模，分别获取对应的分片序号
            long userIdHashKey = noteCreatorId % tableShards;
            
            insertRecordMapper.insert2DataAlignUserNotePublishCountTempTable(TableConstants.buildTableNameSuffix(date, userIdHashKey), noteCreatorId);
            
            // TODO: 3. 数据库写入成功后，再添加布隆过滤器中
            RedisScript<Long> bloomAddScript  = RedisScript.of("return redis.call('BF.ADD', KEYS[1], ARGV[1]')", Long.class);
            redisTemplate.execute(bloomAddScript, Collections.singletonList(bloomKey), noteId);
        }

    }
}
