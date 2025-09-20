# 启动依赖容器
try { docker compose ps | Out-Null } catch {}
if (-not (docker compose ps --status running | Select-String "sensor_mysql")) {
  Write-Host "Starting infra containers ..."
  docker compose up -d mysql redis rabbitmq nginx | Out-Null
}

# 设置环境变量（如果未设置）
if (-not $env:APP_UPLOAD_ROOT) { $env:APP_UPLOAD_ROOT = (Join-Path (Get-Location) "uploads") }
if (-not $env:LOG_PATH) { $env:LOG_PATH = (Join-Path (Get-Location) "logs") }
if (-not $env:JWT_SECRET) { $env:JWT_SECRET = "please-change-this-64-hex-secret" }

# 创建目录
New-Item -ItemType Directory -Force -Path $env:APP_UPLOAD_ROOT | Out-Null
New-Item -ItemType Directory -Force -Path $env:LOG_PATH | Out-Null

# 确保 Gradle wrapper 存在
if (-not (Test-Path ".\gradlew")) {
  if (Get-Command gradle -ErrorAction SilentlyContinue) {
    Write-Host "Generating Gradle wrapper (8.10.0) using system Gradle ..."
    gradle wrapper --gradle-version 8.10.0 | Out-Null
  } else {
    Write-Error "No gradle wrapper found and no system 'gradle' to generate one."
    exit 1
  }
}

# 启动 Spring Boot 应用
./gradlew clean bootRun --args="--spring.profiles.active=local"
