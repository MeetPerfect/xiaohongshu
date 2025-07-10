package com.kaiming.xiaohongshu.oss.biz.factory;

import com.kaiming.xiaohongshu.oss.biz.strategy.FileStrategy;
import com.kaiming.xiaohongshu.oss.biz.strategy.impl.AliyunOSSFileStrategy;
import com.kaiming.xiaohongshu.oss.biz.strategy.impl.MinioFileStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FileStrategyFactory
 * Package: com.kaiming.xiaohongshu.oss.biz.factory
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/5/31 12:57
 * @Version 1.0
 */
@Configuration
@RefreshScope
public class FileStrategyFactory {
    
    @Value("${storage.type}")
    private String strategyType;
    
    @Bean
    @RefreshScope
    public FileStrategy getFileStrategy() {
        if (StringUtils.equals(strategyType, "aliyun")) {
            return new AliyunOSSFileStrategy();    
        } else if (StringUtils.equals(strategyType, "minio")) {
            return new MinioFileStrategy();
        }
        throw new IllegalArgumentException("不支持的存储类型");
    }
}
