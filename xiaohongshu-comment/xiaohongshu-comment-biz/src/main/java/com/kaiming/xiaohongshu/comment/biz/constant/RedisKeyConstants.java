package com.kaiming.xiaohongshu.comment.biz.constant;

/**
 * ClassName: RedisConstant
 * Package: com.kaiming.xiaohongshu.comment.biz.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/20 10:55
 * @Version 1.0
 */

public class RedisKeyConstants {

    /**
     * Key 前缀：一级评论的 first_reply_comment_id 字段值是否更新标识
     */
    private static final String HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX = "comment:havaFirstReplyCommentId:";

    /**
     * Key 前缀：笔记评论总数
     */
    private static final String COUNT_COMMENT_TOTAL_KEY_PREFIX = "count:note:";

    /**
     * Hash Field 键：评论总数
     */
    public static final String FIELD_COMMENT_TOTAL = "commentTotal";

    /**
     * Key 前缀：评论分页 ZSET
     */
    private static final String COMMENT_LIST_KEY_PREFIX = "comment:list:";

    /**
     * Key 前缀：评论详情 JSON
     */
    private static final String COMMENT_DETAIL_KEY_PREFIX = "comment:detail:";
    
    /**
     * 构建完整 KEY
     * @param commentId
     * @return
     */
    public static String buildHaveFirstReplyCommentKey(Long commentId) {
        return HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX + commentId;
    }

    /**
     * 构建笔记评论总数完整 KEY
     * @param noteId
     * @return
     */
    public static String buildNoteCommentTotalKey(Long noteId) {
        return COUNT_COMMENT_TOTAL_KEY_PREFIX + noteId;
    }

    /**
     * 构建评论分页 ZSET 完整 KEY
     * @param noteId
     * @return
     */
    public static String buildCommentListKey(Long noteId) {
        return COMMENT_LIST_KEY_PREFIX + noteId;
    }

    /**
     * 构建评论详情完整 KEY
     * @param commentId
     * @return
     */
    public static String buildCommentDetailKey(Object commentId) {
        return COMMENT_DETAIL_KEY_PREFIX + commentId;
    }
}
