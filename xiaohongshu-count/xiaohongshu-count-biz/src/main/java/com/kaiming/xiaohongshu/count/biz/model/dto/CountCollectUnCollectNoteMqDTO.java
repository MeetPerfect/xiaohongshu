package com.kaiming.xiaohongshu.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: CountCollectUnCollectNoteMqDTO
 * Package: com.kaiming.xiaohongshu.count.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 19:33
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountCollectUnCollectNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消收藏， 1：收藏
     */
    private Integer type;

    private LocalDateTime createTime;
}
