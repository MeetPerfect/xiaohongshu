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

public class RedisKeyConstant {

    /**
     * Key 前缀：一级评论的 first_reply_comment_id 字段值是否更新标识
     */
    private static final String HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX = "comment:havaFirstReplyCommentId:";

    /**
     * 构建完整 KEY
     * @param commentId
     * @return
     */
    public static String buildHaveFirstReplyCommentKey(Long commentId) {
        return HAVE_FIRST_REPLY_COMMENT_KEY_PREFIX + commentId;
    }
}
