package com.kaiming.xiaohongshu.search.biz.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * ClassName: CanalClient
 * Package: com.kaiming.xiaohongshu.search.canal
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/17 11:58
 * @Version 1.0
 */
@Component
@Slf4j
public class CanalClient implements DisposableBean {
    
    @Resource
    private CanalProperties canalProperties;
   
    private CanalConnector canalConnector;

    /**
     * 实例化 CanalConnector 对象
     * @return
     */
    @Bean
    public CanalConnector getCanalConnector() {
        // 链接地址
        String address = canalProperties.getAddress();
        String[] addressArr = address.split(":");
        // Ip
        String host = addressArr[0];
        // 端口
        int port = Integer.parseInt(addressArr[1]);
        canalConnector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port),
                canalProperties.getDestination(),
                canalProperties.getUsername(),
                canalProperties.getPassword());
        
        // 连接 Canal 服务器
        canalConnector.connect();
        // 订阅 Canal 中的数据变化，指定要监听的数据库和表（可以使用表名、数据库名的通配符）
        canalConnector.subscribe(canalProperties.getSubscribe());

        // 回滚 Canal 消费者的位点，回滚到上次提交的消费位置
        canalConnector.rollback();
        return canalConnector;
    }

    /**
     * 在 Spring 容器销毁前释放资源
     */
    @Override
    public void destroy() throws Exception{
        if (Objects.nonNull(canalConnector)) {
            // 断开 canalConnector 与 Canal 服务的连接
            canalConnector.disconnect();
        }
    }
}
