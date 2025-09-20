#!/usr/bin/env bash
set -euo pipefail

# Start infra if not already up
docker compose ps >/dev/null 2>&1 || true
if ! docker compose ps --status running | grep -q 'sensor_mysql'; then
  echo "Starting infra containers ..."
  docker compose up -d mysql redis rabbitmq nginx
fi

# Prepare local dirs
export APP_UPLOAD_ROOT="${APP_UPLOAD_ROOT:-$(pwd)/uploads}"
export LOG_PATH="${LOG_PATH:-$(pwd)/logs}"
mkdir -p "$APP_UPLOAD_ROOT" "$LOG_PATH"

# Secrets
export JWT_SECRET="${JWT_SECRET:-please-change-this-64-hex-or-long-secret}"

# Ensure Gradle wrapper (8.10.0). Falls back to system Gradle if wrapper missing.
if [ ! -f "./gradlew" ]; then
  if command -v gradle >/dev/null 2>&1; then
    echo "Generating Gradle wrapper (8.10.0) using system Gradle ..."
    gradle wrapper --gradle-version 8.10.0
  else
    echo "No Gradle wrapper found and no system 'gradle' to generate one."
    exit 1
  fi
fi

./gradlew clean bootRun --args='--spring.profiles.active=local'
