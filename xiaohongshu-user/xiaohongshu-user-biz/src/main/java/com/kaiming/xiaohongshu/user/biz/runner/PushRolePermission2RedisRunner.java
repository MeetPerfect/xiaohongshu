package com.kaiming.xiaohongshu.user.biz.runner;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kaiming.framework.common.util.JsonUtils;
import com.kaiming.xiaohongshu.user.biz.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.PermissionDO;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.RoleDO;
import com.kaiming.xiaohongshu.user.biz.domain.dataobject.RolePermissionDO;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.PermissionDOMapper;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.RoleDOMapper;
import com.kaiming.xiaohongshu.user.biz.domain.mapper.RolePermissionDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: PushRolePermission2RedisRunner
 * Package: com.kaiming.xiaohongshu.auth.runner
 * Description: 统一放置项目启动时的逻辑类
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 20:18
 * @Version 1.0
 */
@Component
@Slf4j
public class PushRolePermission2RedisRunner implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private RolePermissionDOMapper rolePermissionDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;
    // 权限同步标记 Key
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";


    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            // 是否能够同步数据: 原子操作，只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 天
            boolean canPushed = redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS);
            
            if(!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }
            
            // 1. 查询所有的角色
            List<RoleDO> roleDOS = roleDOMapper.selectEnabledList();
            if (CollUtil.isNotEmpty(roleDOS)) {
                // 获取所有角色id
                List<Long> roleIds = roleDOS.stream().map(RoleDO::getId).toList();
    
                // 2. 查询所有的角色权限
                List<RolePermissionDO> rolePermissionDOS = rolePermissionDOMapper.selectByRoleIds(roleIds);
                // 按角色ID分组，每个角色ID对应多个权限ID
                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionDOS.stream().collect(
                        Collectors.groupingBy(RolePermissionDO::getRoleId,
                                Collectors.mapping(RolePermissionDO::getPermissionId, Collectors.toList()))
                );
                // 查询APP所有被启动的权限
                List<PermissionDO> permissionDOS = permissionDOMapper.selectAppEnabledList();
                
                // 权限 ID - 权限 DO对象
                Map<Long, PermissionDO> permissionIdDOMap = permissionDOS.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );
    
                // 组织 角色ID-权限 的关系
                Map<String, List<String>> roleKeyPermissionsMap = Maps.newHashMap();
    
                // 循环所有角色
                roleDOS.forEach(roleDO -> {
                    // 当前角色Id
                    Long roleId = roleDO.getId();
                    // 当前角色roleKey
                    String roleKey = roleDO.getRoleKey();
                    // 当前角的多色id对应个权限id
                    List<Long> permissionIds = roleIdPermissionIdsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<String> permissionKeys  = Lists.newArrayList();
                        permissionIds.forEach(permissionId -> {
                            PermissionDO permissionDO = permissionIdDOMap.get(permissionId);
                            permissionKeys.add(permissionDO.getPermissionKey());
                        });
                        roleKeyPermissionsMap.put(roleKey, permissionKeys);
                    }
                });
                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roleKeyPermissionsMap.forEach((roleKey, permissions) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissions));
                });
            }

            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }
    }
}
