package com.kaiming.xiaohongshu.note.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: LikeUnlikeNoteMqDTO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo.dto
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/6 15:35
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeUnlikeNoteMqDTO {

    private Long userId;

    private Long noteId;

    /**
     * 0: 取消点赞， 1：点赞
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 笔记发布者 ID
     */
    private Long noteCreatorId;
}
