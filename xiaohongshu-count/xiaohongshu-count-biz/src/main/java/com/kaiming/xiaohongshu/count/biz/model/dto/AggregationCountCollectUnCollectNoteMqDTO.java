package com.kaiming.xiaohongshu.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AggregationCountCollectUnCollectNoteMqDTO
 * Package: com.kaiming.xiaohongshu.count.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/8/31 21:31
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AggregationCountCollectUnCollectNoteMqDTO {

    /**
     * 发布者笔记Id
     */
    private Long creatorId;

    /**
     * 笔记Id
     */
    private Long noteId;

    /**
     * 聚合后数
     */
    private Integer count;
}
