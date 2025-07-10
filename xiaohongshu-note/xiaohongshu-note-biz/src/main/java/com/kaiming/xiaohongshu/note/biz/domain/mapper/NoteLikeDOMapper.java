package com.kaiming.xiaohongshu.note.biz.domain.mapper;

import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteLikeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteLikeDO record);

    int insertSelective(NoteLikeDO record);

    NoteLikeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteLikeDO record);

    int updateByPrimaryKey(NoteLikeDO record);

    /**
     * 查询用户对笔记的点赞记录
     *
     * @param userId
     * @param noteId
     * @return
     */
    int selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户点赞的笔记列表
     *
     * @param userId
     * @return
     */
    List<NoteLikeDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户是否点赞过某个笔记
     *
     * @param userId
     * @param noteId
     * @return
     */
    int selectNoteIsLiked(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户点赞的笔记列表，限制数量
     *
     * @param userId
     * @param limit
     * @return
     */
    List<NoteLikeDO> selectLikedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 新增笔记点赞记录，若已存在，则更新笔记点赞记录
     *
     * @param noteLikeDO
     * @return
     */
    int insertOrUpdate(NoteLikeDO noteLikeDO);

    /**
     * 取消点赞
     * @param noteLikeDO
     * @return
     */
    int update2UnlikeByUserIdAndNoteId(NoteLikeDO noteLikeDO);
}