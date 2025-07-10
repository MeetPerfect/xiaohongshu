package com.kaiming.xiaohongshu.auth.model.vo.verificationcode;

import com.kaiming.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: SendVerificationCodeReqVO
 * Package: com.kaiming.xiaohongshu.auth.model.vo.verificationcode
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 15:46
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVO {

    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;

}
