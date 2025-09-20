# Sensor Spring — Hybrid Dev Setup

> **目标**：Spring Boot 本地进程启动；**Nginx、MySQL、Redis、RabbitMQ** 通过 **Docker Compose** 容器化运行。

## 组件版本
- Java：JDK **21**
- Spring Boot：**3.3.x**
- Gradle：使用 **Wrapper 8.10.0**（脚本会自动生成；无需本机固定版本）
- 端口映射：MySQL **3307**、Redis **6380**、RabbitMQ **5673/15673**、Nginx **8088**、本地 Spring **8080**

## 1. 启动依赖容器
```bash
./dev-up.sh
# or
docker compose up -d mysql redis rabbitmq nginx
```
- MySQL 首次启动会执行 `./db/init/*.sql` 初始化。
- Nginx 通过 `host.docker.internal:8080` 反代到你的本机 Spring。
  - Linux 若失败，请升级 Docker 至 20.10+；或在 compose 中保留 `extra_hosts: host.docker.internal:host-gateway`。

## 2. 本地启动 Spring
```bash
./run-local.sh
# Windows
.un-local.ps1
```
脚本会：
1) 若缺少 Wrapper，使用你的系统 Gradle 生成 **8.10.0** 的 wrapper；  
2) 设置 `APP_UPLOAD_ROOT`/`LOG_PATH`/`JWT_SECRET`；  
3) 使用 `--spring.profiles.active=local` 启动。

## 3. 访问
- 应用（经 Nginx 反代）：http://localhost:8088/
- Spring 直连（开发调试）：http://localhost:8080/
- 健康检查：`/actuator/health`
- Prometheus：`/actuator/prometheus`
- RabbitMQ 管理台：http://localhost:15673/

## 4. 配置
- `src/main/resources/application-local.yml`：
  - MySQL：`jdbc:mysql://localhost:3307/modality`
  - Redis：`localhost:6380`
  - RabbitMQ：`localhost:5673`
  - 上传根目录：`app.upload.upload-root=./uploads`（兼容旧键 `app.upload-root`）

## 5. 清理
```bash
./dev-down.sh
# 或
docker compose down -v
```

> 提示：若你希望让前端静态资源由 Nginx 托管，把构建产物放到 `nginx/html`，通过 `http://localhost:8088/static/` 访问。
