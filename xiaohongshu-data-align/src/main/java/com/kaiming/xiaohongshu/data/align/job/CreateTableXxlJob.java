package com.kaiming.xiaohongshu.data.align.job;

import com.kaiming.xiaohongshu.data.align.constant.TableConstants;
import com.kaiming.xiaohongshu.data.align.domain.mapper.CreateTableMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ClassName: CreateTableXxlJob
 * Package: com.kaiming.xiaohongshu.data.align.job
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/11 22:44
 * @Version 1.0
 */
@Component
public class CreateTableXxlJob {
    
    @Value("${table.shards}")
    private int tableShards;
    
    @Resource
    private CreateTableMapper createTableMapper;
    
    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("createTableJobHandler")
    public void createTableJobHandler() throws Exception {
        // 表后缀
        String date = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        XxlJobHelper.log("## 开始初始化明日增量数据表...");
        
        if (tableShards > 0) {
            for (int hashKey = 0; hashKey < tableShards; hashKey++) {
                // 表明后缀
                String tableNameSuffix = TableConstants.buildTableNameSuffix(date, hashKey);
                // 创建表
                createTableMapper.createDataAlignFollowingCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignFansCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserCollectCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignUserLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNoteLikeCountTempTable(tableNameSuffix);
                createTableMapper.createDataAlignNotePublishCountTempTable(tableNameSuffix);
            }
        }
        XxlJobHelper.log("## 结束创建日增量数据表，日期: {}...", date);
    }
}
