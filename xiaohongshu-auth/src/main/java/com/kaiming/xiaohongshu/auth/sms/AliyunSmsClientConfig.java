package com.kaiming.xiaohongshu.auth.sms;

import com.aliyun.dysmsapi20170525.Client;

import com.aliyun.teaopenapi.models.Config;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: AliyunSmsClientConfig
 * Package: com.kaiming.xiaohongshu.auth.sms
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 19:41
 * @Version 1.0
 */
@Configuration
@Slf4j
public class AliyunSmsClientConfig {
    
    @Resource
    private AliyunAccessKeyProperties aliyunAccessKeyProperties;
    
    @Bean
    public Client client() {
        try {
            Config config = new Config()
                    .setAccessKeyId(aliyunAccessKeyProperties.getAccessKeyId())
                    .setAccessKeySecret(aliyunAccessKeyProperties.getAccessKeySecret());
            config.endpoint = "dysmsapi.aliyuncs.com";

            return new Client(config);
        } catch (Exception e) {
            log.error("初始化阿里云短信发送客户端错误: ", e);
            return null;
        }
    }
}
