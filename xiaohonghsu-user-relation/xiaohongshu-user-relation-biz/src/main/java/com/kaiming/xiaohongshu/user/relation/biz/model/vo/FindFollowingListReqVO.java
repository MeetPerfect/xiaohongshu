package com.kaiming.xiaohongshu.user.relation.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName: FindFollowingListReqVO
 * Package: com.kaiming.xiaohongshu.user.relation.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/2 21:30
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFollowingListReqVO {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;
}
