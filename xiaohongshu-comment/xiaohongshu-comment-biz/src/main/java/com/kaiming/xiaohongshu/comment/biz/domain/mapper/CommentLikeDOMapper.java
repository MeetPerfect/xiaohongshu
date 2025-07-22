package com.kaiming.xiaohongshu.comment.biz.domain.mapper;

import com.kaiming.xiaohongshu.comment.biz.domain.dataobject.CommentLikeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentLikeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentLikeDO record);

    int insertSelective(CommentLikeDO record);

    CommentLikeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentLikeDO record);

    int updateByPrimaryKey(CommentLikeDO record);

    /**
     * 查询评论是否点赞
     * @param userId
     * @param commentId
     * @return
     */
    int selectCountByUserIdAndCommentId(@Param("userId") Long userId,
                                        @Param("commentId") Long commentId);

    /**
     * 查询对应用户点赞的所有评论
     * @param userId
     * @return
     */
    List<CommentLikeDO> selectByUserId(@Param("userId") Long userId);
}