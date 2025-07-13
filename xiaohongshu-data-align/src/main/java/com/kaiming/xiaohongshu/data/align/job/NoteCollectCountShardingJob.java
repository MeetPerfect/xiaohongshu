package com.kaiming.xiaohongshu.data.align.job;

import cn.hutool.core.collection.CollUtil;
import com.kaiming.xiaohongshu.data.align.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.data.align.constant.TableConstants;
import com.kaiming.xiaohongshu.data.align.domain.mapper.DeleteMapper;
import com.kaiming.xiaohongshu.data.align.domain.mapper.SelectMapper;
import com.kaiming.xiaohongshu.data.align.domain.mapper.UpdateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ClassName: NoteCollectCountShardingJob
 * Package: com.kaiming.xiaohongshu.data.align.job
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/13 14:34
 * @Version 1.0
 */
@Component
@Slf4j
public class NoteCollectCountShardingJob {
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private UpdateMapper updateMapper;
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;
    @Resource
    private DeleteMapper deleteMapper;

    @XxlJob("noteCollectCountShardingJobHandler")
    public void noteCollectCountShardingJobHandler() {
        // 获取分片参数
        // 分片序号
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("=================> 开始定时分片广播任务：对当日发生变更的笔记点赞数进行对齐");
        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);

        // 当前日期
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 表名后缀
        String tableNameSuffix = TableConstants.buildTableNameSuffix(date, shardIndex);

        // 一批次 1000 条
        int batchSize = 1000;
        // 共对齐了多少条记录，默认为 0
        int processedTotal = 0;

        for (; ; ) {
            // 分批次查询 t_data_align_note_collect_count_temp_日期_分片序号，如一批次查询 1000 条，直到全部查询完成
            List<Long> noteIds = selectMapper.selectBatchFromDataAlignNoteCollectCountTempTable(tableNameSuffix, batchSize);

            if(CollUtil.isNotEmpty(noteIds)) break;
            
            noteIds.forEach(noteId -> {
                // 2: 循环这一批发生变更的笔记 ID， 对 t_note_collect 关注表执行 count(*) 操作，获取总数
                int collectTotal = selectMapper.selectCountFromNoteCollectionTableByUserId(noteId);
                // 3: 更新 t_note_count 表, 更新对应 Redis 缓存
                int count = updateMapper.updateNoteCollectTotalByUserId(noteId, collectTotal);

                if (count > 0) {
                    // 更新 t_note_count 表，并更新对应 Redis 缓存
                    String redisKey = RedisKeyConstants.buildCountNoteKey(noteId);  
                    // 判断 Hash 是否存在
                    Boolean hasKey = redisTemplate.hasKey(redisKey);
                    
                    if (hasKey) {
                        redisTemplate.opsForHash().put(redisKey, RedisKeyConstants.FIELD_COLLECT_TOTAL, collectTotal);
                    }
                }
            });
            // 4. 批量物理删除这一批次记录
            deleteMapper.batchDeleteDataAlignNoteCollectCountTempTable(tableNameSuffix, noteIds);

            // 当前已处理的记录数
            processedTotal += noteIds.size();
        }
        XxlJobHelper.log("=================> 结束定时分片广播任务：对当日发生变更的笔记收藏数进行对齐，共对齐记录数：{}", processedTotal);
    }
}
