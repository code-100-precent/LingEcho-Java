#!/bin/bash

# LingEcho Java 微服务启动脚本

echo "=========================================="
echo "LingEcho Java Microservices Startup"
echo "=========================================="

# 检查 Java 版本
if ! command -v java &> /dev/null; then
    echo "错误: 未找到 Java，请先安装 JDK 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "错误: Java 版本需要 17 或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到 Maven，请先安装 Maven 3.8+"
    exit 1
fi

# 编译项目
echo "正在编译项目..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi

# 启动顺序
echo ""
echo "启动服务（按 Ctrl+C 停止所有服务）..."
echo ""

# 启动 Eureka Server
echo "1. 启动 Eureka Server (端口 8761)..."
cd lingecho-eureka-server
mvn spring-boot:run > ../logs/eureka.log 2>&1 &
EUREKA_PID=$!
cd ..
sleep 10

# 等待 Eureka 启动
echo "等待 Eureka Server 启动..."
sleep 5

# 启动其他服务
echo "2. 启动 API Gateway (端口 8080)..."
cd lingecho-api-gateway
mvn spring-boot:run > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

echo "3. 启动 Auth Service (端口 8081)..."
cd lingecho-auth-service
mvn spring-boot:run > ../logs/auth.log 2>&1 &
AUTH_PID=$!
cd ..

echo "4. 启动 User Service (端口 8082)..."
cd lingecho-user-service
mvn spring-boot:run > ../logs/user.log 2>&1 &
USER_PID=$!
cd ..

echo "5. 启动 Assistant Service (端口 8083)..."
cd lingecho-assistant-service
mvn spring-boot:run > ../logs/assistant.log 2>&1 &
ASSISTANT_PID=$!
cd ..

echo "6. 启动 Voice Service (端口 8084)..."
cd lingecho-voice-service
mvn spring-boot:run > ../logs/voice.log 2>&1 &
VOICE_PID=$!
cd ..

echo ""
echo "=========================================="
echo "服务启动完成！"
echo "=========================================="
echo "Eureka Server: http://localhost:8761"
echo "API Gateway: http://localhost:8080"
echo ""
echo "日志文件位置: ./logs/"
echo ""
echo "按 Ctrl+C 停止所有服务"
echo "=========================================="

# 等待中断信号
trap "echo '正在停止所有服务...'; kill $EUREKA_PID $GATEWAY_PID $AUTH_PID $USER_PID $ASSISTANT_PID $VOICE_PID 2>/dev/null; exit" INT TERM

# 保持脚本运行
wait

