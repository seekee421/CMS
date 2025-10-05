# 安装指南

本指南将帮助您快速安装和部署CMS系统。

## 系统要求

在开始安装之前，请确保您的系统满足以下要求：

### 后端要求
- **Java**: JDK 17 或更高版本
- **数据库**: MySQL 8.0+ 或 PostgreSQL 13+
- **内存**: 最少 2GB RAM（推荐 4GB+）
- **存储**: 最少 10GB 可用空间

### 前端要求
- **Node.js**: 18.0 或更高版本
- **npm**: 8.0 或更高版本（或 yarn 1.22+）

## 安装方式

CMS提供多种安装方式，您可以根据需要选择：

### 方式一：Docker 安装（推荐）

Docker安装是最简单快捷的方式：

```bash
# 1. 克隆项目
git clone https://github.com/cms-team/cms.git
cd cms

# 2. 启动服务
docker-compose up -d

# 3. 等待服务启动完成
docker-compose logs -f
```

服务启动后，访问：
- 管理后台：http://localhost:3000
- 文档门户：http://localhost:3001
- API接口：http://localhost:8080

### 方式二：手动安装

#### 1. 安装后端

```bash
# 克隆项目
git clone https://github.com/cms-team/cms.git
cd cms/backend

# 配置数据库
cp src/main/resources/application.properties.example src/main/resources/application.properties
# 编辑配置文件，设置数据库连接信息

# 构建项目
./mvnw clean package

# 运行应用
java -jar target/cms-backend-1.0.0.jar
```

#### 2. 安装前端

```bash
# 进入前端目录
cd ../frontend

# 安装管理后台依赖
cd admin
npm install
npm run build

# 安装文档门户依赖
cd ../docs-portal
npm install
npm run build
```

#### 3. 配置Web服务器

使用Nginx配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 管理后台
    location /admin {
        root /path/to/cms/frontend/admin/dist;
        try_files $uri $uri/ /index.html;
    }

    # 文档门户
    location / {
        root /path/to/cms/frontend/docs-portal/build;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 方式三：云平台部署

#### Vercel部署（前端）

```bash
# 安装Vercel CLI
npm i -g vercel

# 部署管理后台
cd frontend/admin
vercel

# 部署文档门户
cd ../docs-portal
vercel
```

#### Railway部署（后端）

1. 连接GitHub仓库到Railway
2. 设置环境变量
3. 自动部署

## 数据库配置

### MySQL配置

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/cms?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
```

### PostgreSQL配置

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cms
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## 环境变量配置

创建 `.env` 文件：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cms
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400

# 文件上传配置
UPLOAD_PATH=/path/to/uploads
MAX_FILE_SIZE=10MB

# 邮件配置
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Redis配置（可选）
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

## 初始化数据

首次启动后，系统会自动创建必要的数据表。您可以通过以下方式初始化基础数据：

### 1. 创建管理员账户

访问 `http://localhost:8080/api/setup` 创建初始管理员账户。

### 2. 导入示例数据（可选）

```bash
# 导入示例数据
curl -X POST http://localhost:8080/api/setup/sample-data \
  -H "Content-Type: application/json" \
  -d '{"includeSampleContent": true}'
```

## 验证安装

安装完成后，请验证以下功能：

### 1. 后端API测试

```bash
# 健康检查
curl http://localhost:8080/api/health

# 获取系统信息
curl http://localhost:8080/api/system/info
```

### 2. 前端访问测试

- 访问管理后台：http://localhost:3000
- 访问文档门户：http://localhost:3001
- 检查页面是否正常加载

### 3. 功能测试

1. 登录管理后台
2. 创建测试文档
3. 检查文档门户显示
4. 测试搜索功能

## 常见问题

### Q: 数据库连接失败

**A**: 检查数据库服务是否启动，用户名密码是否正确，防火墙设置是否允许连接。

### Q: 前端页面空白

**A**: 检查API地址配置是否正确，浏览器控制台是否有错误信息。

### Q: 文件上传失败

**A**: 检查上传目录权限，文件大小限制配置。

### Q: 邮件发送失败

**A**: 检查SMTP配置，确认邮箱服务商的安全设置。

## 下一步

安装完成后，建议您：

1. 阅读[配置说明](./configuration)了解详细配置选项
2. 查看[第一步](./first-steps)开始使用系统
3. 浏览[用户指南](../user-guide/overview)了解所有功能

---

:::tip 提示
如果遇到安装问题，请查看[故障排除](../developer-guide/troubleshooting)或在GitHub上提交Issue。
:::

:::warning 注意
生产环境部署时，请务必修改默认密码和密钥，启用HTTPS，配置防火墙。
:::