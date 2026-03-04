# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a microservices-based Java application for a Xiaohongshu (Little Red Book) clone, implementing social media features like note publishing, likes, collections, and user relationships. Built with Spring Boot 3.0.2 and Spring Cloud Alibaba, it handles high-concurrency operations with distributed architecture for scalability and consistency.

## Architecture Structure

The project follows a modular microservices architecture with the following core modules:

- `xiaohongshu-framework`: Shared infrastructure with common utilities and components
- `xiaohongshu-gateway`: API Gateway for routing and authentication
- `xiaohongshu-auth`: Authentication and authorization service
- `xiaohongshu-user`: User management service
- `xiaohongshu-user-relation`: User relationship management (follow/unfollow)
- `xiaohongshu-note`: Note management service
- `xiaohongshu-comment`: Comment service with hierarchical comment support
- `xiaohongshu-count`: Counting service (likes, comments, followers, etc.)
- `xiaohongshu-oss`: Object storage service (MinIO/AWS S3)
- `xiaohongshu-kv`: Key-value storage service
- `xiaohongshu-search`: Search functionality with Elasticsearch
- `xiaohongshu-distributed-id-generator`: Distributed ID generation service
- `xiaohongshu-data-align`: Data alignment and synchronization

Each service is further divided into `-api` (for RPC interfaces) and `-biz` (business logic) modules.

## Technologies Used

- **Framework**: Spring Boot 3.0.2, Spring Cloud Alibaba
- **Database**: MySQL 8.0.29, Redis 7.2.3, Cassandra 5.0.4
- **Message Queue**: RocketMQ 5.3.1
- **Service Discovery**: Nacos 0.3.0-RC
- **Search**: Elasticsearch 7.3.0
- **Object Storage**: MinIO 8.2.1, AWS S3
- **Task Scheduler**: XXL-JOB 2.4.1
- **Binlog Processing**: Canal 1.1.7
- **Monitoring**: Kibana, Logstash
- **Build Tool**: Maven

## Development Setup

### Prerequisites
- Java 17
- Maven 3.8+
- Docker for running infrastructure services
- MySQL, Redis, Nacos, and other required services

### Building the Project
```bash
# Compile and package the entire project
mvn clean compile

# Build all services
mvn clean package

# Skip tests if needed
mvn clean package -DskipTests
```

### Running Services Individually
```bash
# Navigate to a specific service and run
cd xiaohongshu-user/xiaohongshu-user-biz
mvn spring-boot:run

# Or build and run the executable jar
mvn clean package
java -jar target/xiaohongshu-user-biz-*.jar
```

### Running Individual Tests
```bash
# Run all tests for a module
mvn test

# Run a specific test class
mvn -Dtest=TestClassName test

# Run a specific test method
mvn -Dtest=TestClassName#testMethodName test
```

## Key Development Patterns

### Service-to-Service Communication
- Services communicate via RPC through the `-api` modules
- Use Feign clients for synchronous communication
- Asynchronous communication via RocketMQ for performance-intensive operations

### Database Operations
- MyBatis for ORM operations
- Lombok for reducing boilerplate code
- MapStruct for DTO/entity mapping

### Caching Strategy
- Redis is extensively used for caching
- ZSet for ordered data like feeds and hot topics
- Local cache with Caffeine for frequently accessed static data

### Asynchronous Processing
- CompletableFuture for concurrent operations
- MQ for decoupling heavy operations
- XXL-JOB for scheduled tasks

### Security
- Sa-Token for authentication and session management
- Redis integration for distributed session storage

## Common Tasks

### Adding a New Microservice
1. Add the module to the main pom.xml
2. Create the parent module with -api and -biz submodules
3. Define your interfaces in -api module
4. Implement business logic in -biz module
5. Configure service discovery and configuration

### Database Migration
- Use MyBatis Generator for creating entity classes from DB schema
- Apply Liquibase/Flyway for database versioning (if implemented)

### Testing Guidelines
- Unit tests in src/test/java
- Integration tests with embedded services where possible
- Consider performance implications for high-concurrency operations

### Code Quality
- Use Lombok annotations to reduce boilerplate
- Follow MapStruct for object mapping
- Adhere to the established service interface patterns

## Infrastructure Services
Required services that need to be running:
- MySQL (user data, notes, etc.)
- Redis (caching, sessions, ZSets)
- Nacos (service discovery/config)
- MinIO/RocketMQ/Elasticsearch based on the feature set being developed

## Common Issues and Solutions

- **Service Discovery Issues**: Ensure Nacos server is running and services can register
- **Database Connection Pool**: Tune Druid connection pool settings for high load
- **Cache Consistency**: Implement appropriate cache invalidation strategies
- **MQ Message Delivery**: Handle message failures with retries and DLQ patterns