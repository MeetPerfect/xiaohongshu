package com.kaiming.xiaohongshu.user.biz.domain.mapper;


import com.kaiming.xiaohongshu.user.biz.domain.dataobject.PermissionDO;

import java.util.List;

/**
 * ClassName: PermissionDOMapper
 * Package: com.kaiming.xiaohongshu.auth.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 16:06
 * @Version 1.0
 */
public interface PermissionDOMapper {

    int deleteByPrimaryKey(Long id);

    int insert(PermissionDO record);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);

    int updateByPrimaryKey(PermissionDO record);

    /**
     * 查询所有启用的权限
     * @return
     */
    List<PermissionDO> selectAppEnabledList();
}
