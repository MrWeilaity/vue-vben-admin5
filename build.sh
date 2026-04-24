#!/bin/bash

set -e
echo "🚀 开始下载依赖..."

pnpm install

echo "🚀 开始打包前端..."

pnpm build:play

echo "📦 打包完成"

docker build -t vben-web .
