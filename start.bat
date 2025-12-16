@echo off
REM LingEcho Java 微服务启动脚本 (Windows)

echo ==========================================
echo LingEcho Java Microservices Startup
echo ==========================================

REM 检查 Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 Java，请先安装 JDK 17+
    exit /b 1
)

REM 检查 Maven
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 Maven，请先安装 Maven 3.8+
    exit /b 1
)

REM 创建日志目录
if not exist logs mkdir logs

REM 编译项目
echo 正在编译项目...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败，请检查错误信息
    exit /b 1
)

echo.
echo 启动服务...
echo.

REM 启动 Eureka Server
echo 1. 启动 Eureka Server (端口 8761)...
start "Eureka Server" cmd /k "cd lingecho-eureka-server && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

REM 启动其他服务
echo 2. 启动 API Gateway (端口 8080)...
start "API Gateway" cmd /k "cd lingecho-api-gateway && mvn spring-boot:run"

echo 3. 启动 Auth Service (端口 8081)...
start "Auth Service" cmd /k "cd lingecho-auth-service && mvn spring-boot:run"

echo 4. 启动 User Service (端口 8082)...
start "User Service" cmd /k "cd lingecho-user-service && mvn spring-boot:run"

echo 5. 启动 Assistant Service (端口 8083)...
start "Assistant Service" cmd /k "cd lingecho-assistant-service && mvn spring-boot:run"

echo 6. 启动 Voice Service (端口 8084)...
start "Voice Service" cmd /k "cd lingecho-voice-service && mvn spring-boot:run"

echo.
echo ==========================================
echo 服务启动完成！
echo ==========================================
echo Eureka Server: http://localhost:8761
echo API Gateway: http://localhost:8080
echo.
echo 关闭各个服务窗口即可停止服务
echo ==========================================

pause

