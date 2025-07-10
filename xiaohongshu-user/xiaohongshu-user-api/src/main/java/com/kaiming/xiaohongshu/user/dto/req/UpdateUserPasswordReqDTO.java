package com.kaiming.xiaohongshu.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UpdateUserPasswordReqDTO
 * Package: com.kaiming.xiaohongshu.user.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 20:54
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserPasswordReqDTO {
    
    @NotBlank(message = "密码不能为空")
    private String encodePassword; 
}
