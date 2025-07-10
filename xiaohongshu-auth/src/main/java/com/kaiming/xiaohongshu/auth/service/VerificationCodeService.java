package com.kaiming.xiaohongshu.auth.service;

import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

/**
 * ClassName: VerificationCodeServiceImpl
 * Package: com.kaiming.xiaohongshu.auth.service
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 16:41
 * @Version 1.0
 */
public interface VerificationCodeService {
    
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
