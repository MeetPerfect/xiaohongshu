package com.kaiming.xiaohongshu.kv.biz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

/**
 * ClassName: CassandraConfig
 * Package: com.kaiming.xiaohongshu.kv.biz.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/6/2 17:53
 * @Version 1.0
 */
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {
    
    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;
    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;
    @Value("${spring.cassandra.port}")
    private int port;
    @Override
    protected String getKeyspaceName() {
        return keyspaceName;
    }

    @Override
    public String getContactPoints() {
        return contactPoints;
    }

    @Override
    public int getPort() {
        return port;
    }
}
