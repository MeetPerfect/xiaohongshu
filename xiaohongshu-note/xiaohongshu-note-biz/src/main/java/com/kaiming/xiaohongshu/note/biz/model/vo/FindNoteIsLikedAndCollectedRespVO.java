package com.kaiming.xiaohongshu.note.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindNoteIsLikedAndCollectedRespVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/6 15:06
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteIsLikedAndCollectedRespVO {
    /**
     * 笔记 ID
     */
    private Long noteId;
    /**
     * 是否点赞
     */
    private Boolean isLiked;
    /**
     * 时候收藏
     */
    private Boolean isCollected;

}
