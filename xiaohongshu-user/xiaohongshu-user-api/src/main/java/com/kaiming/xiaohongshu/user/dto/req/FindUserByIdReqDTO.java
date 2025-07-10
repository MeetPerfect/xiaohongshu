package com.kaiming.xiaohongshu.user.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserByIdReqDTO
 * Package: com.kaiming.xiaohongshu.user.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/14 13:54
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByIdReqDTO {

    /**
     * 手机号
     */
    @NotNull(message = "用户ID不能为空")
    private Long id;
    
}
