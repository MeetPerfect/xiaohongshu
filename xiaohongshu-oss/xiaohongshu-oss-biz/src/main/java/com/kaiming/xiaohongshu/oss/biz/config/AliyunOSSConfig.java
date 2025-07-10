package com.kaiming.xiaohongshu.oss.biz.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: AliyunOSSConfig
 * Package: com.kaiming.xiaohongshu.oss.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 16:15
 * @Version 1.0
 */
@Configuration
public class AliyunOSSConfig {
    
    @Resource
    private AliyunOSSProperties aliyunOSSProperties;
    
    @Bean   
    public OSS aliyunOSSClient() {
        // 设置访问凭证
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(
                aliyunOSSProperties.getAccessKey(), aliyunOSSProperties.getSecretKey());

        // 创建 OSSClient 实例
        return new OSSClientBuilder().build(aliyunOSSProperties.getEndpoint(), credentialsProvider);
    }
    
}
