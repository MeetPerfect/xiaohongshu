package com.kaiming.xiaohongshu.auth.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.kaiming.framework.common.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ClassName: AliyunSmsHelper
 * Package: com.kaiming.xiaohongshu.auth.sms
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 19:41
 * @Version 1.0
 */
@Component
@Slf4j
public class AliyunSmsHelper {
    
    @Resource
    private Client client;
    
    public boolean sendMessage(String signName, String templateCode, String phone, String templateParam) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(phone)
                .setTemplateParam(templateParam);
        RuntimeOptions runtime = new  RuntimeOptions();
        try {
            log.info("==> 开始短信发送, phone: {}, signName: {}, templateCode: {}, templateParam: {}", phone, signName, templateCode, templateParam);
            // 发送短信
            SendSmsResponse response = client.sendSmsWithOptions(sendSmsRequest, runtime);
            log.info("==> 短信发送成功, response: {}", JsonUtils.toJsonString(response));
            return true;
        } catch (Exception e) {
            log.error("==> 短信发送错误: ", e);
            return false;
        }
    } 
    
}
