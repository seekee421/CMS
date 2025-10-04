# CMS 缓存系统使用指南

## 概述

CMS 缓存系统是一个基于 Redis 的高性能缓存解决方案，提供了权限缓存、文档状态缓存、文档分配缓存等功能，并包含完整的性能监控、健康检查、内存优化和智能预热机制。

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   应用层        │    │   缓存层        │    │   存储层        │
│                 │    │                 │    │                 │
│ PermissionService│───▶│ CacheManager    │───▶│ Redis Cluster   │
│ DocumentService │    │ RedisTemplate   │    │                 │
│ UserService     │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │   监控层        │
                    │                 │
                    │ Performance     │
                    │ Health Check    │
                    │ Memory Optimizer│
                    │ Warmup Strategy │
                    └─────────────────┘
```

## 核心组件

### 1. 缓存服务层

#### PermissionCacheService
- **功能**: 用户权限缓存管理
- **缓存键**: `cms:permissions:user:{userId}`
- **TTL**: 30分钟
- **用法**:
```java
@Autowired
private PermissionCacheService permissionCacheService;

// 获取用户权限
Set<String> permissions = permissionCacheService.getUserPermissions(userId);

// 检查权限
boolean hasPermission = permissionCacheService.hasPermission(userId, "READ_DOCUMENT");

// 清除用户权限缓存
permissionCacheService.evictUserPermissions(userId);
```

#### DocumentPublicStatusCacheService
- **功能**: 文档公开状态缓存
- **缓存键**: `cms:public:document:{documentId}`
- **TTL**: 10分钟
- **用法**:
```java
@Autowired
private DocumentPublicStatusCacheService statusCacheService;

// 检查文档是否公开
boolean isPublic = statusCacheService.isDocumentPublic(documentId);

// 更新文档公开状态
statusCacheService.updateDocumentPublicStatus(documentId, true);
```

#### DocumentAssignmentCacheService
- **功能**: 文档分配关系缓存
- **缓存键**: `cms:assignments:user:{userId}`
- **TTL**: 15分钟
- **用法**:
```java
@Autowired
private DocumentAssignmentCacheService assignmentCacheService;

// 获取用户分配的文档
List<Long> documents = assignmentCacheService.getUserAssignedDocuments(userId);

// 检查用户是否被分配了特定文档
boolean isAssigned = assignmentCacheService.isUserAssignedToDocument(userId, documentId);
```

### 2. 性能监控

#### CachePerformanceAnalyzer
- **功能**: 缓存性能分析和监控
- **指标**: 命中率、响应时间、驱逐率
- **用法**:
```java
@Autowired
private CachePerformanceAnalyzer performanceAnalyzer;

// 获取性能报告
Map<String, Object> report = performanceAnalyzer.getPerformanceReport();

// 获取整体性能摘要
Map<String, Object> summary = performanceAnalyzer.getOverallPerformanceSummary();

// 重置统计数据
performanceAnalyzer.resetStatistics();
```

### 3. 健康检查

#### CacheHealthService
- **功能**: 缓存系统健康状态检查
- **检查项**: Redis连接、性能指标、缓存大小、配置有效性
- **用法**:
```java
@Autowired
private CacheHealthService healthService;

// 检查缓存健康状态
CacheHealthService.HealthStatus status = healthService.checkCacheHealth();

if (!status.isHealthy()) {
    logger.warn("缓存健康检查失败: {}", status.getIssues());
}
```

### 4. 内存优化

#### CacheMemoryOptimizer
- **功能**: 缓存内存使用优化
- **特性**: 内存监控、自动清理、压缩、优化建议
- **用法**:
```java
@Autowired
private CacheMemoryOptimizer memoryOptimizer;

// 获取内存使用情况
CacheMemoryOptimizer.MemoryUsageInfo memoryInfo = memoryOptimizer.getMemoryUsage();

// 执行内存清理
CacheMemoryOptimizer.CleanupResult result = memoryOptimizer.performCleanup();

// 获取优化建议
List<CacheMemoryOptimizer.OptimizationRecommendation> recommendations = 
    memoryOptimizer.getOptimizationRecommendations();
```

### 5. 智能预热

#### CacheWarmupStrategy
- **功能**: 基于访问模式的智能缓存预热
- **策略**: 高峰时段准备、低命中率恢复、内存压力缓解、定期维护、自适应学习
- **用法**:
```java
@Autowired
private CacheWarmupStrategy warmupStrategy;

// 执行智能预热
CacheWarmupStrategy.WarmupResult result = warmupStrategy.executeSmartWarmup();

// 记录缓存访问
warmupStrategy.recordCacheAccess("permissions", "user123");

// 更新命中率
warmupStrategy.updateHitRate("permissions", 0.85);
```

## 配置说明

### 基础配置 (application-cache.yml)

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 2000ms

cache:
  type: redis
  ttl: 3600  # 默认TTL为1小时
  allow-null-values: false
  key-prefix: "cms:cache:"
  
  # 权限缓存配置
  permissions:
    ttl: 1800  # 30分钟
    max-size: 10000
    statistics: true
    
  # 文档公开状态缓存
  document-public-status:
    ttl: 600  # 10分钟
    max-size: 5000
    statistics: true
    
  # 文档分配缓存
  document-assignments:
    ttl: 900  # 15分钟
    max-size: 20000
    statistics: true
```

### 性能监控配置

```yaml
cache:
  permissions:
    performance:
      enabled: true
      history-size: 1000
      alert-thresholds:
        hit-rate: 0.8
        response-time: 100
        eviction-rate: 0.1
```

### 健康检查配置

```yaml
cache:
  permissions:
    health:
      enabled: true
      redis-timeout: 5000
      performance-check: true
      size-check: true
      config-check: true
      thresholds:
        hit-rate: 0.7
        response-time: 200
        memory-usage: 0.9
```

### 内存优化配置

```yaml
cache:
  optimization:
    memory:
      enabled: true
      max-memory-usage: 0.8
      cleanup-threshold: 0.9
      batch-size: 1000
      ttl-extension-factor: 1.5
      check-interval: 300000  # 5分钟
```

### 预热策略配置

```yaml
cache:
  warmup:
    strategy:
      enabled: true
      peak-hours: [9, 10, 11, 14, 15, 16]
      min-hit-rate: 0.7
      max-memory-usage: 0.8
      batch-size: 500
      access-pattern-window: 3600000  # 1小时
```

## API 接口

### 缓存监控接口

#### 获取缓存统计信息
```http
GET /api/cache/stats
Authorization: Bearer {token}
```

#### 获取缓存健康状态
```http
GET /api/cache/health
Authorization: Bearer {token}
```

#### 清除缓存
```http
DELETE /api/cache/clear/{cacheType}
Authorization: Bearer {token}
```

#### 缓存预热
```http
POST /api/cache/warmup/{type}
Authorization: Bearer {token}
```

### 性能监控接口

#### 获取性能报告
```http
GET /api/cache/performance/report
Authorization: Bearer {token}
```

#### 获取性能摘要
```http
GET /api/cache/performance/summary
Authorization: Bearer {token}
```

#### 重置性能统计
```http
POST /api/cache/performance/reset
Authorization: Bearer {token}
```

### 内存优化接口

#### 获取内存使用情况
```http
GET /api/cache/memory/usage
Authorization: Bearer {token}
```

#### 执行内存清理
```http
POST /api/cache/memory/cleanup
Authorization: Bearer {token}
```

#### 获取优化建议
```http
GET /api/cache/memory/recommendations
Authorization: Bearer {token}
```

### 智能预热接口

#### 执行智能预热
```http
POST /api/cache/warmup/smart
Authorization: Bearer {token}
```

#### 获取访问模式
```http
GET /api/cache/warmup/patterns
Authorization: Bearer {token}
```

#### 获取预热历史
```http
GET /api/cache/warmup/history
Authorization: Bearer {token}
```

## 最佳实践

### 1. 缓存键设计
- 使用有意义的前缀: `cms:cache:type:identifier`
- 避免键冲突: 包含足够的上下文信息
- 保持键长度合理: 避免过长的键名

### 2. TTL 设置
- 根据数据更新频率设置合适的TTL
- 权限数据: 30分钟 (相对稳定)
- 文档状态: 10分钟 (可能频繁变更)
- 文档分配: 15分钟 (中等频率变更)

### 3. 性能优化
- 监控缓存命中率，目标 > 80%
- 控制缓存大小，避免内存溢出
- 定期清理过期和低价值数据
- 使用批量操作减少网络开销

### 4. 错误处理
- 缓存失败时降级到数据库查询
- 记录缓存异常但不影响业务流程
- 实现熔断机制防止缓存雪崩

### 5. 监控告警
- 设置命中率告警阈值 (< 70%)
- 监控响应时间 (> 200ms)
- 关注内存使用率 (> 90%)
- 跟踪驱逐率 (> 10%)

## 故障排查

### 常见问题

#### 1. 缓存命中率低
- **原因**: TTL设置过短、数据变更频繁、预热不充分
- **解决**: 调整TTL、优化预热策略、分析访问模式

#### 2. 内存使用率高
- **原因**: 缓存数据过多、TTL设置过长、内存泄漏
- **解决**: 执行内存清理、调整缓存大小限制、优化数据结构

#### 3. 响应时间慢
- **原因**: Redis连接池不足、网络延迟、序列化开销
- **解决**: 调整连接池配置、优化网络、使用高效序列化

#### 4. Redis连接失败
- **原因**: Redis服务不可用、网络问题、连接池耗尽
- **解决**: 检查Redis状态、验证网络连通性、调整连接池参数

### 日志分析

#### 启用调试日志
```yaml
logging:
  level:
    com.cms.cache: DEBUG
    org.springframework.cache: INFO
    org.springframework.data.redis: INFO
```

#### 关键日志位置
- 缓存操作: `com.cms.cache.service`
- 性能监控: `com.cms.cache.service.CachePerformanceAnalyzer`
- 健康检查: `com.cms.cache.service.CacheHealthService`
- 内存优化: `com.cms.cache.service.CacheMemoryOptimizer`

## 版本更新

### v1.0.0
- 基础缓存功能
- 权限、文档状态、文档分配缓存
- 基本性能监控

### v1.1.0
- 添加健康检查功能
- 缓存预热机制
- 配置管理功能

### v1.2.0
- 内存优化功能
- 智能预热策略
- 性能拦截器
- 自动化调度任务

## 支持与反馈

如有问题或建议，请联系开发团队或提交Issue。

---

*最后更新: 2024年*