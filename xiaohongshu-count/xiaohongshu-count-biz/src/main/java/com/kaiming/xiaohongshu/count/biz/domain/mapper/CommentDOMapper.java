package com.kaiming.xiaohongshu.count.biz.domain.mapper;

import com.kaiming.xiaohongshu.count.biz.domain.dataobject.CommentDO;
import org.apache.ibatis.annotations.Param;

public interface CommentDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentDO record);

    int insertSelective(CommentDO record);

    CommentDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentDO record);

    int updateByPrimaryKey(CommentDO record);

    /**
     * 更新一级评论的子评论总数
     * @param parentId
     * @param count
     * @return
     */
    int updateChildCommentTotal(@Param("parentId") Long parentId, @Param("count") int count);

    /**
     * 评论点赞数更新
     * @param count
     * @param commentId
     * @return
     */
    int updateLikeTotalByCommentId(@Param("count") Integer count,
                                   @Param("commentId") Long commentId);
}