package com.kaiming.xiaohongshu.oss.biz.config;

import io.minio.MinioClient;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MinioConfig
 * Package: com.kaiming.xiaohongshu.oss.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 14:15
 * @Version 1.0
 */
@Configuration
public class MinioConfig {
    @Resource
    private MinioProperties minioProperties;
    
    @Bean
    public MinioClient minioClient() {
        // 构建Minio客户端
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
