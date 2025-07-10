package com.kaiming.xiaohongshu.note.biz.domain.mapper;

import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteDO;
import com.kaiming.xiaohongshu.note.biz.domain.dataobject.NoteLikeDO;

public interface NoteDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteDO record);

    int insertSelective(NoteDO record);

    NoteDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteDO record);

    int updateByPrimaryKey(NoteDO record);

    /**
     * 笔记设置仅自己可见
     * @param noteDO
     * @return
     */
    int updateVisibleOnlyMe(NoteDO noteDO);

    /**
     * 更新笔记置顶状态
     * @param noteDO
     * @return
     */
    int updateIsTop(NoteDO noteDO);

    /**
     * 根据笔记ID查询数量
     * @param noteId
     * @return
     */
    int selectCountByNoteId(Long noteId);

    /**
     * 查询笔记的发布者用户 Id
     * @param noteId
     * @return
     */
    Long selectCreatorIdByNoteId(Long noteId);
}