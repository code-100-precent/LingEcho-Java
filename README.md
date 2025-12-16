# LingEcho Server - Java Microservices Version

基于 Spring Cloud 的微服务架构实现，将原 Go 版本拆分为多个独立的微服务。

## 🏗️ 架构设计

### 服务列表

| 服务名称 | 端口 | 说明 |
|---------|------|------|
| eureka-server | 8761 | 服务注册中心 |
| api-gateway | 8080 | API 网关 |
| auth-service | 8081 | 认证授权服务 |
| user-service | 8082 | 用户管理服务 |
| assistant-service | 8083 | AI 助手服务 |
| voice-service | 8084 | 语音服务（WebSocket/WebRTC） |
| knowledge-service | 8086 | 知识库服务 |
| billing-service | 8087 | 计费服务 |
| alert-service | 8088 | 告警服务 |
| device-service | 8089 | 设备管理服务 |

## 📦 技术栈

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Cloud 2023.0.0**
- **Spring Cloud Alibaba 2022.0.0.0**
- **Spring Data JPA**
- **MySQL / PostgreSQL**
- **Redis**
- **WebSocket**
- **gRPC** (服务间通信)

## 🚀 快速开始

### 前置要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+ / PostgreSQL 14+
- Redis 6.0+

### 方式一：使用启动脚本（推荐）

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

**Windows:**
```cmd
start.bat
```

### 方式二：手动启动

1. **编译项目**
```bash
mvn clean install -DskipTests
```

2. **启动 Eureka Server**
```bash
cd lingecho-eureka-server
mvn spring-boot:run
```

3. **启动其他服务**（可以并行启动）
```bash
# 启动 API Gateway
cd lingecho-api-gateway && mvn spring-boot:run

# 启动各个业务服务
cd lingecho-auth-service && mvn spring-boot:run
cd lingecho-user-service && mvn spring-boot:run
cd lingecho-assistant-service && mvn spring-boot:run
cd lingecho-voice-service && mvn spring-boot:run
# ... 其他服务
```

### 方式三：Docker Compose 启动

```bash
docker-compose up -d
```

### 验证服务

- **Eureka 控制台**: http://localhost:8761
- **API 网关**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health

## 📁 项目结构

```
server-java/
├── lingecho-common/              # 公共模块
│   ├── lingecho-common-core/     # 核心工具类
│   ├── lingecho-common-web/     # Web 公共组件
│   └── lingecho-common-db/       # 数据库公共组件
├── lingecho-eureka-server/       # 服务注册中心
├── lingecho-api-gateway/         # API 网关
├── lingecho-auth-service/        # 认证服务
├── lingecho-user-service/         # 用户服务
├── lingecho-assistant-service/   # 助手服务
├── lingecho-voice-service/        # 语音服务
├── lingecho-knowledge-service/    # 知识库服务
├── lingecho-billing-service/      # 计费服务
├── lingecho-alert-service/        # 告警服务
└── lingecho-device-service/       # 设备服务
```

## 🔧 配置说明

各服务的配置文件位于 `src/main/resources/application.yml`

主要配置项：
- 数据库连接
- Redis 连接
- Eureka 注册中心地址
- 服务端口

## 📝 开发规范

1. 使用统一的响应格式（在 common 模块中定义）
2. 统一异常处理
3. 统一日志格式
4. API 文档使用 Swagger/OpenAPI

## 🔗 服务间通信

- **同步调用**: OpenFeign
- **异步消息**: Spring Cloud Stream / RabbitMQ
- **实时通信**: WebSocket / gRPC

## 📚 文档

- [API 文档](docs/api.md)
- [架构设计](docs/architecture.md)
- [部署指南](docs/deployment.md)

