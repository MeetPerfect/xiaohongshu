package com.kaiming.xiaohongshu.auth.sms;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: AliyunAccessKeyProperties
 * Package: com.kaiming.xiaohongshu.auth.sms
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/4 19:40
 * @Version 1.0
 */
@ConfigurationProperties(prefix = "aliyun")
@Component
@Data
public class AliyunAccessKeyProperties {

    private String accessKeyId;
    private String accessKeySecret;
}
