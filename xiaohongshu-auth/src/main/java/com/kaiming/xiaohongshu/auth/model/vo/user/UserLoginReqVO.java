package com.kaiming.xiaohongshu.auth.model.vo.user;

import com.kaiming.framework.common.validator.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.message.Message;

/**
 * ClassName: UserLoginReqVO
 * Package: com.kaiming.xiaohongshu.auth.model.vo.user
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 21:53
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginReqVO {

    //手机号
    @NotBlank(message = "手机号不能为空")
    @PhoneNumber
    private String phone;
    // 验证码
    private String code;
    //密码
    private String password;
    // 登陆类型
    @NotBlank(message = "登陆类型不能为空")
    private Integer type;

}
