package com.kaiming.xiaohongshu.user.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserProfileReqVO
 * Package: com.kaiming.xiaohongshu.user.biz.model.vo
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/24 21:58
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserProfileReqVO {
    
    @NotNull(message = "用户Id不能为空")
    private Long userId;
}
