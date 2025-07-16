package com.kaiming.xiaohongshu.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * ClassName: ElasticsearchProperties
 * Package: com.kaiming.xiaohongshu.search.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 11:04
 * @Version 1.0
 */
@Component
@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class ElasticsearchProperties {
    
    private String address;
}
