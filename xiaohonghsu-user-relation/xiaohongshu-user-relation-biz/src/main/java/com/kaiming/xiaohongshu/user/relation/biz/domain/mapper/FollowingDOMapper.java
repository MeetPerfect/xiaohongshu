package com.kaiming.xiaohongshu.user.relation.biz.domain.mapper;

import com.kaiming.xiaohongshu.user.relation.biz.domain.dataobject.FollowingDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowingDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingDO record);

    int insertSelective(FollowingDO record);

    FollowingDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingDO record);

    int updateByPrimaryKey(FollowingDO record);
    
    List<FollowingDO> selectByUserId(Long userId);

    /**
     * 根据用户ID和关注的用户ID删除关注关系
     * @param userId
     * @param unfollowUserId
     * @return
     */
    int deleteByUserIdAndFollowingUserId(@Param("userId") Long userId, @Param("unfollowUserId") Long unfollowUserId);

    /**
     * 根据用户ID查询关注的用户列表记录总数
     * @param userId
     * @return
     */
    long selectCountByUserId(Long userId);

    /**
     * 分页查询用户关注的用户列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FollowingDO> selectPageListByUserId(@Param("userId") long userId, 
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);

    /**
     * 查询关注用户列表
     * @param userId
     * @return
     */
    List<FollowingDO> selectAllByUserId(Long userId);
}