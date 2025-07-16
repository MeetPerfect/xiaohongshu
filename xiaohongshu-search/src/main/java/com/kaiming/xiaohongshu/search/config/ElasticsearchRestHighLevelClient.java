package com.kaiming.xiaohongshu.search.config;

import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: ElasticsearchRestHighLevelClient
 * Package: com.kaiming.xiaohongshu.search.config
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/16 11:05
 * @Version 1.0
 */
@Configuration
public class ElasticsearchRestHighLevelClient {

    @Resource
    private ElasticsearchProperties elasticsearchProperties;

    private static final String COLON = ":";
    private static final String HTTP = "http";

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String address = elasticsearchProperties.getAddress();
        // 按:分割
        String[] addressArr = address.split(COLON);
        // Ip地址
        address = addressArr[0];
        // 端口
        int port = Integer.parseInt(addressArr[1]);

        HttpHost httpHost = new HttpHost(address, port, HTTP);

        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
