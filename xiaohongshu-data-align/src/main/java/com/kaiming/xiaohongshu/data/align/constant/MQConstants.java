package com.kaiming.xiaohongshu.data.align.constant;

/**
 * ClassName: MQConstants
 * Package: com.kaiming.xiaohongshu.data.align.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 11:24
 * @Version 1.0
 */

public interface MQConstants {

    /**
     * Topic: 计数 - 笔记点赞数
     */
    String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";

    /**
     * Topic: 计数 - 笔记收藏数
     */
    String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";

    /**
     * Topic: 笔记操作（发布、删除）
     */
    String TOPIC_NOTE_OPERATE = "NoteOperateTopic";
    
    /**
     * Topic: 关注数计数
     */
    String TOPIC_COUNT_FOLLOWING = "CountFollowingTopic";
}
