#!/bin/bash

# CMS前端项目安装脚本
# 用于快速设置开发环境

set -e

echo "🚀 开始设置CMS前端开发环境..."

# 检查Node.js版本
echo "📋 检查Node.js版本..."
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js，请先安装Node.js 18+版本"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ 错误: Node.js版本过低，需要18+版本，当前版本: $(node -v)"
    exit 1
fi

echo "✅ Node.js版本检查通过: $(node -v)"

# 检查npm版本
echo "📋 检查npm版本..."
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm"
    exit 1
fi

NPM_VERSION=$(npm -v | cut -d'.' -f1)
if [ "$NPM_VERSION" -lt 9 ]; then
    echo "❌ 错误: npm版本过低，需要9+版本，当前版本: $(npm -v)"
    exit 1
fi

echo "✅ npm版本检查通过: $(npm -v)"

# 安装根目录依赖
echo "📦 安装根目录依赖..."
npm install

# 安装共享库依赖
echo "📦 安装共享库依赖..."
cd shared
npm install
echo "🔨 构建共享库..."
npm run build
cd ..

# 安装管理后台依赖
echo "📦 安装管理后台依赖..."
cd admin
npm install
cd ..

# 安装文档门户依赖
echo "📦 安装文档门户依赖..."
cd docs-portal
npm install
cd ..

# 设置Git hooks
echo "🔧 设置Git hooks..."
if [ -d ".git" ]; then
    npx husky install
    npx husky add .husky/pre-commit "npx lint-staged"
    npx husky add .husky/commit-msg "npx commitlint --edit \$1"
    echo "✅ Git hooks设置完成"
else
    echo "⚠️  警告: 未找到Git仓库，跳过Git hooks设置"
fi

# 创建环境变量文件
echo "🔧 创建环境变量文件..."

# 管理后台环境变量
if [ ! -f "admin/.env.local" ]; then
    cat > admin/.env.local << EOF
# 开发环境配置
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=CMS管理后台
VITE_APP_VERSION=1.0.0
VITE_ENABLE_MOCK=false
VITE_ENABLE_DEVTOOLS=true
EOF
    echo "✅ 创建管理后台环境变量文件: admin/.env.local"
fi

# 文档门户环境变量
if [ ! -f "docs-portal/.env.local" ]; then
    cat > docs-portal/.env.local << EOF
# 开发环境配置
API_BASE_URL=http://localhost:8080
ENABLE_ANALYTICS=false
ENABLE_FEEDBACK=true
ALGOLIA_APP_ID=YOUR_APP_ID
ALGOLIA_API_KEY=YOUR_SEARCH_API_KEY
EOF
    echo "✅ 创建文档门户环境变量文件: docs-portal/.env.local"
fi

# 运行代码检查
echo "🔍 运行代码检查..."
npm run lint

echo ""
echo "🎉 CMS前端开发环境设置完成！"
echo ""
echo "📚 可用命令:"
echo "  npm run dev          - 启动所有开发服务器"
echo "  npm run dev:admin    - 启动管理后台开发服务器"
echo "  npm run dev:docs     - 启动文档门户开发服务器"
echo "  npm run build        - 构建所有项目"
echo "  npm run test         - 运行所有测试"
echo "  npm run lint         - 运行代码检查"
echo ""
echo "🌐 访问地址:"
echo "  管理后台: http://localhost:3000"
echo "  文档门户: http://localhost:3001"
echo ""
echo "📖 更多信息请查看 README.md 文件"