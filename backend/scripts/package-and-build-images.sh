#!/bin/bash

# 如果发生错误，则退出
set -e

# 1. 使用 Maven 打包应用程序
echo "Packaging applications with Maven..."
# 指定根 pom.xml 文件并跳过测试
mvn -f ../pom.xml clean package -DskipTests

# 2. 构建 Docker 镜像
echo "Building Docker images..."

# 构建 tide-admin-server 镜像
echo "Building tide-admin-server image..."
container build -f ../tide-admin-server/Dockerfile -t wangyonghao/wyh-admin ../tide-admin-server

# 构建 continew-schedule-server 镜像
echo "Building continew-schedule-server image..."
container build -f ../continew-schedule-server/Dockerfile -t wangyonghao/wyh-admin-job ../continew-schedule-server

echo "Docker images built successfully."