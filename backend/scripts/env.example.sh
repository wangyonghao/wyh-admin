#!/usr/bin/env bash
# wyh-admin — 宿主机后端环境变量示例
#
# 与 backend/wyh-bootstrap/src/main/resources/config 里的 ${VAR} 对应。
# 只在宿主机跑后端时需要。Compose 里的 api 直接读 backend/docker/.env。
# 本地 Postgres / Redis 等由 backend/docker 的 Compose 提供，口令以该目录 .env 为准。
#
#   cp backend/scripts/env.example.sh backend/scripts/env.sh
#   # 编辑 env.sh：DB_PWD、REDIS_PWD、AES_KEY 与 backend/docker/.env 一致
#   set -a && source backend/scripts/env.sh && set +a
#   cd backend && mvn -pl wyh-bootstrap -am spring-boot:run
#
# env.sh 已被 backend/.gitignore 忽略，不要提交。
# IDEA：Run Configuration → Environment variables，填同名变量。

# application.yml：application.crypto.aes-key（必填，16、24 或 32 字节）
export AES_KEY='change-me-16char'

# application-dev.yml：spring.datasource
# 库名、用户须与 Postgres 中已创建的库一致。backend/docker 的示例是 postgres / postgres。
export DB_HOST='localhost'
export DB_PORT='5432'
export DB_NAME='postgres'
export DB_USER='postgres'
export DB_PWD='change-me'

# application-dev.yml：spring.data.redis
export REDIS_HOST='127.0.0.1'
export REDIS_PORT='6379'
export REDIS_PWD='change-me'
export REDIS_DB='15'

# application-dev.yml 已开启 Liquibase。要临时关掉：
# export SPRING_LIQUIBASE_ENABLED=false

# application-dev.yml / application-prod.yml：sa-token.jwt-secret-key
export WYH_JWT_SECRET='change-me-jwt-secret'

# 仅 application-prod.yml 需要：
# export WYH_RSA_PUBLIC_KEY=''
# export WYH_RSA_PRIVATE_KEY=''

# JDK 17（必填）。Homebrew 默认 openjdk 可能是 21/26，不钉死会编译/启动失败。
export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
if [[ -z "${JAVA_HOME}" && -d /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home ]]; then
  export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
fi
if [[ -n "${JAVA_HOME}" ]]; then
  export PATH="${JAVA_HOME}/bin:${PATH}"
fi


# GitHub OAuth App
export GITHUB_CLIENT_ID=
export GITHUB_CLIENT_SECRET=
