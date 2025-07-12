package com.kaiming.xiaohongshu.data.align.constant;

/**
 * ClassName: RedisConstants
 * Package: com.kaiming.xiaohongshu.data.align.constant
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 12:31
 * @Version 1.0
 */
public class RedisKeyConstants {

    /**
     * 布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞 前缀
     */
    public static final String BLOOM_TODAY_NOTE_LIKE_LIST_KEY = "bloom:dataAlign:note:likes:";

    /**
     * rbitmap：日增量变更数据，用户笔记点赞，取消点赞 前缀
     */
    public static final String R_BITMAP_TODAY_NOTE_LIKE_LIST_KEY = "rbitmap:dataAlign:note:likes:";

    /**
     * 布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏 前缀
     */
    public static final String BLOOM_TODAY_NOTE_COLLECT_LIST_KEY = "bloom:dataAlign:note:collects:";

    /**
     * rbitmap：日增量变更数据，用户笔记收藏，取消收藏 前缀
     */
    public static final String R_BITMAP_TODAY_NOTE_COLLECT_LIST_KEY = "rbitmap:dataAlign:note:collects:";

    /**
     * 布隆过滤器：日增量变更数据，用户笔记发布，删除 前缀
     */
    public static final String BLOOM_TODAY_USER_NOTE_OPERATOR_LIST_KEY = "bloom:dataAlign:user:note:operators:";

    /**
     * 布隆过滤器：日增量变更数据，用户关注数 前缀
     */
    public static final String BLOOM_TODAY_USER_FOLLOW_LIST_KEY = "bloom:dataAlign:user:follows:";

    /**
     * 布隆过滤器：日增量变更数据，用户粉丝数 前缀
     */
    public static final String BLOOM_TODAY_USER_FANS_LIST_KEY = "bloom:dataAlign:user:fans:";
    
    /**
     * 构建完整的布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞 KEY
     * @param date
     * @return
     */
    public static String buildBloomUserNoteLikeListKey(String date) {
        return BLOOM_TODAY_NOTE_LIKE_LIST_KEY + date;
    }

    /**
     * 构建完整的rbitmap：日增量变更数据，用户笔记点赞，取消点赞 KEY
     * @param date
     * @return
     */
    public static String buildRBitmapUserNoteLikeListKey(String date) {
        return R_BITMAP_TODAY_NOTE_LIKE_LIST_KEY + date;
    }

    /**
     * 构建完整的布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏 KEY
     * @param date
     * @return
     */
    public static String buildBloomUserNoteCollectListKey(String date) {
        return BLOOM_TODAY_NOTE_COLLECT_LIST_KEY + date;
    }

    /**
     * 构建完整的rbitmap：日增量变更数据，用户笔记收藏，取消收藏 KEY
     * @param date
     * @return
     */
    public static String buildRBitmapUserNoteCollectListKey(String date) {
        return R_BITMAP_TODAY_NOTE_COLLECT_LIST_KEY + date;
    }

    /**
     * 构建完整的布隆过滤器：日增量变更数据，用户笔记发布，删除 KEY
     * @param date
     * @return
     */
    public static String buildBloomUserNoteOperateListKey(String date) {
        return BLOOM_TODAY_USER_NOTE_OPERATOR_LIST_KEY + date;
    }

    /**
     * 构建完整的布隆过滤器：日增量变更数据，用户关注数 KEY
     * @param date
     * @return
     */
    public static String buildBloomUserFollowListKey(String date) {
        return BLOOM_TODAY_USER_FOLLOW_LIST_KEY + date;
    }

    /**
     * 构建完整的布隆过滤器：日增量变更数据，用户粉丝数 KEY
     * @param date
     * @return
     */
    public static String buildBloomUserFansListKey(String date) {
        return BLOOM_TODAY_USER_FANS_LIST_KEY + date;
    }
}
