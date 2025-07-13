package com.kaiming.xiaohongshu.data.align.domain.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * ClassName: UpdateMapper
 * Package: com.kaiming.xiaohongshu.data.align.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 20:42
 * @Version 1.0
 */
public interface UpdateMapper {

    /**
     * 更新 t_user_count 计数表总关注数
     * @param userId
     * @return
     */
    int updateUserFollowingTotalByUserId(@Param("userId") long userId,
                                         @Param("followingTotal") int followingTotal);

    /**
     * 更新 t_note_count 计数表笔记点赞数
     */
    int updateNoteLikeTotalByUserId(@Param("noteId") long noteId,
                                    @Param("noteLikeTotal") int noteLikeTotal);

    /**
     * 更新 t_note_count 计数表笔记收藏数
     * @param noteId
     * @param collectTotal
     */
    int updateNoteCollectTotalByUserId(@Param("noteId") Long noteId, @Param("noteCollectTotal") int collectTotal);

    /**
     * 更新 t_user_count 计数表总粉丝数
     * @param userId
     * @param fansTotal
     * @return
     */
    int updateUserFansTotalByUserId(@Param("userId") Long userId, @Param("fansTotal") int fansTotal);

    /**
     * 更新 t_user_count 计数表总点赞数
     * @param userId
     * @param likeTotal
     * @return
     */
    int updateUserLikeTotalByUserId(@Param("userId") Long userId, @Param("likeTotal") int likeTotal);

    /**
     * 更新 t_user_count 计数表总收藏数
     * @param userId
     * @param collectTotal
     * @return
     */
    int updateUserCollectTotalByUserId(@Param("userId") Long userId, @Param("collectTotal") int collectTotal);

    /**
     * 更新 t_user_count 计数表总笔记数
     * @param userId
     * @param noteTotal
     * @return
     */
    int updateUserNoteTotalByUserId(@Param("userId")Long userId, @Param("noteTotal") int noteTotal);
}
