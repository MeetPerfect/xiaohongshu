package com.kaiming.xiaohongshu.note.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: CollectUnCollectNoteDTO
 * Package: com.kaiming.xiaohongshu.note.biz.model.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/9 14:42
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectUnCollectNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消收藏， 1：收藏
     */
    private Integer type;

    private LocalDateTime createTime;

    /**
     * 笔记发布者 ID
     */
    private Long noteCreatorId;
}
