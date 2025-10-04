# 权限缓存系统使用文档

## 概述

权限缓存系统是CMS系统的核心组件，用于提高权限检查和文档访问控制的性能。该系统基于Redis实现，提供了用户权限、文档分配和文档公开状态的缓存功能。

## 系统架构

### 核心组件

1. **PermissionCacheService** - 核心缓存服务
2. **CustomPermissionEvaluator** - 权限评估器（集成缓存）
3. **CachePerformanceAnalyzer** - 性能分析器
4. **CacheHealthService** - 健康检查服务
5. **CacheWarmupService** - 缓存预热服务
6. **CacheConfigManager** - 配置管理器

### 缓存类型

- **用户权限缓存** (`userPermissions`)
  - 键格式: `user:permissions:{username}`
  - 存储用户的所有权限代码
  - TTL: 10分钟

- **用户文档分配缓存** (`userDocumentPermissions`)
  - 键格式: `user:doc:permissions:{userId}:{documentId}`
  - 存储用户对特定文档的分配关系
  - TTL: 10分钟

- **文档公开状态缓存** (`documentPublic`)
  - 键格式: `document:public:{documentId}`
  - 存储文档是否为公开状态
  - TTL: 10分钟

## 使用指南

### 1. 权限检查

```java
@Autowired
private PermissionCacheService permissionCacheService;

// 检查用户是否有特定权限
boolean hasPermission = permissionCacheService.hasPermission("username", "DOCUMENT:READ");

// 获取用户所有权限
Set<String> permissions = permissionCacheService.getUserPermissions("username");
```

### 2. 文档访问控制

```java
// 检查文档是否公开
boolean isPublic = permissionCacheService.isDocumentPublic(documentId);

// 检查用户是否被分配到文档
boolean isAssigned = permissionCacheService.isUserAssignedToDocument(userId, documentId);

// 检查用户对文档的特定分配类型
boolean hasAssignment = permissionCacheService.hasDocumentAssignment(
    userId, documentId, DocumentAssignment.AssignmentType.EDITOR
);
```

### 3. 缓存失效

```java
// 清除用户权限缓存
permissionCacheService.evictUserPermissions("username");

// 清除文档相关缓存
permissionCacheService.evictDocumentCache(documentId);

// 清除所有缓存
permissionCacheService.evictAllUserPermissions();
```

### 4. 使用注解进行权限控制

```java
@PreAuthorize("@permissionEvaluator.hasPermission(authentication, 'DOCUMENT:READ')")
public Document getDocument(Long id) {
    return documentRepository.findById(id).orElse(null);
}

@PreAuthorize("@permissionEvaluator.hasPermission(authentication, #document, 'WRITE')")
public Document updateDocument(Document document) {
    return documentRepository.save(document);
}
```

## 监控和管理

### 1. 缓存统计

```bash
# 获取缓存统计信息
GET /api/cache/monitor/stats

# 响应示例
{
  "userPermissionsCacheSize": 150,
  "documentPermissionsCacheSize": 300,
  "documentPublicCacheSize": 50,
  "userPermissionHits": 1200,
  "userPermissionMisses": 80,
  "documentAssignmentHits": 800,
  "documentAssignmentMisses": 45,
  "documentPublicStatusHits": 400,
  "documentPublicStatusMisses": 20
}
```

### 2. 健康检查

```bash
# 检查缓存健康状态
GET /api/cache/monitor/health

# 响应示例
{
  "healthy": true,
  "redisConnected": true,
  "cachePerformance": {
    "healthy": true,
    "hitRate": 0.85,
    "averageResponseTime": 45.2
  },
  "cacheSize": {
    "healthy": true,
    "totalCacheSize": 500
  },
  "lastCheckTime": "2024-01-15T10:30:00"
}
```

### 3. 性能分析

```bash
# 获取性能报告
GET /api/cache/performance/report

# 获取性能趋势
GET /api/cache/performance/trends?hours=24

# 获取性能建议
GET /api/cache/performance/recommendations
```

### 4. 缓存管理

```bash
# 清除特定用户缓存
DELETE /api/cache/monitor/user/{userId}

# 清除特定文档缓存
DELETE /api/cache/monitor/document/{documentId}

# 清除所有缓存
DELETE /api/cache/monitor/all

# 缓存预热
POST /api/cache/monitor/warmup/all
```

## 配置管理

### 1. 动态配置TTL

```bash
# 设置缓存TTL
PUT /api/cache/config/ttl
{
  "cacheName": "userPermissions",
  "ttlMinutes": 15
}

# 获取推荐配置
GET /api/cache/config/recommendations

# 重置为默认配置
POST /api/cache/config/reset
```

### 2. 配置文件

```yaml
# application.yml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

# 自定义配置
cache:
  permission:
    ttl-minutes: 10
    max-size: 10000
    enable-statistics: true
```

## 最佳实践

### 1. 缓存策略

- **预热策略**: 系统启动时预热热门用户和文档的缓存
- **失效策略**: 数据变更时及时清除相关缓存
- **监控策略**: 定期检查缓存命中率和性能指标

### 2. 性能优化

- 合理设置TTL，平衡数据一致性和性能
- 监控缓存大小，避免内存溢出
- 使用批量操作减少Redis访问次数

### 3. 故障处理

- 实现缓存降级机制，Redis不可用时直接查询数据库
- 设置合理的超时时间，避免长时间等待
- 记录缓存异常日志，便于问题排查

## 故障排除

### 常见问题

1. **缓存命中率低**
   - 检查TTL设置是否合理
   - 确认缓存预热是否正常执行
   - 分析访问模式，调整缓存策略

2. **Redis连接失败**
   - 检查Redis服务状态
   - 验证连接配置
   - 查看网络连通性

3. **内存使用过高**
   - 检查缓存大小限制
   - 清理过期缓存
   - 优化缓存键的设计

### 日志分析

```bash
# 查看缓存相关日志
grep "Cache" application.log

# 查看性能日志
grep "Performance" application.log

# 查看错误日志
grep "ERROR.*Cache" application.log
```

## API参考

详细的API文档请参考 [API文档](API.md)

## 更新日志

- v1.0.0: 初始版本，基础缓存功能
- v1.1.0: 添加性能监控和分析
- v1.2.0: 添加健康检查和预热功能
- v1.3.0: 添加动态配置管理