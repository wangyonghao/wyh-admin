#!/bin/bash

# 如果发生错误，则退出
set -e

# 1. 使用 Maven 打包应用程序
echo "Packaging applications with Maven..."
# 指定根 pom.xml 文件并跳过测试
mvn -f ../pom.xml clean package -DskipTests

# 2. 构建 Docker 镜像
echo "Building Docker images..."

# 构建 wyh-admin 镜像
echo "Building wyh-admin image..."
container build -f ../wyh-admin-server/Dockerfile -t wangyonghao/wyh-admin ../wyh-admin-server

echo "Docker images built successfully."