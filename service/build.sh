#!/bin/bash

set -e

echo "🚀 开始打包项目..."

# 1. Maven 打包

mvn clean package -DskipTests

echo "📦 打包完成"

# 2. 构建 Docker 镜像

docker build -t vben-service .

echo "🐳 Docker 镜像构建完成"
