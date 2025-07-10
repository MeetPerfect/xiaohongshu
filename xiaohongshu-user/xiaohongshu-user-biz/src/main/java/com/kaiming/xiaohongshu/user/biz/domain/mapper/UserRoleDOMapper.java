package com.kaiming.xiaohongshu.user.biz.domain.mapper;


import com.kaiming.xiaohongshu.user.biz.domain.dataobject.UserRoleDO;

/**
 * ClassName: UserRoleDOMapper
 * Package: com.kaiming.xiaohongshu.auth.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 16:01
 * @Version 1.0
 */
public interface UserRoleDOMapper {

    int deleteByPrimaryKey(Long id);

    int insert(UserRoleDO record);

    int insertSelective(UserRoleDO record);

    UserRoleDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserRoleDO record);

    int updateByPrimaryKey(UserRoleDO record);
}
