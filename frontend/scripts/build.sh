#!/bin/bash

# CMS前端项目构建脚本
# 用于生产环境构建

set -e

echo "🏗️  开始构建CMS前端项目..."

# 检查环境
echo "📋 检查构建环境..."
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm"
    exit 1
fi

echo "✅ 环境检查通过"

# 清理旧的构建文件
echo "🧹 清理旧的构建文件..."
npm run clean

# 安装依赖
echo "📦 安装依赖..."
npm run install:all

# 运行代码检查
echo "🔍 运行代码检查..."
npm run lint

# 运行测试
echo "🧪 运行测试..."
npm run test

# 构建共享库
echo "🔨 构建共享库..."
cd shared
npm run build
cd ..

# 构建管理后台
echo "🔨 构建管理后台..."
cd admin
npm run build
cd ..

# 构建文档门户
echo "🔨 构建文档门户..."
cd docs-portal
npm run build
cd ..

# 生成构建报告
echo "📊 生成构建报告..."
BUILD_TIME=$(date '+%Y-%m-%d %H:%M:%S')
BUILD_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

cat > build-report.json << EOF
{
  "buildTime": "$BUILD_TIME",
  "buildHash": "$BUILD_HASH",
  "nodeVersion": "$(node -v)",
  "npmVersion": "$(npm -v)",
  "projects": {
    "shared": {
      "status": "success",
      "outputDir": "shared/dist"
    },
    "admin": {
      "status": "success",
      "outputDir": "admin/dist"
    },
    "docs-portal": {
      "status": "success",
      "outputDir": "docs-portal/build"
    }
  }
}
EOF

echo ""
echo "🎉 构建完成！"
echo ""
echo "📁 构建输出:"
echo "  共享库: shared/dist/"
echo "  管理后台: admin/dist/"
echo "  文档门户: docs-portal/build/"
echo ""
echo "📊 构建报告: build-report.json"
echo "🕐 构建时间: $BUILD_TIME"
echo "📝 Git提交: $BUILD_HASH"