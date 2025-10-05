package com.cms.permissions.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@Tag(name = "系统概览", description = "系统整体信息和概览相关接口")
public class SystemOverviewController {

    @Operation(summary = "获取系统概览信息", description = "获取CMS权限管理系统的整体概览信息")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        overview.put("systemName", "CMS 权限管理系统");
        overview.put("version", "1.0.0");
        overview.put("description", "一个基于Spring Boot的企业级内容管理系统权限管理模块");

        Map<String, Object> features = new HashMap<>();
        features.put("authentication", "JWT Token认证");
        features.put("authorization", "基于角色的访问控制(RBAC)");
        features.put("caching", "Redis缓存优化");
        features.put("monitoring", "性能监控和健康检查");
        features.put("documentation", "Swagger API文档");
        features.put("backup", "文档备份和恢复");
        features.put("migration", "文档迁移功能");

        overview.put("features", features);

        Map<String, Object> modules = new HashMap<>();
        modules.put("权限管理", "管理系统的权限定义和分配");
        modules.put("角色管理", "管理用户角色和权限关联");
        modules.put("用户管理", "管理系统用户和角色分配");
        modules.put("文档管理", "管理文档和文档分配");
        modules.put("缓存管理", "Redis缓存监控和优化");
        modules.put("审计日志", "系统操作审计和日志记录");
        modules.put("文档备份", "文档备份和恢复功能");
        modules.put("文档迁移", "文档导入和迁移功能");

        overview.put("modules", modules);

        Map<String, Object> techStack = new HashMap<>();
        techStack.put("backendFramework", "Spring Boot 3.5.6");
        techStack.put("database", "MySQL 8.0");
        techStack.put("cache", "Redis with Lettuce");
        techStack.put("security", "Spring Security 6.5.5");
        techStack.put("apiDocs", "Springdoc OpenAPI 2.6.0");
        techStack.put("storage", "MinIO");

        overview.put("technologyStack", techStack);

        return ResponseEntity.ok(overview);
    }

    @Operation(summary = "获取API端点概览", description = "获取系统中所有API端点的概览信息")
    @GetMapping("/endpoints")
    public ResponseEntity<Map<String, Object>> getApiEndpointsOverview() {
        Map<String, Object> endpoints = new HashMap<>();

        Map<String, Object> authEndpoints = new HashMap<>();
        authEndpoints.put("description", "认证和授权相关API");
        authEndpoints.put("basePath", "/api/auth");
        authEndpoints.put("endpoints", new String[]{
            "POST /login - 用户登录",
            "POST /register - 用户注册",
            "GET /logout - 用户登出",
            "GET /refresh - 刷新令牌"
        });

        endpoints.put("authentication", authEndpoints);

        Map<String, Object> permissionEndpoints = new HashMap<>();
        permissionEndpoints.put("description", "权限管理相关API");
        permissionEndpoints.put("basePath", "/api/permissions");
        permissionEndpoints.put("endpoints", new String[]{
            "POST / - 创建权限",
            "GET /{id} - 根据ID获取权限",
            "GET /code/{code} - 根据代码获取权限",
            "GET / - 获取所有权限",
            "PUT /{id} - 更新权限",
            "DELETE /{id} - 删除权限"
        });

        endpoints.put("permissions", permissionEndpoints);

        Map<String, Object> roleEndpoints = new HashMap<>();
        roleEndpoints.put("description", "角色管理相关API");
        roleEndpoints.put("basePath", "/api/roles");
        roleEndpoints.put("endpoints", new String[]{
            "POST / - 创建角色",
            "GET /{id} - 根据ID获取角色",
            "GET /name/{name} - 根据名称获取角色",
            "GET / - 获取所有角色",
            "PUT /{id}/permissions - 更新角色权限",
            "PUT /{id} - 更新角色信息",
            "DELETE /{id} - 删除角色"
        });

        endpoints.put("roles", roleEndpoints);

        Map<String, Object> userEndpoints = new HashMap<>();
        userEndpoints.put("description", "用户管理相关API");
        userEndpoints.put("basePath", "/api/users");
        userEndpoints.put("endpoints", new String[]{
            "POST / - 创建用户",
            "GET /{id} - 根据ID获取用户",
            "GET /username/{username} - 根据用户名获取用户",
            "GET / - 获取所有用户",
            "PUT /{id}/roles - 更新用户角色",
            "PUT /{id} - 更新用户信息",
            "DELETE /{id} - 删除用户"
        });

        endpoints.put("users", userEndpoints);

        Map<String, Object> documentEndpoints = new HashMap<>();
        documentEndpoints.put("description", "文档管理相关API");
        documentEndpoints.put("basePath", "/api/documents");
        documentEndpoints.put("endpoints", new String[]{
            "POST / - 创建文档",
            "GET /{id} - 获取文档",
            "PUT /{id} - 更新文档",
            "DELETE /{id} - 删除文档",
            "PUT /{id}/publish - 发布文档",
            "PUT /{id}/approve - 审批文档",
            "GET / - 获取用户文档",
            "POST /{documentId}/assign - 分配文档给用户"
        });

        endpoints.put("documents", documentEndpoints);

        Map<String, Object> cacheEndpoints = new HashMap<>();
        cacheEndpoints.put("description", "缓存管理相关API");
        cacheEndpoints.put("basePath", "/api/cache");
        cacheEndpoints.put("endpoints", new String[]{
            "GET /health - 获取缓存健康状态",
            "GET /performance - 获取缓存性能报告",
            "POST /warmup/user-permissions - 预热用户权限缓存",
            "POST /warmup/document-public-status - 预热文档公开状态缓存",
            "POST /warmup/popular-document-assignments - 预热热门文档分配缓存",
            "POST /warmup/full - 执行完整缓存预热",
            "DELETE /clear/user-permissions/{username} - 清除用户权限缓存",
            "DELETE /clear/user-document-assignments/{userId} - 清除用户文档分配缓存",
            "DELETE /clear/document-public-status/{documentId} - 清除文档公开状态缓存",
            "DELETE /clear/all-user-permissions - 清除所有用户权限缓存",
            "DELETE /clear/all-user-document-assignments - 清除所有用户文档分配缓存",
            "DELETE /clear/all-document-public-status - 清除所有文档公开状态缓存"
        });

        endpoints.put("cache", cacheEndpoints);

        Map<String, Object> auditEndpoints = new HashMap<>();
        auditEndpoints.put("description", "审计日志相关API");
        auditEndpoints.put("basePath", "/api/audit");
        auditEndpoints.put("endpoints", new String[]{
            "GET /logs - 查询审计日志",
            "GET /logs/user/{username} - 根据用户名查询审计日志",
            "GET /logs/type/{resourceType} - 根据资源类型查询审计日志",
            "GET /logs/operation/{operationType} - 根据操作类型查询审计日志",
            "GET /logs/recent/{hours} - 查询最近的审计日志",
            "GET /stats - 获取审计统计信息",
            "GET /distinct/usernames - 获取所有唯一用户名",
            "GET /distinct/operation-types - 获取所有操作类型",
            "GET /distinct/resource-types - 获取所有资源类型"
        });

        endpoints.put("audit", auditEndpoints);

        Map<String, Object> backupEndpoints = new HashMap<>();
        backupEndpoints.put("description", "文档备份相关API");
        backupEndpoints.put("basePath", "/api/documents/backup");
        backupEndpoints.put("endpoints", new String[]{
            "POST /{documentId} - 创建文档备份",
            "GET /{backupId} - 获取备份详情",
            "GET /document/{documentId} - 获取文档的所有备份",
            "DELETE /{backupId} - 删除备份",
            "POST /{backupId}/restore - 恢复备份",
            "GET /config - 获取备份配置",
            "PUT /config - 更新备份配置"
        });

        endpoints.put("backup", backupEndpoints);

        Map<String, Object> migrationEndpoints = new HashMap<>();
        migrationEndpoints.put("description", "文档迁移相关API");
        migrationEndpoints.put("basePath", "/api/migration");
        migrationEndpoints.put("endpoints", new String[]{
            "POST /migrate - 迁移单个文档",
            "POST /migrate/batch - 批量迁移文档",
            "GET /statistics - 获取迁移统计信息",
            "GET /history - 获取迁移历史",
            "POST /retry/{migrationLogId} - 重试失败的迁移"
        });

        endpoints.put("migration", migrationEndpoints);

        return ResponseEntity.ok(endpoints);
    }

    @Operation(summary = "获取系统配置信息", description = "获取系统的配置参数和设置信息")
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSystemConfiguration() {
        Map<String, Object> config = new HashMap<>();

        Map<String, Object> cacheConfig = new HashMap<>();
        cacheConfig.put("provider", "Redis");
        cacheConfig.put("ttl", "10 minutes");
        cacheConfig.put("maxSize", "10000 entries");
        cacheConfig.put("evictionPolicy", "LRU");

        config.put("cache", cacheConfig);

        Map<String, Object> securityConfig = new HashMap<>();
        securityConfig.put("authMethod", "JWT Token");
        securityConfig.put("tokenExpiration", "24 hours");
        securityConfig.put("encryption", "BCrypt");

        config.put("security", securityConfig);

        Map<String, Object> storageConfig = new HashMap<>();
        storageConfig.put("primaryStorage", "MySQL Database");
        storageConfig.put("cacheStorage", "Redis");
        storageConfig.put("fileStorage", "MinIO");
        storageConfig.put("backupStorage", "Local File System");

        config.put("storage", storageConfig);

        Map<String, Object> monitoringConfig = new HashMap<>();
        monitoringConfig.put("performanceMonitoring", "Enabled");
        monitoringConfig.put("healthChecks", "Enabled");
        monitoringConfig.put("loggingLevel", "INFO");

        config.put("monitoring", monitoringConfig);

        return ResponseEntity.ok(config);
    }
}
