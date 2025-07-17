package com.kaiming.xiaohongshu.search.canal;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: CanalProperties
 * Package: com.kaiming.xiaohongshu.search.canal
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 11:56
 * @Version 1.0
 */

@ConfigurationProperties(prefix = CanalProperties.PREFIX)
@Component
@Data
public class CanalProperties {

    public static final String PREFIX = "canal";

    /**
     * Canal 链接地址
     */
    private String address;

    /**
     * 数据目标
     */
    private String destination;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 订阅规则
     */
    private String subscribe;

    /**
     * 一批次拉取数据量，默认 1000 条
     */
    private int batchSize = 1000;
}
