package com.kaiming.xiaohongshu.oss.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: AliyunOSSProperties
 * Package: com.kaiming.xiaohongshu.oss.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 16:12
 * @Version 1.0
 */
@ConfigurationProperties(prefix = "storage.aliyun-oss")
@Component
@Data
public class AliyunOSSProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
}
