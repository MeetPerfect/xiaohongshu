package com.kaiming.xiaohongshu.user.dto.req;

import com.kaiming.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: FindUserByPhoneReqDTO
 * Package: com.kaiming.xiaohongshu.user.dto.req
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/1 20:18
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByPhoneReqDTO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;
}
