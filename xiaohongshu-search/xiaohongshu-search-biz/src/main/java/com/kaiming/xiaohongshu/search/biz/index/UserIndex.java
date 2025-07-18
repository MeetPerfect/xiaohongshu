package com.kaiming.xiaohongshu.search.biz.index;

/**
 * ClassName: UserIndex
 * Package: com.kaiming.xiaohongshu.search.index
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 13:03
 * @Version 1.0
 */
public class UserIndex {

    /**
     * 索引名称
     */
    public static final String NAME = "user";

    /**
     * 用户ID
     */
    public static final String FIELD_USER_ID = "id";

    /**
     * 昵称
     */
    public static final String FIELD_USER_NICKNAME = "nickname";

    /**
     * 头像
     */
    public static final String FIELD_USER_AVATAR = "avatar";

    /**
     * 小哈书ID
     */
    public static final String FIELD_USER_XIAOHONGSHU_ID = "xiaohongshu_id";

    /**
     * 发布笔记总数
     */
    public static final String FIELD_USER_NOTE_TOTAL = "note_total";

    /**
     * 粉丝总数
     */
    public static final String FIELD_USER_FANS_TOTAL = "fans_total";

}
