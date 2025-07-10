package com.kaiming.xiaohongshu.user.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: RoleDO
 * Package: com.kaiming.xiaohongshu.auth.domain.dataobject
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 15:57
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDO {
    
    private Long id;
    
    private String roleName;
    
    private String roleKey;
    
    private Integer status;
    
    private Integer sort;
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    private Boolean isDeleted;
}
