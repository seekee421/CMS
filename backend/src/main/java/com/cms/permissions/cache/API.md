# 缓存系统 API 文档

## 概述

本文档描述了CMS权限缓存系统的所有REST API接口。所有API都需要适当的权限才能访问。

## 基础信息

- **Base URL**: `/api/cache`
- **认证方式**: JWT Token
- **内容类型**: `application/json`

## 权限要求

- `CACHE:READ` - 查看缓存信息
- `CACHE:MANAGE` - 管理缓存（清除、预热等）
- `CACHE:CONFIG` - 配置缓存参数

## 缓存监控 API

### 1. 获取缓存统计信息

```http
GET /api/cache/monitor/stats
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "userPermissionsCacheSize": 150,
  "documentPermissionsCacheSize": 300,
  "documentPublicCacheSize": 50,
  "userPermissionHits": 1200,
  "userPermissionMisses": 80,
  "documentAssignmentHits": 800,
  "documentAssignmentMisses": 45,
  "documentPublicStatusHits": 400,
  "documentPublicStatusMisses": 20,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 2. 获取缓存健康状态

```http
GET /api/cache/monitor/health
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "healthy": true,
  "redisConnected": true,
  "cachePerformance": {
    "healthy": true,
    "hitRate": 0.85,
    "averageResponseTime": 45.2,
    "status": "GOOD"
  },
  "cacheSize": {
    "healthy": true,
    "totalCacheSize": 500,
    "maxCacheSize": 10000,
    "status": "NORMAL"
  },
  "cacheConfiguration": {
    "healthy": true,
    "status": "VALID"
  },
  "lastCheckTime": "2024-01-15T10:30:00Z"
}
```

**HTTP状态码**:
- `200 OK` - 缓存健康
- `503 Service Unavailable` - 缓存不健康

### 3. 获取缓存信息

```http
GET /api/cache/monitor/info
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "caches": [
    {
      "name": "userPermissions",
      "size": 150,
      "ttl": 600,
      "hitRate": 0.92
    },
    {
      "name": "userDocumentPermissions", 
      "size": 300,
      "ttl": 600,
      "hitRate": 0.88
    },
    {
      "name": "documentPublic",
      "size": 50,
      "ttl": 600,
      "hitRate": 0.95
    }
  ],
  "totalSize": 500,
  "redisInfo": {
    "connected": true,
    "usedMemory": "2.5MB",
    "maxMemory": "100MB"
  }
}
```

## 缓存管理 API

### 4. 清除用户缓存

```http
DELETE /api/cache/monitor/user/{userId}
```

**权限要求**: `CACHE:MANAGE`

**路径参数**:
- `userId` (Long) - 用户ID

**响应示例**:
```json
{
  "success": true,
  "message": "用户缓存已清除",
  "clearedCaches": ["userPermissions", "userDocumentPermissions"],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 5. 清除文档缓存

```http
DELETE /api/cache/monitor/document/{documentId}
```

**权限要求**: `CACHE:MANAGE`

**路径参数**:
- `documentId` (Long) - 文档ID

**响应示例**:
```json
{
  "success": true,
  "message": "文档缓存已清除",
  "clearedCaches": ["documentPublic", "userDocumentPermissions"],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 6. 清除所有用户缓存

```http
DELETE /api/cache/monitor/users
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "message": "所有用户缓存已清除",
  "clearedCount": 150,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 7. 清除所有缓存

```http
DELETE /api/cache/monitor/all
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "message": "所有缓存已清除",
  "clearedCaches": ["userPermissions", "userDocumentPermissions", "documentPublic"],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 缓存预热 API

### 8. 预热用户权限缓存

```http
POST /api/cache/monitor/warmup/users
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "warmedUpCount": 100,
  "duration": 2500,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 9. 预热文档公开状态缓存

```http
POST /api/cache/monitor/warmup/documents
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "warmedUpCount": 50,
  "duration": 1200,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 10. 预热热门文档分配缓存

```http
POST /api/cache/monitor/warmup/assignments
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "warmedUpCount": 200,
  "duration": 3000,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 11. 完整缓存预热

```http
POST /api/cache/monitor/warmup/all
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "userPermissionsResult": {
    "success": true,
    "warmedUpCount": 100,
    "duration": 2500
  },
  "documentPublicResult": {
    "success": true,
    "warmedUpCount": 50,
    "duration": 1200
  },
  "documentAssignmentsResult": {
    "success": true,
    "warmedUpCount": 200,
    "duration": 3000
  },
  "totalDuration": 6700,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 性能监控 API

### 12. 获取性能报告

```http
GET /api/cache/performance/report
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "userPermissions": {
    "hitCount": 1200,
    "missCount": 80,
    "evictionCount": 5,
    "hitRate": 0.9375,
    "missRate": 0.0625,
    "averageResponseTime": 45.2,
    "recommendations": ["考虑增加TTL到15分钟"]
  },
  "userDocumentPermissions": {
    "hitCount": 800,
    "missCount": 45,
    "evictionCount": 2,
    "hitRate": 0.9467,
    "missRate": 0.0533,
    "averageResponseTime": 38.7,
    "recommendations": []
  },
  "documentPublic": {
    "hitCount": 400,
    "missCount": 20,
    "evictionCount": 1,
    "hitRate": 0.9524,
    "missRate": 0.0476,
    "averageResponseTime": 42.1,
    "recommendations": []
  },
  "overallRecommendations": [
    "整体缓存性能良好",
    "建议监控内存使用情况"
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 13. 获取性能摘要

```http
GET /api/cache/performance/summary
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "overallHitRate": 0.9421,
  "overallMissRate": 0.0579,
  "averageResponseTime": 42.0,
  "totalOperations": 2545,
  "totalHits": 2400,
  "totalMisses": 145,
  "totalEvictions": 8,
  "performanceGrade": "A",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 14. 获取性能趋势

```http
GET /api/cache/performance/trends?hours={hours}
```

**权限要求**: `CACHE:READ`

**查询参数**:
- `hours` (int, 可选) - 时间范围（小时），默认24小时

**响应示例**:
```json
{
  "timeRange": {
    "startTime": "2024-01-14T10:30:00Z",
    "endTime": "2024-01-15T10:30:00Z",
    "hours": 24
  },
  "trends": [
    {
      "timestamp": "2024-01-14T11:00:00Z",
      "hitRate": 0.92,
      "averageResponseTime": 45.0,
      "operationCount": 150
    },
    {
      "timestamp": "2024-01-14T12:00:00Z",
      "hitRate": 0.94,
      "averageResponseTime": 42.0,
      "operationCount": 180
    }
  ],
  "summary": {
    "averageHitRate": 0.9421,
    "peakHitRate": 0.96,
    "lowestHitRate": 0.88,
    "averageResponseTime": 42.0
  }
}
```

### 15. 获取性能建议

```http
GET /api/cache/performance/recommendations
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "recommendations": [
    {
      "type": "TTL_OPTIMIZATION",
      "priority": "MEDIUM",
      "cacheName": "userPermissions",
      "message": "考虑将TTL从10分钟增加到15分钟以提高命中率",
      "impact": "预计可提高5%的命中率"
    },
    {
      "type": "MEMORY_OPTIMIZATION",
      "priority": "LOW",
      "cacheName": "all",
      "message": "当前内存使用正常，建议继续监控",
      "impact": "维持当前性能水平"
    }
  ],
  "overallScore": 85,
  "performanceGrade": "A",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 16. 获取性能告警

```http
GET /api/cache/performance/alerts
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "alerts": [
    {
      "level": "WARNING",
      "type": "LOW_HIT_RATE",
      "cacheName": "userPermissions",
      "message": "用户权限缓存命中率低于90%",
      "currentValue": 0.85,
      "threshold": 0.90,
      "timestamp": "2024-01-15T10:25:00Z"
    }
  ],
  "hasActiveAlerts": true,
  "alertCount": 1,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 17. 重置性能统计

```http
POST /api/cache/performance/reset
```

**权限要求**: `CACHE:MANAGE`

**响应示例**:
```json
{
  "success": true,
  "message": "性能统计已重置",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 配置管理 API

### 18. 获取缓存TTL配置

```http
GET /api/cache/config/ttl/{cacheName}
```

**权限要求**: `CACHE:READ`

**路径参数**:
- `cacheName` (String) - 缓存名称

**响应示例**:
```json
{
  "cacheName": "userPermissions",
  "ttlMinutes": 10,
  "lastModified": "2024-01-15T09:00:00Z",
  "modifiedBy": "admin"
}
```

### 19. 设置缓存TTL

```http
PUT /api/cache/config/ttl
```

**权限要求**: `CACHE:CONFIG`

**请求体**:
```json
{
  "cacheName": "userPermissions",
  "ttlMinutes": 15
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "TTL配置已更新",
  "oldTtl": 10,
  "newTtl": 15,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 20. 获取所有TTL配置

```http
GET /api/cache/config/ttl
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "configurations": [
    {
      "cacheName": "userPermissions",
      "ttlMinutes": 10,
      "lastModified": "2024-01-15T09:00:00Z"
    },
    {
      "cacheName": "userDocumentPermissions",
      "ttlMinutes": 10,
      "lastModified": "2024-01-15T09:00:00Z"
    },
    {
      "cacheName": "documentPublic",
      "ttlMinutes": 10,
      "lastModified": "2024-01-15T09:00:00Z"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 21. 获取推荐配置

```http
GET /api/cache/config/recommendations
```

**权限要求**: `CACHE:READ`

**响应示例**:
```json
{
  "recommendations": [
    {
      "cacheName": "userPermissions",
      "currentTtl": 10,
      "recommendedTtl": 15,
      "reason": "基于访问模式分析，建议增加TTL"
    },
    {
      "cacheName": "documentPublic",
      "currentTtl": 10,
      "recommendedTtl": 30,
      "reason": "文档公开状态变化较少，可以增加TTL"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 22. 重置为默认配置

```http
POST /api/cache/config/reset
```

**权限要求**: `CACHE:CONFIG`

**响应示例**:
```json
{
  "success": true,
  "message": "配置已重置为默认值",
  "resetConfigurations": [
    {
      "cacheName": "userPermissions",
      "oldTtl": 15,
      "newTtl": 10
    },
    {
      "cacheName": "documentPublic",
      "oldTtl": 30,
      "newTtl": 10
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 23. 验证缓存名称

```http
GET /api/cache/config/validate/{cacheName}
```

**权限要求**: `CACHE:READ`

**路径参数**:
- `cacheName` (String) - 要验证的缓存名称

**响应示例**:
```json
{
  "valid": true,
  "cacheName": "userPermissions",
  "exists": true,
  "message": "缓存名称有效"
}
```

## 错误响应

所有API在出错时都会返回统一的错误格式：

```json
{
  "error": {
    "code": "CACHE_ERROR_001",
    "message": "缓存操作失败",
    "details": "Redis连接超时",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### 常见错误码

- `CACHE_ERROR_001` - 缓存操作失败
- `CACHE_ERROR_002` - Redis连接失败
- `CACHE_ERROR_003` - 无效的缓存名称
- `CACHE_ERROR_004` - 权限不足
- `CACHE_ERROR_005` - 参数验证失败

## 限流和配额

- 每个用户每分钟最多100次API调用
- 批量操作（如清除所有缓存）每小时最多10次
- 性能统计API每分钟最多20次调用

## SDK示例

### Java客户端示例

```java
// 获取缓存统计
CacheStats stats = cacheClient.getStats();

// 清除用户缓存
cacheClient.evictUserCache(userId);

// 预热缓存
WarmupResult result = cacheClient.warmupAll();

// 获取性能报告
PerformanceReport report = cacheClient.getPerformanceReport();
```

### JavaScript客户端示例

```javascript
// 获取缓存健康状态
const health = await cacheApi.getHealth();

// 设置TTL配置
await cacheApi.setTtl('userPermissions', 15);

// 获取性能趋势
const trends = await cacheApi.getPerformanceTrends(24);
```

## 版本信息

- **当前版本**: v1.3.0
- **API版本**: v1
- **最后更新**: 2024-01-15

## 联系支持

如有问题或建议，请联系开发团队或查看项目文档。