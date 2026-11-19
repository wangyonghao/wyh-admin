#!/bin/bash

# 如果发生错误，则退出
set -e

# 1. 设置版本号，如果未提供第一个参数，则默认为 "latest"
VERSION=${1:-latest}
echo "Using version: $VERSION"

# 2. 定义镜像名称
ADMIN_IMAGE="wangyonghao/wyh-admin"
SCHEDULE_IMAGE="wangyonghao/wyh-admin-job"

# 3. 为镜像打标签
echo "Tagging images..."
docker tag "${ADMIN_IMAGE}:latest" "${ADMIN_IMAGE}:${VERSION}"
docker tag "${SCHEDULE_IMAGE}:latest" "${SCHEDULE_IMAGE}:${VERSION}"
echo "Images tagged successfully."

# 4. 推送镜像到仓库
echo "Pushing images to repository..."
docker push "${ADMIN_IMAGE}:${VERSION}"
docker push "${SCHEDULE_IMAGE}:${VERSION}"

# 如果版本不是 "latest"，也推送 "latest" 标签
if [ "$VERSION" != "latest" ]; then
    echo "Pushing 'latest' tag as well..."
    docker push "${ADMIN_IMAGE}:latest"
    docker push "${SCHEDULE_IMAGE}:latest"
fi

echo "Images pushed successfully."
