package com.kaiming.xiaohongshu.user.biz.domain.mapper;

import com.kaiming.xiaohongshu.user.biz.domain.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;


import java.util.List;

public interface UserDOMapper {
    int deleteByPrimaryKey(Long id);

    /**
     * 插入用户信息
     * @param record
     * @return
     */
    int insert(UserDO record);
    
    int insertSelective(UserDO record);

    /**
     * 根据主键查询用户信息
     * @param id
     * @return
     */
    UserDO selectByPrimaryKey(Long id);

    /**
     * 更新用户信息
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(UserDO record);
    
    /**
     * 更新用户信息
     * @param record
     * @return
     */
    int updateByPrimaryKey(UserDO record);

    /**
     * 根据手机号查询用户信息
     * @param phone
     * @return
     */
    UserDO selectByPhone(String phone);

    /**
     * 根据用户ID列表查询用户信息
     * @param ids
     * @return
     */
    List<UserDO> selectByIds(@Param("ids") List<Long> ids);
}