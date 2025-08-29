# xiaohongshu
仿小红书社区

![](https://img.shields.io/github/stars/meetPerfect/xiaohongshu)![](https://img.shields.io/github/forks/MeetPerfect/xiaohongshu)![](https://img.shields.io/badge/lincese-GPL-brightgreen)



## 项目简介

仿小红书社区项目，主要包括笔记发布、点赞、收藏、关注等功能。平台需要满足海量用户的高并发读写和数据一致性要求，确保用户操作的实时响应，并通过分布式架构实现高可用和高扩展性。



## 环境搭建

### 开发工具

| 工具名称           | 说明                | 官网地址                                        |
| ------------------ | ------------------- | ----------------------------------------------- |
| IDEA               | 开发编辑器          | https://www.jetbrains.com/idea/                 |
| VMware Workstation | 虚拟机              | https://www.vmware.com/                         |
| X-shell            | ssh远程链连接工具   | https://www.xshell.com/zh/xshell/               |
| Xftp               | 可视化上传文件      | https://www.xshell.com/zh/xftp/                 |
| Navicat            | 可视化数据库        | https://www.navicat.com.cn/                     |
| Postman            | Api接口调试工具     | https://www.postman.com/                        |
| RedisDesktop       | Redis客户端连接工具 | https://goanother.com/cn/                       |
| Docker Desktop     | docker可视化        | https://www.docker.com/products/docker-desktop/ |
| Visual Studio Code | 编辑器              | https://code.visualstudio.com/                  |
| Snipaste           | 屏幕截图工具        | https://www.snipaste.com/                       |
| Typora             | Markdown编辑器      | https://typora.io/                              |
| Presson            | 流程图              | https://www.processon.com/                      |



## 技术选型

### 后端

| 技术               | 版本     | 说明                     | 官网地址                                       |
| ------------------ | -------- | ------------------------ | ---------------------------------------------- |
| SpringBoot         | 3.0.2    | Web应用框架              | https://spring.io/projects/spring-boot         |
| MyBatis            | 1.3.5    | ORM框架                  | https://mybatis.org/mybatis-3/                 |
| MyBatisGenerator   | 1.3.5    | 数据层代码生成器         | https://mybatis.org/generator/                 |
| Lombok             | 1.18.30  | Java语言增强库           | https://github.com/projectlombok/lombok        |
| MySql              | 8.0.29   | 关系型数据库             | https://www.mysql.com/                         |
| Redis              | 7.2.3    | 内存数据库               | https://redis.io/                              |
| nacos              | 0.3.0-RC | 服务注册中心             | https://nacos.io/en/                           |
| Cassandra          | 5.0.4    | 短文本存储               | https://github.com/apache/cassandra            |
| MinIO              | 8.2.1    | 对象存储                 | https://github.com/minio/minio                 |
| OSS                | 3.17.4   | 阿里云对象存储           | https://www.aliyun.com/product/oss             |
| Canal              | 1.1.7    | binlog 增量订阅&消费组件 | https://github.com/alibaba/canal               |
| Zookeeper          | 3.5.6    | 分布式系统服务           | https://github.com/apache/zookeeper            |
| RocketMQ           | 5.3.1    | 消息中间件               | https://github.com/apache/rocketmq             |
| RocketMQ-dashboard | latest   | 可视化RocketMQ           | https://github.com/apache/rocketmq-dashboard   |
| XXL-JOB            | 2.4.1    | 分布式任务调度平台       | https://github.com/xuxueli/xxl-job             |
| Elasticsearch      | 7.3.0    | 分布式搜索               | https://www.elastic.co/cn/elasticsearch        |
| Logstash           | 7.3.0    | 全量ES索引构建           | https://www.elastic.co/downloads/past-releases |
| Kibana             | 7.3.0    | 日志可视化查看工具       | https://www.elastic.co/cn/kibana               |
| Docker             | 26.1.4   | 应用容器引擎             | https://www.docker.com                         |
| FRP                | v0.61.2  | 内网穿透                 | https://github.com/fatedier/frp                |



### 前端

| 技术       | 版本    | 说明                | 官网地址                                              |
| ---------- | ------- | ------------------- | ----------------------------------------------------- |
| Node.js    | 22.14.0 | 服务端的 JavaScript | https://nodejs.org/zh-cn                              |
| Vue        |         | 前端框架            | https://vuejs.org/                                    |
| Vue-router |         | 路由框架            | https://router.vuejs.org/                             |
| Vuex       |         | 全局状态管理框架    | https://vuex.vuejs.org/                               |
| Element    |         | 前端UI框架          | [https://element.eleme.io](https://element.eleme.io/) |
|            |         |                     |                                                       |
|            |         |                     |                                                       |



## 项目组织结构

```java
|---xiaohongshu
    |---xiaohonghsu-user-relation		-- 用户关系模块
    |   |---xiaohongshu-user-relation-api		-- 用户关系,RPC层,供其他服务调用
    |   |---xiaohongshu-user-relation-biz		-- 用户关系业务模块
    |---xiaohongshu-auth			-- 用户认证模块
    |---xiaohongshu-comment			-- 评论模块
    |   |---xiaohongshu-comment-api		-- 评论,RPC层,供其他服务调用
    |   |---xiaohongshu-comment-biz		-- 评论业务模块
    |---xiaohongshu-count			-- 计数模块
    |   |---xiaohongshu-count-api		-- 计数,RPC层,供其他服务调用
    |   |---xiaohongshu-count-biz		-- 计数业务模块
    |---xiaohongshu-data-align			-- 数据对齐模块
    |---xiaohongshu-distributed-id-generator		-- 分布式Id模块
    |   |---xiaohongshu-distributed-id-generator-api		-- 分布式Id生成,RPC层,供其他服务调用
    |   |---xiaohongshu-distributed-id-generator-biz		-- 分布式Id生成业务模块
    |---xiaohongshu-framework		-- 平台基础设施层模块-封装一些常用功能，供各个业务线拿来即用		
    |   |---xiaohongshu-common			-- 平台通用模块，如一些通用枚举、工具类		
    |   |---xiaohongshu-spring-boot-starter-biz-context			-- 上下文组件
    |   |---xiaohongshu-spring-boot-starter-biz-operationlog		-- 接口日志文件
    |   |---xiaohongshu-spring-boot-starter-jackson			-- 自定义 Jackson 配置
    |---xiaohongshu-gateway			-- 网关模块
    |---xiaohongshu-kv			-- KV 键值模块
    |   |---xiaohongshu-kv-api			-- KV,RPC层,供其他服务调用
    |   |---xiaohongshu-kv-biz			-- KV业务模块
    |---xiaohongshu-note		-- 笔记模块
    |   |---xiaohongshu-note-api		-- 笔记,RPC层,供其他服务调用
    |   |---xiaohongshu-note-biz		-- 笔记业务模块
    |---xiaohongshu-oss			-- 对象存储模块
    |   |---xiaohongshu-oss-api			-- 对象存储,RPC层,供其他服务调用
    |   |---xiaohongshu-oss-biz			-- 对象存储业务模块
    |---xiaohongshu-search		-- 搜索模块
    |   |---xiaohonghsu-search-api		-- 搜索,RPC层,供其他服务调用
    |   |---xiaohongshu-search-biz		-- 搜索业务模块
    |---xiaohongshu-user		-- 用户模块
        |---xiaohongshu-user-api		-- 用户,RPC层,供其他服务调用
        |---xiaohongshu-user-biz		-- 用户业务模块
```



## 项目展示







## TODO
