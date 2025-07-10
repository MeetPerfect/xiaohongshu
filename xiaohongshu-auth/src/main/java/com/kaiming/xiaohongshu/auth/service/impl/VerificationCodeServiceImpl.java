package com.kaiming.xiaohongshu.auth.service.impl;

import com.kaiming.framework.common.exception.BizException;
import com.kaiming.framework.common.response.Response;
import com.kaiming.xiaohongshu.auth.constant.RedisKeyConstants;
import com.kaiming.xiaohongshu.auth.enums.ResponseCodeEnum;
import com.kaiming.xiaohongshu.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.kaiming.xiaohongshu.auth.service.VerificationCodeService;
import com.kaiming.xiaohongshu.auth.sms.AliyunSmsHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: VerificationCodeServiceImpl
 * Package: com.kaiming.xiaohongshu.auth.service.impl
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 16:42
 * @Version 1.0
 */
@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private AliyunSmsHelper aliyunSmsHelper;

    /**
     * 发送验证码
     * @param sendVerificationCodeReqVO
     * @return
     */
    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        // 手机号
        String phone = sendVerificationCodeReqVO.getPhone();
        // 构建验证码的Redis Key
        String key = RedisKeyConstants.buildVerificationCodeKey(phone);
        // 判断验证码是否已经发送
        Boolean isSent = redisTemplate.hasKey(key);
        if (isSent) {
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_KEY_FREQUENTLY);
        }
        
        // 生成验证码
        // TODO
//        String verificationCode = RandomUtil.randomNumbers(6);
        String verificationCode = "123456";
        
        // 
        threadPoolTaskExecutor.submit(() -> {
            String signName = "阿里云短信测试" ;
            String templateCode = "SMS_154950909";
            String templateParam = String.format("{\"code\":\"%s\"}", verificationCode);
            aliyunSmsHelper.sendMessage(signName, templateCode, phone, templateParam);
        });
        
        log.info("==> 手机号: {}, 已发送验证码: {}", phone, verificationCode);
        redisTemplate.opsForValue().set(key, verificationCode, 3, TimeUnit.MINUTES);

        return Response.success();
    }
    
}
