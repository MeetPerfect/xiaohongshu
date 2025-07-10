package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindFansListReqVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/3 16:10
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFansListReqVO {

    @NotNull(message = "查询用户 ID 不能为空")
    private Long userId;

    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1; // 默认值为第一页
    
}
