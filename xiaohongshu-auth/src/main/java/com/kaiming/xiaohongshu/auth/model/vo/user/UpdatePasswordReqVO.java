package com.kaiming.xiaohongshu.auth.model.vo.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UpdatePasswordReqVO
 * Package: com.kaiming.xiaohongshu.auth.model.vo.user
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/24 18:36
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordReqVO {
    
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
