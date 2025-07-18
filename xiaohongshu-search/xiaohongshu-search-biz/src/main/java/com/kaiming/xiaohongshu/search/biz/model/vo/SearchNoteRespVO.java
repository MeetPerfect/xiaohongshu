package com.kaiming.xiaohongshu.search.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
* ClassName: SearchNoteRespVO
* Package: com.kaiming.xiaohongshu.search.model.vo
* Description:
* @Auther gongkaiming
* @Create 2025/7/16 14:15
* @Version 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchNoteRespVO {

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 封面
     */
    private String cover;

    /**
     * 标题
     */
    private String title;

    /**
     * 标题：关键词高亮
     */
    private String highlightTitle;

    /**
     * 发布者头像
     */
    private String avatar;

    /**
     * 发布者昵称
     */
    private String nickname;

    /**
     * 最后一次编辑时间
     */
    private String updateTime;

    /**
     * 被点赞总数
     */
    private String likeTotal;
    /**
     * 被评论数
     */
    private String commentTotal;

    /**
     * 被收藏数
     */
    private String collectTotal;

}
