# CLAUDE.md

本文档为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

这是一个基于微服务架构的 Java 应用程序，用于仿小红书（Xiaohongshu），实现了社交媒体功能，如笔记发布、点赞、收藏和用户关系。基于 Spring Boot 3.0.2 和 Spring Cloud Alibaba 构建，具有处理高并发操作的分布式架构，以确保可扩展性和一致性。

## 架构结构

该项目遵循模块化的微服务架构，包含以下核心模块：

- `xiaohongshu-framework`: 包含通用工具和组件的共享基础设施
- `xiaohongshu-gateway`: 用于路由和身份验证的 API 网关
- `xiaohongshu-auth`: 认证和授权服务
- `xiaohongshu-user`: 用户管理服务
- `xiaohongshu-user-relation`: 用户关系管理（关注/取消关注）
- `xiaohongshu-note`: 笔记管理服务
- `xiaohongshu-comment`: 评论服务，支持分级评论
- `xiaohongshu-count`: 计数服务（点赞数、评论数、粉丝数等）
- `xiaohongshu-oss`: 对象存储服务（MinIO/AWS S3）
- `xiaohongshu-kv`: 键值存储服务
- `xiaohongshu-search`: 使用 Elasticsearch 的搜索功能
- `xiaohongshu-distributed-id-generator`: 分布式ID生成服务
- `xiaohongshu-data-align`: 数据对齐和同步

每个服务进一步分为 `-api`（用于 RPC 接口）和 `-biz`（业务逻辑）模块。

## 使用的技术

- **框架**: Spring Boot 3.0.2, Spring Cloud Alibaba
- **数据库**: MySQL 8.0.29, Redis 7.2.3, Cassandra 5.0.4
- **消息队列**: RocketMQ 5.3.1
- **服务发现**: Nacos 0.3.0-RC
- **搜索**: Elasticsearch 7.3.0
- **对象存储**: MinIO 8.2.1, AWS S3
- **任务调度器**: XXL-JOB 2.4.1
- **Binlog 处理**: Canal 1.1.7
- **监控**: Kibana, Logstash
- **构建工具**: Maven

## 开发环境设置

### 前提条件
- Java 17
- Maven 3.8+
- Docker 用于运行基础设施服务
- MySQL, Redis, Nacos 及其他必需服务

### 构建项目
```bash
# 编译和打包整个项目
mvn clean compile

# 构建所有服务
mvn clean package

# 如需要跳过测试
mvn clean package -DskipTests
```

### 单独运行服务
```bash
# 导航到特定服务并运行
cd xiaohongshu-user/xiaohongshu-user-biz
mvn spring-boot:run

# 或构建并运行可执行jar
mvn clean package
java -jar target/xiaohongshu-user-biz-*.jar
```

### 运行单个测试
```bash
# 运行模块的所有测试
mvn test

# 运行特定测试类
mvn -Dtest=TestClassName test

# 运行特定测试方法
mvn -Dtest=TestClassName#testMethodName test
```

## 主要开发模式

### 服务间通信
- 通过 `-api` 模块进行 RPC 通信
- 使用 Feign 客户端进行同步通信
- 通过 RocketMQ 进行异步通信以提高性能密集型操作

### 数据库操作
- MyBatis 用于 ORM 操作
- Lombok 减少样板代码
- MapStruct 用于 DTO/entity 映射

### 缓存策略
- Redis 广泛用于缓存
- ZSet 用于有序数据如动态和热门话题
- 使用 Caffeine 本地缓存频繁访问的静态数据

### 异步处理
- CompletableFuture 用于并发操作
- MQ 用于解耦繁重的操作
- XXL-JOB 用于计划任务

### 安全性
- Sa-Token 用于身份验证和会话管理
- Redis 集成用于分布式会话存储

## 常见任务

### 添加新的微服务
1. 将模块添加到主 pom.xml 中
2. 创建带有 -api 和 -biz 子模块的父模块
3. 在 -api 模块中定义接口
4. 在 -biz 模块中实现业务逻辑
5. 配置服务发现和配置

### 数据库迁移
- 使用 MyBatis Generator 从数据库模式创建实体类
- 使用 Liquibase/Flyway 进行数据库版本控制（如果已实现）

### 测试指南
- src/test/java 中的单元测试
- 具有嵌入式服务的集成测试（如果可能）
- 考虑高并发操作的性能影响

### 代码质量
- 使用 Lombok 注释减少样板代码
- 遵循 MapStruct 进行对象映射
- 遵循已建立的服务接口模式

## 基础设施服务
需要运行的必需服务：
- MySQL（用户数据、笔记等）
- Redis（缓存、会话、ZSets）
- Nacos（服务发现/配置）
- MinIO/RocketMQ/Elasticsearch 基于正在开发的功能集

## 常见问题及解决方案

- **服务发现问题**: 确保 Nacos 服务器正在运行且服务可以注册
- **数据库连接池**: 调整 Druid 连接池设置以适应高负载
- **缓存一致性**: 实施适当的缓存失效策略
- **MQ 消息传递**: 使用重试和死信队列模式处理消息失败