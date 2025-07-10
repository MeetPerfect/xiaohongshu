package com.kaiming.xiaohongshu.auth.controller;

import com.kaiming.framework.biz.operationlog.aspect.ApiOperationLog;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.kaiming.xiaohongshu.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: VerificationCodeController
 * Package: com.kaiming.xiaohongshu.auth.controller
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 16:54
 * @Version 1.0
 */
@RestController
@Slf4j
public class VerificationCodeController {
    
    @Resource
    private  VerificationCodeService verificationCodeService;
    
    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送短信验证码")
    public Response<?> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }
}
