# CMS 缓存系统部署指南

## 概述

本指南详细介绍了如何在不同环境中部署和配置 CMS 缓存系统，包括开发环境、测试环境和生产环境的部署步骤。

## 系统要求

### 硬件要求

#### 最低配置
- CPU: 2核心
- 内存: 4GB RAM
- 存储: 20GB 可用空间
- 网络: 100Mbps

#### 推荐配置
- CPU: 4核心或更多
- 内存: 8GB RAM 或更多
- 存储: 50GB SSD
- 网络: 1Gbps

### 软件要求

- Java 17 或更高版本
- Redis 6.0 或更高版本
- Spring Boot 3.0+
- Maven 3.6+

## 环境准备

### 1. Redis 安装与配置

#### Docker 方式 (推荐)

```bash
# 拉取 Redis 镜像
docker pull redis:7-alpine

# 创建 Redis 配置文件
mkdir -p /opt/redis/conf
cat > /opt/redis/conf/redis.conf << EOF
# 基础配置
bind 0.0.0.0
port 6379
protected-mode yes
requirepass your_redis_password

# 内存配置
maxmemory 2gb
maxmemory-policy allkeys-lru

# 持久化配置
save 900 1
save 300 10
save 60 10000

# 日志配置
loglevel notice
logfile /var/log/redis/redis-server.log

# 网络配置
tcp-keepalive 300
timeout 0

# 客户端配置
maxclients 10000
EOF

# 启动 Redis 容器
docker run -d \
  --name cms-redis \
  -p 6379:6379 \
  -v /opt/redis/conf/redis.conf:/usr/local/etc/redis/redis.conf \
  -v /opt/redis/data:/data \
  redis:7-alpine redis-server /usr/local/etc/redis/redis.conf
```

#### 原生安装方式

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install redis-server

# CentOS/RHEL
sudo yum install epel-release
sudo yum install redis

# macOS
brew install redis

# 启动 Redis 服务
sudo systemctl start redis
sudo systemctl enable redis
```

### 2. Redis 集群配置 (生产环境)

```bash
# 创建集群配置目录
mkdir -p /opt/redis-cluster/{7001,7002,7003,7004,7005,7006}

# 为每个节点创建配置文件
for port in {7001..7006}; do
cat > /opt/redis-cluster/$port/redis.conf << EOF
port $port
cluster-enabled yes
cluster-config-file nodes-$port.conf
cluster-node-timeout 15000
appendonly yes
bind 0.0.0.0
protected-mode no
EOF
done

# 启动所有节点
for port in {7001..7006}; do
  redis-server /opt/redis-cluster/$port/redis.conf &
done

# 创建集群
redis-cli --cluster create \
  127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 \
  127.0.0.1:7004 127.0.0.1:7005 127.0.0.1:7006 \
  --cluster-replicas 1
```

## 应用配置

### 1. 开发环境配置

创建 `application-dev.yml`:

```yaml
spring:
  profiles:
    active: dev
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      jedis:
        pool:
          max-active: 8
          max-idle: 4
          min-idle: 2
          max-wait: 2000ms

cache:
  type: redis
  ttl: 1800  # 30分钟
  allow-null-values: false
  key-prefix: "cms:dev:cache:"
  
  permissions:
    ttl: 900  # 15分钟 (开发环境较短)
    max-size: 1000
    statistics: true
    
  optimization:
    memory:
      enabled: false  # 开发环境关闭优化
      
  warmup:
    strategy:
      enabled: false  # 开发环境关闭预热

logging:
  level:
    com.cms.cache: DEBUG
    org.springframework.cache: DEBUG
```

### 2. 测试环境配置

创建 `application-test.yml`:

```yaml
spring:
  profiles:
    active: test
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      database: 15  # 使用独立数据库
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      jedis:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2
          max-wait: 2000ms

cache:
  type: redis
  ttl: 300  # 5分钟 (测试环境更短)
  allow-null-values: false
  key-prefix: "cms:test:cache:"
  
  permissions:
    ttl: 300
    max-size: 1000
    statistics: true
    
  optimization:
    memory:
      enabled: true
      max-memory-usage: 0.7
      cleanup-threshold: 0.8
      
  warmup:
    strategy:
      enabled: true
      batch-size: 100

logging:
  level:
    com.cms.cache: INFO
```

### 3. 生产环境配置

创建 `application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
  data:
    redis:
      cluster:
        nodes:
          - ${REDIS_NODE1:redis-node1:7001}
          - ${REDIS_NODE2:redis-node2:7002}
          - ${REDIS_NODE3:redis-node3:7003}
          - ${REDIS_NODE4:redis-node4:7004}
          - ${REDIS_NODE5:redis-node5:7005}
          - ${REDIS_NODE6:redis-node6:7006}
        max-redirects: 3
      password: ${REDIS_PASSWORD}
      timeout: 5000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 3000ms

cache:
  type: redis
  ttl: 3600  # 1小时
  allow-null-values: false
  key-prefix: "cms:prod:cache:"
  
  permissions:
    ttl: 1800  # 30分钟
    max-size: 10000
    statistics: true
    performance:
      enabled: true
      history-size: 1000
      alert-thresholds:
        hit-rate: 0.8
        response-time: 100
        eviction-rate: 0.1
    health:
      enabled: true
      redis-timeout: 5000
      thresholds:
        hit-rate: 0.7
        response-time: 200
        memory-usage: 0.9
    warmup:
      enabled: true
      batch-size: 1000
      timeout: 30000
      
  document-public-status:
    ttl: 600
    max-size: 5000
    statistics: true
    
  document-assignments:
    ttl: 900
    max-size: 20000
    statistics: true
    
  optimization:
    memory:
      enabled: true
      max-memory-usage: 0.8
      cleanup-threshold: 0.9
      batch-size: 1000
      check-interval: 300000
      
  warmup:
    strategy:
      enabled: true
      peak-hours: [9, 10, 11, 14, 15, 16]
      min-hit-rate: 0.7
      max-memory-usage: 0.8
      batch-size: 500

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,cache,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.cms.cache: INFO
    org.springframework.cache: WARN
  file:
    name: /var/log/cms/cache.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 部署步骤

### 1. 构建应用

```bash
# 克隆代码
git clone <repository-url>
cd cms-backend

# 构建应用
mvn clean package -DskipTests

# 或者包含测试
mvn clean package
```

### 2. Docker 部署

#### 创建 Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

# 设置工作目录
WORKDIR /app

# 复制应用文件
COPY target/cms-backend-*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/cms

# 设置环境变量
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    container_name: cms-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
      - ./redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - cms-network
    restart: unless-stopped

  cms-backend:
    build: .
    container_name: cms-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
    volumes:
      - ./logs:/var/log/cms
    depends_on:
      - redis
    networks:
      - cms-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  redis_data:

networks:
  cms-network:
    driver: bridge
```

#### 部署命令

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f cms-backend

# 停止服务
docker-compose down

# 更新服务
docker-compose pull
docker-compose up -d --force-recreate
```

### 3. Kubernetes 部署

#### Redis 部署

```yaml
# redis-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
        volumeMounts:
        - name: redis-config
          mountPath: /usr/local/etc/redis/redis.conf
          subPath: redis.conf
        - name: redis-data
          mountPath: /data
        command: ["redis-server", "/usr/local/etc/redis/redis.conf"]
      volumes:
      - name: redis-config
        configMap:
          name: redis-config
      - name: redis-data
        persistentVolumeClaim:
          claimName: redis-pvc

---
apiVersion: v1
kind: Service
metadata:
  name: redis-service
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
```

#### 应用部署

```yaml
# cms-backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: cms-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: cms-backend
  template:
    metadata:
      labels:
        app: cms-backend
    spec:
      containers:
      - name: cms-backend
        image: cms-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: REDIS_HOST
          value: "redis-service"
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-secret
              key: password
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: cms-backend-service
spec:
  selector:
    app: cms-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 监控与告警

### 1. Prometheus 配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'cms-backend'
    static_configs:
      - targets: ['cms-backend:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 30s
```

### 2. Grafana 仪表板

导入预配置的仪表板模板：

```json
{
  "dashboard": {
    "title": "CMS Cache System",
    "panels": [
      {
        "title": "Cache Hit Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "cache_gets_total{result=\"hit\"} / (cache_gets_total{result=\"hit\"} + cache_gets_total{result=\"miss\"})"
          }
        ]
      },
      {
        "title": "Cache Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "cache_operation_time_seconds"
          }
        ]
      }
    ]
  }
}
```

### 3. 告警规则

```yaml
# alert-rules.yml
groups:
  - name: cache-alerts
    rules:
      - alert: CacheHitRateLow
        expr: cache_hit_rate < 0.7
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Cache hit rate is low"
          description: "Cache hit rate is {{ $value }} which is below 70%"

      - alert: CacheResponseTimeSlow
        expr: cache_response_time > 200
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Cache response time is slow"
          description: "Cache response time is {{ $value }}ms"

      - alert: RedisDown
        expr: up{job="redis"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis is down"
          description: "Redis instance is not responding"
```

## 安全配置

### 1. Redis 安全

```bash
# 设置强密码
CONFIG SET requirepass "your_strong_password_here"

# 禁用危险命令
CONFIG SET rename-command FLUSHDB ""
CONFIG SET rename-command FLUSHALL ""
CONFIG SET rename-command DEBUG ""

# 配置防火墙
sudo ufw allow from 10.0.0.0/8 to any port 6379
sudo ufw deny 6379
```

### 2. 应用安全

```yaml
# application-prod.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
```

## 性能调优

### 1. JVM 参数

```bash
# 生产环境 JVM 参数
JAVA_OPTS="-Xmx4g -Xms4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+UseStringDeduplication \
  -XX:+PrintGCDetails \
  -XX:+PrintGCTimeStamps \
  -Xloggc:/var/log/cms/gc.log"
```

### 2. Redis 调优

```conf
# redis.conf 优化配置
maxmemory 4gb
maxmemory-policy allkeys-lru
tcp-keepalive 300
timeout 0
tcp-backlog 511
databases 16
save 900 1
save 300 10
save 60 10000
```

## 备份与恢复

### 1. Redis 备份

```bash
# 自动备份脚本
#!/bin/bash
BACKUP_DIR="/backup/redis"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
redis-cli --rdb $BACKUP_DIR/dump_$DATE.rdb

# 清理旧备份 (保留7天)
find $BACKUP_DIR -name "dump_*.rdb" -mtime +7 -delete
```

### 2. 恢复流程

```bash
# 停止 Redis 服务
sudo systemctl stop redis

# 恢复数据文件
cp /backup/redis/dump_20240101_120000.rdb /var/lib/redis/dump.rdb
chown redis:redis /var/lib/redis/dump.rdb

# 启动 Redis 服务
sudo systemctl start redis
```

## 故障排查

### 常见问题及解决方案

#### 1. 连接超时
```bash
# 检查网络连通性
telnet redis-host 6379

# 检查防火墙
sudo ufw status

# 检查 Redis 日志
tail -f /var/log/redis/redis-server.log
```

#### 2. 内存不足
```bash
# 检查内存使用
redis-cli info memory

# 清理过期键
redis-cli --scan --pattern "*" | xargs redis-cli del

# 调整内存策略
redis-cli config set maxmemory-policy allkeys-lru
```

#### 3. 性能问题
```bash
# 监控慢查询
redis-cli slowlog get 10

# 检查连接数
redis-cli info clients

# 分析键分布
redis-cli --bigkeys
```

## 维护计划

### 日常维护
- 监控缓存命中率和响应时间
- 检查 Redis 内存使用情况
- 查看应用日志和错误信息

### 周期维护
- 每周备份 Redis 数据
- 每月检查和清理过期数据
- 每季度评估性能指标和优化配置

### 升级计划
- 定期更新 Redis 版本
- 升级应用依赖库
- 测试新功能和性能改进

---

*最后更新: 2024年*