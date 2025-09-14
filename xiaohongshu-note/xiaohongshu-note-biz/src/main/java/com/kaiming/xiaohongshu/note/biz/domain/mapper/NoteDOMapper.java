package com.kaiming.xiaohongshu.note.biz.domain.mapper;

import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteDO record);

    int insertSelective(NoteDO record);

    NoteDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteDO record);

    int updateByPrimaryKey(NoteDO record);

    /**
     * 笔记设置仅自己可见
     *
     * @param noteDO
     * @return
     */
    int updateVisibleOnlyMe(NoteDO noteDO);

    /**
     * 更新笔记置顶状态
     *
     * @param noteDO
     * @return
     */
    int updateIsTop(NoteDO noteDO);

    /**
     * 根据笔记ID查询数量
     *
     * @param noteId
     * @return
     */
    int selectCountByNoteId(Long noteId);

    /**
     * 查询笔记的发布者用户 Id
     *
     * @param noteId
     * @return
     */
    Long selectCreatorIdByNoteId(Long noteId);

    /**
     * 查询个人主页已发布笔记列表
     *
     * @param creatorId
     * @param cursor
     * @return
     */
    List<NoteDO> selectPublishedNoteListByUserIdAndCursor(@Param("creatorId") Long creatorId, @Param("cursor") Long cursor);

    /**
     * 根据频道Id查询笔记个数
     *
     * @param channel
     * @return
     */
    int selectTotalCount(Long channel);

    /**
     * 根据频道Id分页查询笔记
     *
     * @param channelId
     * @param offset
     * @param pageSize
     * @return
     */
    List<NoteDO> selectPageList(@Param("channelId") Long channelId, @Param("offset") long offset, @Param("pageSize") long pageSize);

    /**
     * 查询作者发布的笔记数
     *
     * @param creatorId
     * @return
     */
    int selectTotalCountByCreatorId(Long creatorId);


    /**
     * 分页查询用户笔记
     * @param userId
     * @param offset
     * @param pageSize
     * @return
     */
    List<NoteDO> selectPageListByCreatorId(@Param("userId") Long userId, @Param("offset") long offset, @Param("pageSize") long pageSize);

    /**
     * 根据笔记Ids集合批量查询笔记
     * @param noteIds
     * @return
     */
    List<NoteDO> selectByNoteIds(List<Long> noteIds);
}