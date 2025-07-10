package com.kaiming.xiaohongshu.user.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: UserRoleDO
 * Package: com.kaiming.xiaohongshu.auth.domain.dataobject
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 15:54
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleDO {
    private Long id;
    private Long userId;
    private Long roleId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isDeleted;
    
}
