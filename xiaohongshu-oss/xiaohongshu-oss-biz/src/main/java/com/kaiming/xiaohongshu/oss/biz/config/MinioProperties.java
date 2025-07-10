package com.kaiming.xiaohongshu.oss.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: MinioProperties
 * Package: com.kaiming.xiaohongshu.oss.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 14:13
 * @Version 1.0
 */
@ConfigurationProperties(prefix = "storage.minio")
@Component
@Data
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
}
