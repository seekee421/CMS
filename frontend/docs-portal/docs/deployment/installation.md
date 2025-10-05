# 系统安装部署指南

本指南详细介绍CMS系统的安装部署过程，包括环境准备、系统安装、配置和启动。

## 系统要求

### 硬件要求

#### 最低配置
- **CPU**: 2核心
- **内存**: 4GB RAM
- **存储**: 50GB 可用空间
- **网络**: 100Mbps

#### 推荐配置
- **CPU**: 4核心或更多
- **内存**: 8GB RAM或更多
- **存储**: 100GB SSD
- **网络**: 1Gbps

#### 生产环境配置
- **CPU**: 8核心或更多
- **内存**: 16GB RAM或更多
- **存储**: 500GB SSD
- **网络**: 1Gbps或更高

### 软件要求

#### 操作系统
- **Linux**: CentOS 7+, Ubuntu 18.04+, RHEL 7+
- **Windows**: Windows Server 2016+
- **macOS**: macOS 10.14+（仅用于开发环境）

#### 运行环境
- **Java**: JDK 11 或 JDK 17
- **Node.js**: 16.x 或 18.x
- **数据库**: MySQL 8.0+ 或 PostgreSQL 12+
- **Web服务器**: Nginx 1.18+ 或 Apache 2.4+

#### 可选组件
- **搜索引擎**: Elasticsearch 7.x+
- **缓存**: Redis 6.x+
- **消息队列**: RabbitMQ 3.8+

## 环境准备

### 1. 安装Java环境

#### CentOS/RHEL
```bash
# 安装OpenJDK 11
sudo yum install java-11-openjdk java-11-openjdk-devel

# 验证安装
java -version
javac -version
```

#### Ubuntu/Debian
```bash
# 更新包列表
sudo apt update

# 安装OpenJDK 11
sudo apt install openjdk-11-jdk

# 验证安装
java -version
javac -version
```

#### 配置JAVA_HOME
```bash
# 编辑环境变量
sudo vim /etc/profile

# 添加以下内容
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH=$PATH:$JAVA_HOME/bin

# 重新加载配置
source /etc/profile
```

### 2. 安装Node.js

#### 使用NodeSource仓库（推荐）
```bash
# CentOS/RHEL
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install nodejs

# Ubuntu/Debian
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install nodejs

# 验证安装
node --version
npm --version
```

#### 使用NVM（开发环境推荐）
```bash
# 安装NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# 重新加载shell
source ~/.bashrc

# 安装Node.js 18
nvm install 18
nvm use 18
nvm alias default 18
```

### 3. 安装数据库

#### MySQL 8.0安装

**CentOS/RHEL**:
```bash
# 添加MySQL仓库
sudo yum install mysql80-community-release-el7-3.noarch.rpm

# 安装MySQL
sudo yum install mysql-community-server

# 启动MySQL服务
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 获取临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 安全配置
sudo mysql_secure_installation
```

**Ubuntu/Debian**:
```bash
# 更新包列表
sudo apt update

# 安装MySQL
sudo apt install mysql-server

# 启动MySQL服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

#### PostgreSQL 12+安装

**CentOS/RHEL**:
```bash
# 安装PostgreSQL仓库
sudo yum install postgresql12-server postgresql12

# 初始化数据库
sudo /usr/pgsql-12/bin/postgresql-12-setup initdb

# 启动服务
sudo systemctl start postgresql-12
sudo systemctl enable postgresql-12
```

**Ubuntu/Debian**:
```bash
# 安装PostgreSQL
sudo apt install postgresql postgresql-contrib

# 启动服务
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 4. 安装Web服务器

#### Nginx安装
```bash
# CentOS/RHEL
sudo yum install nginx

# Ubuntu/Debian
sudo apt install nginx

# 启动服务
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### Apache安装
```bash
# CentOS/RHEL
sudo yum install httpd

# Ubuntu/Debian
sudo apt install apache2

# 启动服务
sudo systemctl start httpd  # CentOS/RHEL
sudo systemctl start apache2  # Ubuntu/Debian
sudo systemctl enable httpd   # CentOS/RHEL
sudo systemctl enable apache2 # Ubuntu/Debian
```

## 数据库配置

### MySQL配置

#### 1. 创建数据库和用户
```sql
-- 连接到MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE cms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER 'cms_user'@'localhost' IDENTIFIED BY 'strong_password';

-- 授权
GRANT ALL PRIVILEGES ON cms_db.* TO 'cms_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

#### 2. 优化MySQL配置
编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`：

```ini
[mysqld]
# 基本配置
port = 3306
bind-address = 127.0.0.1

# 字符集配置
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 性能优化
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 连接配置
max_connections = 200
max_connect_errors = 10000
wait_timeout = 28800

# 查询缓存
query_cache_type = 1
query_cache_size = 128M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

重启MySQL服务：
```bash
sudo systemctl restart mysql
```

### PostgreSQL配置

#### 1. 创建数据库和用户
```bash
# 切换到postgres用户
sudo -u postgres psql

-- 创建用户
CREATE USER cms_user WITH PASSWORD 'strong_password';

-- 创建数据库
CREATE DATABASE cms_db OWNER cms_user ENCODING 'UTF8';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE cms_db TO cms_user;

-- 退出
\q
```

#### 2. 配置PostgreSQL
编辑 `/etc/postgresql/12/main/postgresql.conf`：

```ini
# 连接配置
listen_addresses = 'localhost'
port = 5432
max_connections = 200

# 内存配置
shared_buffers = 2GB
effective_cache_size = 6GB
work_mem = 64MB
maintenance_work_mem = 512MB

# 日志配置
log_statement = 'all'
log_duration = on
log_min_duration_statement = 1000
```

编辑 `/etc/postgresql/12/main/pg_hba.conf`：

```
# 本地连接
local   all             all                                     peer
host    all             all             127.0.0.1/32            md5
host    all             all             ::1/128                 md5
```

重启PostgreSQL服务：
```bash
sudo systemctl restart postgresql
```

## 应用部署

### 1. 下载源码

#### 从Git仓库克隆
```bash
# 创建部署目录
sudo mkdir -p /opt/cms
sudo chown $USER:$USER /opt/cms
cd /opt/cms

# 克隆代码
git clone https://github.com/your-org/cms-system.git .

# 切换到指定版本
git checkout v1.0.0
```

#### 从发布包安装
```bash
# 下载发布包
wget https://releases.cms.example.com/v1.0.0/cms-system-1.0.0.tar.gz

# 解压
tar -xzf cms-system-1.0.0.tar.gz -C /opt/cms --strip-components=1
```

### 2. 后端部署

#### 配置应用
```bash
cd /opt/cms/backend

# 复制配置文件
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 编辑配置文件
vim src/main/resources/application.properties
```

配置文件示例：
```properties
# 服务器配置
server.port=8080
server.servlet.context-path=/api

# 数据库配置（MySQL）
spring.datasource.url=jdbc:mysql://localhost:3306/cms_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=cms_user
spring.datasource.password=strong_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# 文件上传配置
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB

# 文件存储路径
cms.file.upload-path=/opt/cms/uploads

# JWT配置
cms.jwt.secret=your-secret-key-here
cms.jwt.expiration=3600

# 日志配置
logging.level.com.cms=INFO
logging.file.name=/opt/cms/logs/cms.log
```

#### 构建应用
```bash
# 安装Maven依赖
./mvnw clean install -DskipTests

# 或者使用系统Maven
mvn clean install -DskipTests
```

#### 创建启动脚本
```bash
# 创建启动脚本
cat > /opt/cms/start-backend.sh << 'EOF'
#!/bin/bash

# 设置环境变量
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export SPRING_PROFILES_ACTIVE=production

# 设置JVM参数
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
cd /opt/cms/backend
nohup java $JAVA_OPTS -jar target/cms-backend-1.0.0.jar > /opt/cms/logs/backend.log 2>&1 &

echo $! > /opt/cms/backend.pid
echo "Backend started with PID: $(cat /opt/cms/backend.pid)"
EOF

# 设置执行权限
chmod +x /opt/cms/start-backend.sh
```

#### 创建停止脚本
```bash
cat > /opt/cms/stop-backend.sh << 'EOF'
#!/bin/bash

if [ -f /opt/cms/backend.pid ]; then
    PID=$(cat /opt/cms/backend.pid)
    if ps -p $PID > /dev/null; then
        kill $PID
        echo "Backend stopped (PID: $PID)"
        rm /opt/cms/backend.pid
    else
        echo "Backend is not running"
        rm /opt/cms/backend.pid
    fi
else
    echo "PID file not found"
fi
EOF

chmod +x /opt/cms/stop-backend.sh
```

### 3. 前端部署

#### 管理后台部署
```bash
cd /opt/cms/frontend/admin

# 安装依赖
npm install

# 配置环境变量
cat > .env.production << 'EOF'
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_UPLOAD_URL=http://localhost:8080/api/files/upload
REACT_APP_VERSION=1.0.0
EOF

# 构建生产版本
npm run build

# 复制到Web服务器目录
sudo cp -r build/* /var/www/html/admin/
```

#### 文档门户部署
```bash
cd /opt/cms/frontend/docs-portal

# 安装依赖
npm install

# 配置环境变量
cat > .env.production << 'EOF'
API_BASE_URL=http://localhost:8080/api
ALGOLIA_APP_ID=your-algolia-app-id
ALGOLIA_API_KEY=your-algolia-api-key
EOF

# 构建生产版本
npm run build

# 复制到Web服务器目录
sudo cp -r build/* /var/www/html/docs/
```

### 4. Web服务器配置

#### Nginx配置
创建 `/etc/nginx/sites-available/cms`：

```nginx
# 后端API代理
upstream cms_backend {
    server 127.0.0.1:8080;
}

# 管理后台
server {
    listen 80;
    server_name admin.cms.example.com;
    root /var/www/html/admin;
    index index.html;

    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://cms_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# 文档门户
server {
    listen 80;
    server_name docs.cms.example.com;
    root /var/www/html/docs;
    index index.html;

    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://cms_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置：
```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/cms /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重新加载配置
sudo systemctl reload nginx
```

#### Apache配置
创建 `/etc/apache2/sites-available/cms.conf`：

```apache
# 管理后台虚拟主机
<VirtualHost *:80>
    ServerName admin.cms.example.com
    DocumentRoot /var/www/html/admin
    
    # 启用重写模块
    RewriteEngine On
    
    # SPA路由支持
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule . /index.html [L]
    
    # API代理
    ProxyPreserveHost On
    ProxyPass /api/ http://localhost:8080/api/
    ProxyPassReverse /api/ http://localhost:8080/api/
    
    # 静态文件缓存
    <LocationMatch "\.(css|js|png|jpg|jpeg|gif|ico|svg)$">
        ExpiresActive On
        ExpiresDefault "access plus 1 year"
    </LocationMatch>
</VirtualHost>

# 文档门户虚拟主机
<VirtualHost *:80>
    ServerName docs.cms.example.com
    DocumentRoot /var/www/html/docs
    
    # 启用重写模块
    RewriteEngine On
    
    # SPA路由支持
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule . /index.html [L]
    
    # API代理
    ProxyPreserveHost On
    ProxyPass /api/ http://localhost:8080/api/
    ProxyPassReverse /api/ http://localhost:8080/api/
    
    # 静态文件缓存
    <LocationMatch "\.(css|js|png|jpg|jpeg|gif|ico|svg)$">
        ExpiresActive On
        ExpiresDefault "access plus 1 year"
    </LocationMatch>
</VirtualHost>
```

启用配置：
```bash
# 启用必要的模块
sudo a2enmod rewrite
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod expires

# 启用站点
sudo a2ensite cms

# 重新加载配置
sudo systemctl reload apache2
```

## 系统服务配置

### 创建Systemd服务

#### 后端服务
创建 `/etc/systemd/system/cms-backend.service`：

```ini
[Unit]
Description=CMS Backend Service
After=network.target mysql.service

[Service]
Type=forking
User=cms
Group=cms
WorkingDirectory=/opt/cms/backend
ExecStart=/opt/cms/start-backend.sh
ExecStop=/opt/cms/stop-backend.sh
PIDFile=/opt/cms/backend.pid
Restart=always
RestartSec=10

Environment=JAVA_HOME=/usr/lib/jvm/java-11-openjdk
Environment=SPRING_PROFILES_ACTIVE=production

[Install]
WantedBy=multi-user.target
```

#### 创建专用用户
```bash
# 创建CMS用户
sudo useradd -r -s /bin/false cms

# 设置目录权限
sudo chown -R cms:cms /opt/cms
sudo mkdir -p /opt/cms/logs /opt/cms/uploads
sudo chown -R cms:cms /opt/cms/logs /opt/cms/uploads
```

#### 启用服务
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启用服务
sudo systemctl enable cms-backend

# 启动服务
sudo systemctl start cms-backend

# 检查状态
sudo systemctl status cms-backend
```

## SSL证书配置

### 使用Let's Encrypt

#### 安装Certbot
```bash
# CentOS/RHEL
sudo yum install certbot python3-certbot-nginx

# Ubuntu/Debian
sudo apt install certbot python3-certbot-nginx
```

#### 获取证书
```bash
# 为Nginx获取证书
sudo certbot --nginx -d admin.cms.example.com -d docs.cms.example.com

# 为Apache获取证书
sudo certbot --apache -d admin.cms.example.com -d docs.cms.example.com
```

#### 自动续期
```bash
# 添加到crontab
sudo crontab -e

# 添加以下行
0 12 * * * /usr/bin/certbot renew --quiet
```

### 手动SSL配置

如果使用自己的SSL证书，需要修改Nginx配置：

```nginx
server {
    listen 443 ssl http2;
    server_name admin.cms.example.com;
    
    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;
    
    # SSL配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # 其他配置...
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name admin.cms.example.com docs.cms.example.com;
    return 301 https://$server_name$request_uri;
}
```

## 监控和日志

### 日志配置

#### 应用日志
应用日志位置：
- 后端日志：`/opt/cms/logs/cms.log`
- 启动日志：`/opt/cms/logs/backend.log`

#### Web服务器日志
- Nginx访问日志：`/var/log/nginx/access.log`
- Nginx错误日志：`/var/log/nginx/error.log`
- Apache访问日志：`/var/log/apache2/access.log`
- Apache错误日志：`/var/log/apache2/error.log`

#### 数据库日志
- MySQL错误日志：`/var/log/mysql/error.log`
- MySQL慢查询日志：`/var/log/mysql/slow.log`
- PostgreSQL日志：`/var/log/postgresql/postgresql-12-main.log`

### 日志轮转配置

创建 `/etc/logrotate.d/cms`：

```
/opt/cms/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 cms cms
    postrotate
        systemctl reload cms-backend
    endscript
}
```

### 监控脚本

创建健康检查脚本 `/opt/cms/health-check.sh`：

```bash
#!/bin/bash

# 检查后端服务
if curl -f http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "Backend: OK"
else
    echo "Backend: FAILED"
    # 发送告警邮件或重启服务
    systemctl restart cms-backend
fi

# 检查数据库连接
if mysql -u cms_user -p'strong_password' -e "SELECT 1" cms_db > /dev/null 2>&1; then
    echo "Database: OK"
else
    echo "Database: FAILED"
fi

# 检查磁盘空间
DISK_USAGE=$(df /opt/cms | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 80 ]; then
    echo "Disk usage: WARNING ($DISK_USAGE%)"
else
    echo "Disk usage: OK ($DISK_USAGE%)"
fi
```

添加到crontab：
```bash
# 每5分钟检查一次
*/5 * * * * /opt/cms/health-check.sh >> /opt/cms/logs/health-check.log 2>&1
```

## 备份策略

### 数据库备份

#### MySQL备份脚本
```bash
#!/bin/bash

BACKUP_DIR="/opt/cms/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="cms_db"
DB_USER="cms_user"
DB_PASS="strong_password"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/cms_db_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/cms_db_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "cms_db_*.sql.gz" -mtime +7 -delete

echo "Database backup completed: cms_db_$DATE.sql.gz"
```

#### PostgreSQL备份脚本
```bash
#!/bin/bash

BACKUP_DIR="/opt/cms/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="cms_db"
DB_USER="cms_user"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
pg_dump -U $DB_USER -h localhost $DB_NAME > $BACKUP_DIR/cms_db_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/cms_db_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "cms_db_*.sql.gz" -mtime +7 -delete

echo "Database backup completed: cms_db_$DATE.sql.gz"
```

### 文件备份

```bash
#!/bin/bash

BACKUP_DIR="/opt/cms/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz -C /opt/cms uploads/

# 备份配置文件
tar -czf $BACKUP_DIR/config_$DATE.tar.gz -C /opt/cms backend/src/main/resources/application.properties

# 删除30天前的文件备份
find $BACKUP_DIR -name "uploads_*.tar.gz" -mtime +30 -delete
find $BACKUP_DIR -name "config_*.tar.gz" -mtime +30 -delete

echo "File backup completed"
```

### 自动备份

添加到crontab：
```bash
# 每天凌晨2点备份数据库
0 2 * * * /opt/cms/backup-database.sh

# 每周日凌晨3点备份文件
0 3 * * 0 /opt/cms/backup-files.sh
```

## 故障排除

### 常见问题

#### 1. 后端服务启动失败

**检查步骤**：
```bash
# 查看服务状态
sudo systemctl status cms-backend

# 查看日志
sudo journalctl -u cms-backend -f

# 查看应用日志
tail -f /opt/cms/logs/cms.log
```

**常见原因**：
- 数据库连接失败
- 端口被占用
- 配置文件错误
- 权限问题

#### 2. 前端页面无法访问

**检查步骤**：
```bash
# 检查Web服务器状态
sudo systemctl status nginx
# 或
sudo systemctl status apache2

# 检查配置文件
sudo nginx -t
# 或
sudo apache2ctl configtest

# 查看错误日志
tail -f /var/log/nginx/error.log
# 或
tail -f /var/log/apache2/error.log
```

#### 3. 数据库连接问题

**检查步骤**：
```bash
# 测试数据库连接
mysql -u cms_user -p cms_db
# 或
psql -U cms_user -d cms_db

# 检查数据库服务状态
sudo systemctl status mysql
# 或
sudo systemctl status postgresql
```

#### 4. 文件上传失败

**检查步骤**：
```bash
# 检查上传目录权限
ls -la /opt/cms/uploads/

# 检查磁盘空间
df -h /opt/cms

# 检查文件大小限制
grep -i "max_file_size" /opt/cms/backend/src/main/resources/application.properties
```

### 性能优化

#### JVM调优
```bash
# 编辑启动脚本，调整JVM参数
JAVA_OPTS="-Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"
```

#### 数据库优化
```sql
-- MySQL优化查询
SHOW PROCESSLIST;
SHOW STATUS LIKE 'Slow_queries';
EXPLAIN SELECT * FROM documents WHERE title LIKE '%keyword%';

-- 添加索引
CREATE INDEX idx_documents_title ON documents(title);
CREATE INDEX idx_documents_category ON documents(category_id);
```

#### Web服务器优化
```nginx
# Nginx优化配置
worker_processes auto;
worker_connections 1024;

# 启用gzip压缩
gzip on;
gzip_vary on;
gzip_min_length 1024;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

# 启用缓存
location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

## 安全加固

### 系统安全

#### 防火墙配置
```bash
# 启用防火墙
sudo systemctl enable firewalld
sudo systemctl start firewalld

# 开放必要端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=22/tcp

# 重新加载配置
sudo firewall-cmd --reload
```

#### SSH安全配置
编辑 `/etc/ssh/sshd_config`：

```
# 禁用root登录
PermitRootLogin no

# 修改默认端口
Port 2222

# 禁用密码认证（推荐使用密钥认证）
PasswordAuthentication no

# 限制登录尝试
MaxAuthTries 3
```

### 应用安全

#### 数据库安全
```sql
-- 删除测试数据库
DROP DATABASE IF EXISTS test;

-- 删除匿名用户
DELETE FROM mysql.user WHERE User='';

-- 禁用远程root登录
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- 刷新权限
FLUSH PRIVILEGES;
```

#### Web服务器安全
```nginx
# 隐藏服务器版本信息
server_tokens off;

# 添加安全头
add_header X-Frame-Options DENY;
add_header X-Content-Type-Options nosniff;
add_header X-XSS-Protection "1; mode=block";
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

# 限制请求大小
client_max_body_size 20M;

# 限制请求频率
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
limit_req zone=api burst=20 nodelay;
```

这个安装部署指南涵盖了CMS系统的完整部署流程，包括环境准备、系统安装、配置优化、监控备份和安全加固等各个方面。按照这个指南，您可以成功部署一个生产级别的CMS系统。