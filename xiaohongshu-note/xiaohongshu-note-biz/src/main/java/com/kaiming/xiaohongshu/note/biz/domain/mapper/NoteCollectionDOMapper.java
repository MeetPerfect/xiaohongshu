package com.kaiming.xiaohongshu.note.biz.domain.mapper;

import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteCollectionDO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteCollectionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCollectionDO record);

    int insertSelective(NoteCollectionDO record);

    NoteCollectionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCollectionDO record);

    int updateByPrimaryKey(NoteCollectionDO record);

    /**
     * 查询笔记时候被收藏
     * @param userId
     * @param noteId
     * @return
     */
    int selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户收藏的笔记
     * @param userId
     * @return
     */
    List<NoteCollectionDO> selectByUserId(Long userId);

    /**
     * 查询笔记是否已经被收藏
     * @param userId
     * @param noteId
     * @return
     */
    int selectNoteIsCollected(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询用户最忌收藏的笔记
     * @param userId
     * @param limit
     * @return
     */
    List<NoteCollectionDO> selectCollectedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 插入或更新笔记收藏记录
     * @param noteCollectionDO
     * @return
     */
    int insertOrUpdate(NoteCollectionDO noteCollectionDO);

    /**
     * 更新笔记收藏状态为未收藏
     * @param noteCollectionDO
     * @return
     */
    int update2UnCollectByUserIdAndNoteId(NoteCollectionDO noteCollectionDO);

    /**
     * 根据用户Id查询用户收藏数
     * @param userId
     * @return
     */
    int selectTotalCountByUserId(Long userId);


    /**
     * 根据用户Id分页查询用户收藏笔记Ids
     * @param creator
     * @param offset
     * @param pageSize
     * @return
     */
    List<Long> selectPageListByUserId(@Param("userId") Long creator, @Param("offset") long offset, @Param("pageSize") long pageSize);
}