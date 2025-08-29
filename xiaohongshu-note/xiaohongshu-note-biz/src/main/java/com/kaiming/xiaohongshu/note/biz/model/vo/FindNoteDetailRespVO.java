package com.kaiming.xiaohongshu.note.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: FindNoteDetailRespVO
 * Package: com.kaiming.xiaohongshu.note.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/15 14:11
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteDetailRespVO {
    
    private Long id;
    
    private Integer type;

    private String title;

    private String content;

    private List<String> imgUris;

    private Long topicId;

    /**
     * 话题集合
     */
    private List<FindTopicRespVO> topics;

    private String topicName;

    private Long creatorId;

    private String creatorName;

    private String avatar;

    private String videoUri;

    /**
     * 编辑时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否可见
     */
    private Integer visible;

    /**
     * 点赞总数
     */
    private String LikeTotal;

    /**
     * 收藏总数
     */
    private String CollectTotal;

    /**
     * 评论总数
     */
    private String CommentTotal;
    
}
