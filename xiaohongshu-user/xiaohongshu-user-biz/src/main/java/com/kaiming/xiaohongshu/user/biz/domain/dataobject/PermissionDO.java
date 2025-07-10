package com.kaiming.xiaohongshu.user.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: PermissionDO
 * Package: com.kaiming.xiaohongshu.auth.domain.dataobject
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/5 16:00
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionDO {

    private Long id;

    private Long parentId;

    private String name;

    private Integer type;

    private String menuUrl;

    private String menuIcon;

    private Integer sort;

    private String permissionKey;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDeleted;
}
