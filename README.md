# Sensor Spring — Hybrid Dev Setup

> **目标**：Spring Boot 本地进程启动；**Nginx、MySQL、Redis、RabbitMQ** 通过 **Docker Compose** 容器化运行。

## 组件版本
- Java：JDK **21**
- Spring Boot：**3.3.2**
- Gradle：使用 **Wrapper 8.12**（脚本会自动生成；无需本机固定版本）
- 端口映射：MySQL **3307**、Redis **6380**、RabbitMQ **5673/15673**、Nginx **8088**、本地 Spring **8080**

---

## 功能特性

### 核心功能
- **用户认证**：JWT Token 认证，支持登录、注册、刷新Token、登出
- **文件管理**：文件上传、下载、删除，支持SHA-256去重
- **会员体系**：四级会员系统（普通用户、白银、黄金、钻石）
- **缓存管理**：Redis缓存用户、文件、会员信息
- **性能监控**：系统资源监控、缓存命中率统计

### 会员等级

| 等级 | 代码 | 存储空间 | 单文件限制 | 文件数量 |
|------|------|----------|-----------|----------|
| 普通用户 | free | 1 GB | 50 MB | 100 |
| 白银会员 | silver | 5 GB | 100 MB | 500 |
| 黄金会员 | gold | 10 GB | 200 MB | 1,000 |
| 钻石会员 | diamond | 50 GB | 1 GB | 10,000 |

---

## API 接口

### 认证接口 `/api/auth`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /register | 用户注册 | 否 |
| POST | /login | 用户登录 | 否 |
| POST | /refresh | 刷新Token | 否 |
| POST | /logout | 登出 | 是 |

### 用户接口 `/api/users`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /me | 获取当前用户信息 | 是 |
| GET | / | 获取所有用户（管理员） | 是(ADMIN) |

### 会员接口 `/api/membership`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /info | 获取当前用户会员信息 | 是 |
| GET | /levels | 获取所有会员等级 | 否 |
| POST | /upgrade | 升级会员 | 是 |
| POST | /renew | 续费会员 | 是 |
| POST | /cancel | 取消自动续费 | 是 |
| GET | /history | 获取会员历史 | 是 |

### 监控接口 `/api/monitor`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /health | 健康检查 | 是 |
| GET | /stats | 获取所有统计 | 是 |
| GET | /cache | 获取缓存统计 | 是 |
| GET | /database | 获取数据库统计 | 是 |
| GET | /system | 获取系统统计 | 是 |

### Actuator 监控 `/actuator`
| 路径 | 说明 |
|------|------|
| /health | 健康检查 |
| /info | 应用信息 |
| /prometheus | Prometheus指标 |
| /metrics | Metrics指标 |

---

## 快速开始

### 1. 启动依赖容器
```bash
# Linux/macOS
./dev-up.sh

# 或直接使用 docker-compose
docker-compose up -d mysql redis rabbitmq nginx
```

- MySQL 首次启动会执行 `./db/init/*.sql` 初始化数据库表
- Nginx 通过 `host.docker.internal:8080` 反代到本机 Spring

### 2. 启动 Spring Boot
```bash
# Linux/macOS
./gradlew bootRun

# Windows
.\gradlew.bat bootRun

# 或直接运行jar
java -jar build/libs/sensor-spring-ha-mq-1.1.0.jar
```

### 3. 访问服务
| 服务 | 地址 | 说明 |
|------|------|------|
| 应用（Nginx反代） | http://localhost:8088/ | 推荐使用 |
| Spring直连 | http://localhost:8080/ | 开发调试 |
| RabbitMQ管理台 | http://localhost:15673/ | guest/guest |

---

## 项目结构

```
Sensor_Spring/
├── db/init/                    # 数据库初始化脚本
│   ├── V001__Create_Users.sql
│   ├── V002__Create_Files.sql
│   ├── V003__Create_Audit_Logs.sql
│   └── V004__Create_Membership_Tables.sql
├── nginx/
│   ├── conf.d/default.conf     # Nginx配置
│   └── html/                   # 静态资源目录
├── src/main/java/
│   └── com/example/sensorspring/
│       ├── cache/              # 缓存管理
│       ├── config/             # 配置类
│       ├── controller/         # 控制器
│       ├── dto/                # 数据传输对象
│       ├── entity/             # 实体类
│       ├── exception/          # 异常处理
│       ├── monitor/            # 性能监控
│       ├── repository/         # 数据访问层
│       ├── security/           # 安全认证
│       ├── service/            # 业务逻辑层
│       └── util/               # 工具类
└── src/main/resources/
    └── application.yml         # 应用配置
```

---

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/modality
    username: sensor
    password: Sensor#Passw0rd!
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6380
```

### JWT配置
```yaml
jwt:
  secret: <your-secret-key>
  access-ttl-minutes: 15
  refresh-ttl-days: 7
```

### 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 2GB
      max-request-size: 2GB

app:
  upload-root: ./uploads
```

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Spring Boot 3.3.2 |
| 安全 | Spring Security + JWT |
| 数据库 | MySQL 8 + Spring Data JPA |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 4.2 |
| 监控 | Spring Actuator + Prometheus |
| 系统监控 | OSHI 6.4.0 |
| 构建 | Gradle 8.12 |
| 容器化 | Docker Compose |

---

## 清理
```bash
# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷
docker-compose down -v
```

---

## 开发说明

### 密码规则
- 最少6个字符
- 必须包含大写字母
- 必须包含小写字母
- 必须包含数字

### 用户名规则
- 3-20个字符
- 只允许字母、数字、下划线

### 手机号验证
- 支持中国大陆手机号格式

---

> 提示：若你希望让前端静态资源由 Nginx 托管，把构建产物放到 `nginx/html`，通过 `http://localhost:8088/` 访问。
