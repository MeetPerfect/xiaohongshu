package com.kaiming.xiaohongshu.user.biz.domain.mapper;


import com.kaiming.xiaohongshu.user.biz.domain.dataobject.RoleDO;

import java.util.List;

/**
 * ClassName: RoleDOMapper
 * Package: com.kaiming.xiaohongshu.auth.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 16:02
 * @Version 1.0
 */
public interface RoleDOMapper {
    /**
     * 根据id删除角色
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Long id);
    /**
     * 插入角色
     * @param record
     * @return
     */
    int insert(RoleDO record);
    
    int insertSelective(RoleDO record);
    
    
    RoleDO selectByPrimaryKey(Long id);

    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(RoleDO record);
    /**
     * 
     * @param record
     * @return
     */
    int updateByPrimaryKey(RoleDO record);

    /**
     * 查询所有启用的角色
     * @return
     */
    List<RoleDO> selectEnabledList();
}
