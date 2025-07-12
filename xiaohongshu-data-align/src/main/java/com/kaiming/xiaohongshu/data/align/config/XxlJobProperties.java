package com.kaiming.xiaohongshu.data.align.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: XxlJobProperties
 * Package: com.kaiming.xiaohongshu.data.align.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/11 22:32
 * @Version 1.0
 */
@ConfigurationProperties(prefix = XxlJobProperties.PREFIX)
@Component
@Data
public class XxlJobProperties  {

    public  static final String PREFIX =  "xxl.job";
    
    private String adminAddresses;
    
    private String accessToken;
    
    private String appName;
    
    private String ip;
    
    private int port;
    
    private String logPath;
    
    private int logRetentionDays = 30;
}
