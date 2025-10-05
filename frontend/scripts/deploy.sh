#!/bin/bash

# CMS前端项目部署脚本
# 用于部署到生产环境

set -e

# 配置变量
DEPLOY_ENV=${1:-production}
DEPLOY_TARGET=${2:-local}

echo "🚀 开始部署CMS前端项目..."
echo "📋 部署环境: $DEPLOY_ENV"
echo "📋 部署目标: $DEPLOY_TARGET"

# 检查参数
if [ "$DEPLOY_ENV" != "staging" ] && [ "$DEPLOY_ENV" != "production" ]; then
    echo "❌ 错误: 无效的部署环境，支持: staging, production"
    exit 1
fi

# 构建项目
echo "🏗️  构建项目..."
./scripts/build.sh

# 根据部署目标执行不同的部署策略
case $DEPLOY_TARGET in
    "local")
        echo "📁 本地部署..."
        # 创建部署目录
        DEPLOY_DIR="deploy/$DEPLOY_ENV"
        mkdir -p "$DEPLOY_DIR"
        
        # 复制构建文件
        cp -r admin/dist "$DEPLOY_DIR/admin"
        cp -r docs-portal/build "$DEPLOY_DIR/docs-portal"
        cp -r shared/dist "$DEPLOY_DIR/shared"
        
        echo "✅ 本地部署完成: $DEPLOY_DIR"
        ;;
        
    "nginx")
        echo "🌐 Nginx部署..."
        # 检查Nginx配置
        if [ ! -f "nginx.conf" ]; then
            echo "❌ 错误: 未找到nginx.conf配置文件"
            exit 1
        fi
        
        # 部署到Nginx目录
        NGINX_ROOT="/usr/share/nginx/html/cms"
        sudo mkdir -p "$NGINX_ROOT"
        sudo cp -r admin/dist "$NGINX_ROOT/admin"
        sudo cp -r docs-portal/build "$NGINX_ROOT/docs"
        sudo cp nginx.conf /etc/nginx/sites-available/cms
        sudo ln -sf /etc/nginx/sites-available/cms /etc/nginx/sites-enabled/
        sudo nginx -t && sudo systemctl reload nginx
        
        echo "✅ Nginx部署完成"
        ;;
        
    "docker")
        echo "🐳 Docker部署..."
        # 构建Docker镜像
        docker build -t cms-admin:$DEPLOY_ENV -f admin/Dockerfile admin/
        docker build -t cms-docs:$DEPLOY_ENV -f docs-portal/Dockerfile docs-portal/
        
        # 运行容器
        docker-compose -f docker-compose.$DEPLOY_ENV.yml up -d
        
        echo "✅ Docker部署完成"
        ;;
        
    "s3")
        echo "☁️  AWS S3部署..."
        # 检查AWS CLI
        if ! command -v aws &> /dev/null; then
            echo "❌ 错误: 未找到AWS CLI"
            exit 1
        fi
        
        # 部署到S3
        aws s3 sync admin/dist s3://cms-admin-$DEPLOY_ENV --delete
        aws s3 sync docs-portal/build s3://cms-docs-$DEPLOY_ENV --delete
        
        # 清除CloudFront缓存
        if [ -n "$CLOUDFRONT_DISTRIBUTION_ID" ]; then
            aws cloudfront create-invalidation --distribution-id $CLOUDFRONT_DISTRIBUTION_ID --paths "/*"
        fi
        
        echo "✅ S3部署完成"
        ;;
        
    "vercel")
        echo "▲ Vercel部署..."
        # 检查Vercel CLI
        if ! command -v vercel &> /dev/null; then
            echo "❌ 错误: 未找到Vercel CLI"
            exit 1
        fi
        
        # 部署管理后台
        cd admin
        vercel --prod
        cd ..
        
        # 部署文档门户
        cd docs-portal
        vercel --prod
        cd ..
        
        echo "✅ Vercel部署完成"
        ;;
        
    *)
        echo "❌ 错误: 不支持的部署目标: $DEPLOY_TARGET"
        echo "支持的部署目标: local, nginx, docker, s3, vercel"
        exit 1
        ;;
esac

# 生成部署报告
DEPLOY_TIME=$(date '+%Y-%m-%d %H:%M:%S')
DEPLOY_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

cat > deploy-report.json << EOF
{
  "deployTime": "$DEPLOY_TIME",
  "deployHash": "$DEPLOY_HASH",
  "environment": "$DEPLOY_ENV",
  "target": "$DEPLOY_TARGET",
  "status": "success"
}
EOF

echo ""
echo "🎉 部署完成！"
echo "🕐 部署时间: $DEPLOY_TIME"
echo "📝 Git提交: $DEPLOY_HASH"
echo "📊 部署报告: deploy-report.json"

# 健康检查
if [ "$DEPLOY_TARGET" = "local" ] || [ "$DEPLOY_TARGET" = "nginx" ]; then
    echo ""
    echo "🔍 执行健康检查..."
    
    # 检查管理后台
    if [ -f "deploy/$DEPLOY_ENV/admin/index.html" ] || [ -f "/usr/share/nginx/html/cms/admin/index.html" ]; then
        echo "✅ 管理后台部署成功"
    else
        echo "❌ 管理后台部署失败"
    fi
    
    # 检查文档门户
    if [ -f "deploy/$DEPLOY_ENV/docs-portal/index.html" ] || [ -f "/usr/share/nginx/html/cms/docs/index.html" ]; then
        echo "✅ 文档门户部署成功"
    else
        echo "❌ 文档门户部署失败"
    fi
fi