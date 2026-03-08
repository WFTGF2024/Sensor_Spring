# Sensor Spring API 文档

> **Base URL**: `http://localhost:8080` (直连) 或 `http://localhost:8088` (Nginx代理)
> 
> **认证方式**: JWT Bearer Token

---

## 目录

1. [认证接口](#1-认证接口)
2. [用户接口](#2-用户接口)
3. [会员接口](#3-会员接口)
4. [监控接口](#4-监控接口)
5. [Actuator接口](#5-actuator接口)
6. [错误响应](#6-错误响应)

---

## 1. 认证接口

### 1.1 用户注册

**POST** `/api/auth/register`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123456",
  "phone": "13800138000",
  "qq": "123456789",
  "wechat": "wechat_id"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名，3-20字符，仅字母数字下划线 |
| email | string | 是 | 邮箱地址 |
| password | string | 是 | 密码，至少6字符，需包含大小写字母和数字 |
| phone | string | 否 | 手机号（中国大陆格式） |
| qq | string | 否 | QQ号 |
| wechat | string | 否 | 微信号 |

**成功响应** (200):
```json
{
  "message": "注册成功"
}
```

**错误响应**:
```json
{
  "message": "用户名已存在"
}
```

---

### 1.2 用户登录

**POST** `/api/auth/login`

**请求头**:
```
Content-Type: application/json
```

**请求体**:
```json
{
  "usernameOrEmail": "testuser",
  "password": "Test123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| usernameOrEmail | string | 是 | 用户名或邮箱 |
| password | string | 是 | 密码 |

**成功响应** (200):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| accessToken | string | 访问令牌，有效期15分钟 |
| refreshToken | string | 刷新令牌，有效期7天 |
| tokenType | string | 令牌类型，固定为 "Bearer" |

---

### 1.3 刷新Token

**POST** `/api/auth/refresh`

**请求参数**:
```
?refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

**成功响应** (200):
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

---

### 1.4 登出

**POST** `/api/auth/logout`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "message": "登出成功"
}
```

---

## 2. 用户接口

### 2.1 获取当前用户信息

**GET** `/api/users/me`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000",
  "passwordHash": "$2a$12$...",
  "role": "USER",
  "qq": "123456789",
  "wechat": "wechat_id",
  "point": 0,
  "createdAt": "2026-03-08T14:19:15Z",
  "updatedAt": "2026-03-08T14:19:15Z"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |
| username | string | 用户名 |
| email | string | 邮箱 |
| phone | string | 手机号 |
| passwordHash | string | 密码哈希（不暴露给前端） |
| role | string | 角色：USER / ADMIN |
| qq | string | QQ号 |
| wechat | string | 微信号 |
| point | number | 积分 |
| createdAt | string | 创建时间（ISO 8601） |
| updatedAt | string | 更新时间（ISO 8601） |

---

### 2.2 获取所有用户（管理员）

**GET** `/api/users`

**请求头**:
```
Authorization: Bearer <accessToken>
```

> 需要 ADMIN 角色

**成功响应** (200):
```json
[
  {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER",
    "createdAt": "2026-03-08T14:19:15Z"
  }
]
```

---

## 3. 会员接口

### 3.1 获取会员信息

**GET** `/api/membership/info`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "membershipId": 1,
  "userId": 1,
  "levelId": 1,
  "levelName": "普通用户",
  "levelCode": "free",
  "storageLimit": 1073741824,
  "storageLimitFormatted": "1.00 GB",
  "maxFileSize": 52428800,
  "maxFileSizeFormatted": "50.00 MB",
  "maxFileCount": 100,
  "storageUsed": 0,
  "storageUsedFormatted": "0 B",
  "fileCount": 0,
  "storageUsagePercentage": 0.0,
  "storageFull": false,
  "startDate": "2026-03-08T14:44:04Z",
  "endDate": null,
  "endDateFormatted": "永久",
  "active": true,
  "canShareFiles": true,
  "canCreatePublicLinks": false
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| membershipId | number | 会员记录ID |
| userId | number | 用户ID |
| levelId | number | 会员等级ID |
| levelName | string | 会员等级名称 |
| levelCode | string | 会员等级代码：free/silver/gold/diamond |
| storageLimit | number | 存储限制（字节） |
| storageLimitFormatted | string | 存储限制（格式化） |
| maxFileSize | number | 单文件大小限制（字节） |
| maxFileSizeFormatted | string | 单文件大小限制（格式化） |
| maxFileCount | number | 文件数量限制 |
| storageUsed | number | 已使用存储（字节） |
| storageUsedFormatted | string | 已使用存储（格式化） |
| fileCount | number | 文件数量 |
| storageUsagePercentage | number | 存储使用百分比 |
| storageFull | boolean | 存储是否已满 |
| startDate | string | 会员开始时间 |
| endDate | string | 会员结束时间（null表示永久） |
| endDateFormatted | string | 会员结束时间（格式化） |
| active | boolean | 是否激活 |
| canShareFiles | boolean | 是否可以分享文件 |
| canCreatePublicLinks | boolean | 是否可以创建公开链接 |

---

### 3.2 获取所有会员等级

**GET** `/api/membership/levels`

> 公开接口，无需认证

**成功响应** (200):
```json
[
  {
    "id": 1,
    "levelName": "普通用户",
    "levelCode": "free",
    "displayOrder": 1,
    "description": "免费用户，基础文件存储",
    "storageLimit": 1073741824,
    "maxFileSize": 52428800,
    "maxFileCount": 100,
    "canShareFiles": true,
    "canCreatePublicLinks": false,
    "isActive": true
  },
  {
    "id": 2,
    "levelName": "白银会员",
    "levelCode": "silver",
    "displayOrder": 2,
    "description": "白银会员，支持文件分享",
    "storageLimit": 5368709120,
    "maxFileSize": 104857600,
    "maxFileCount": 500,
    "canShareFiles": true,
    "canCreatePublicLinks": false,
    "isActive": true
  },
  {
    "id": 3,
    "levelName": "黄金会员",
    "levelCode": "gold",
    "displayOrder": 3,
    "description": "黄金会员，支持公开链接",
    "storageLimit": 10737418240,
    "maxFileSize": 209715200,
    "maxFileCount": 1000,
    "canShareFiles": true,
    "canCreatePublicLinks": true,
    "isActive": true
  },
  {
    "id": 4,
    "levelName": "钻石会员",
    "levelCode": "diamond",
    "displayOrder": 4,
    "description": "钻石会员，最高权限",
    "storageLimit": 53687091200,
    "maxFileSize": 1073741824,
    "maxFileCount": 10000,
    "canShareFiles": true,
    "canCreatePublicLinks": true,
    "isActive": true
  }
]
```

---

### 3.3 升级会员

**POST** `/api/membership/upgrade`

**请求头**:
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**请求体**:
```json
{
  "levelId": 2,
  "durationDays": 30
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| levelId | number | 是 | 目标会员等级ID |
| durationDays | number | 是 | 购买天数 |

**成功响应** (200):
```json
{
  "membershipId": 1,
  "levelCode": "silver",
  "levelName": "白银会员",
  "endDate": "2026-04-07T14:44:04Z",
  "message": "升级成功"
}
```

---

### 3.4 续费会员

**POST** `/api/membership/renew`

**请求头**:
```
Authorization: Bearer <accessToken>
Content-Type: application/json
```

**请求体**:
```json
{
  "durationDays": 30
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| durationDays | number | 是 | 续费天数 |

**成功响应** (200):
```json
{
  "membershipId": 1,
  "levelCode": "silver",
  "endDate": "2026-05-07T14:44:04Z",
  "message": "续费成功"
}
```

---

### 3.5 取消自动续费

**POST** `/api/membership/cancel`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "message": "已取消自动续费"
}
```

---

### 3.6 获取会员历史

**GET** `/api/membership/history`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**请求参数**:
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| page | number | 0 | 页码（从0开始） |
| size | number | 20 | 每页数量 |

**成功响应** (200):
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "actionType": "upgrade",
      "actionDetail": "升级到白银会员",
      "oldLevelId": 1,
      "newLevelId": 2,
      "createdAt": "2026-03-08T14:44:04Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

---

## 4. 监控接口

### 4.1 健康检查

**GET** `/api/monitor/health`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "status": "UP",
  "timestamp": "2026-03-08T15:00:00Z",
  "services": {
    "database": "UP",
    "redis": "UP",
    "rabbitmq": "UP"
  }
}
```

---

### 4.2 获取所有统计

**GET** `/api/monitor/stats`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "cache": {
    "hits": 100,
    "misses": 20,
    "hitRate": 0.833
  },
  "database": {
    "totalQueries": 500,
    "avgQueryTime": 15.5,
    "failedQueries": 0
  },
  "system": {
    "cpuUsage": 25.5,
    "memoryUsage": 60.2,
    "diskUsage": 45.0
  }
}
```

---

### 4.3 获取缓存统计

**GET** `/api/monitor/cache`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "hits": 100,
  "misses": 20,
  "hitRate": 0.833,
  "evictions": 5,
  "size": 150
}
```

---

### 4.4 获取数据库统计

**GET** `/api/monitor/database`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "totalQueries": 500,
  "selectQueries": 400,
  "insertQueries": 50,
  "updateQueries": 30,
  "deleteQueries": 20,
  "avgQueryTime": 15.5,
  "maxQueryTime": 150,
  "failedQueries": 0
}
```

---

### 4.5 获取系统统计

**GET** `/api/monitor/system`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "cpuUsage": 25.5,
  "memoryUsage": 60.2,
  "memoryTotal": 17179869184,
  "memoryUsed": 10334765018,
  "diskUsage": 45.0,
  "diskTotal": 107374182400,
  "diskUsed": 48318382080,
  "uptime": 3600
}
```

---

### 4.6 清除缓存

**POST** `/api/monitor/cache/clear`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "message": "缓存已清除"
}
```

---

### 4.7 重置统计

**POST** `/api/monitor/stats/reset`

**请求头**:
```
Authorization: Bearer <accessToken>
```

**成功响应** (200):
```json
{
  "message": "统计已重置"
}
```

---

## 5. Actuator接口

> 公开接口，无需认证

### 5.1 健康检查

**GET** `/actuator/health`

**成功响应** (200):
```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

---

### 5.2 应用信息

**GET** `/actuator/info`

**成功响应** (200):
```json
{
  "app": {
    "name": "sensor-spring-ha-mq",
    "version": "1.1.0"
  }
}
```

---

### 5.3 Prometheus指标

**GET** `/actuator/prometheus`

**成功响应** (200):
```
# HELP jvm_memory_used_bytes Used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",} 1.234567E8
...
```

---

### 5.4 Metrics指标

**GET** `/actuator/metrics`

**成功响应** (200):
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "process.cpu.usage",
    "http.server.requests"
  ]
}
```

---

## 6. 错误响应

### 标准错误格式

```json
{
  "message": "错误描述",
  "status": 400,
  "timestamp": "2026-03-08T15:00:00Z"
}
```

### HTTP状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证/Token无效 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如用户名已存在） |
| 413 | 请求体过大 |
| 429 | 请求频率超限 |
| 500 | 服务器内部错误 |
| 507 | 存储空间不足 |

### 常见错误示例

**认证失败** (401):
```json
{
  "message": "Token无效或已过期"
}
```

**权限不足** (403):
```json
{
  "message": "权限不足"
}
```

**资源不存在** (404):
```json
{
  "message": "用户不存在"
}
```

**参数验证失败** (400):
```json
{
  "message": "密码必须包含至少一个大写字母"
}
```

**存储空间不足** (507):
```json
{
  "message": "存储空间不足，当前已使用 1.00 GB / 1.00 GB"
}
```

---

## 附录：会员等级对照表

| 等级 | 代码 | 存储空间 | 单文件限制 | 文件数量 | 公开链接 |
|------|------|----------|-----------|----------|----------|
| 普通用户 | free | 1 GB | 50 MB | 100 | 否 |
| 白银会员 | silver | 5 GB | 100 MB | 500 | 否 |
| 黄金会员 | gold | 10 GB | 200 MB | 1,000 | 是 |
| 钻石会员 | diamond | 50 GB | 1 GB | 10,000 | 是 |

---

## 附录：PowerShell 调用示例

### 登录获取Token
```powershell
$body = @{
    usernameOrEmail = "testuser"
    password = "Test123456"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"

$token = $response.accessToken
```

### 使用Token访问接口
```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/users/me" `
    -Method Get `
    -Headers $headers
```

### 获取会员信息
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/membership/info" `
    -Method Get `
    -Headers $headers | ConvertTo-Json -Depth 5
```
