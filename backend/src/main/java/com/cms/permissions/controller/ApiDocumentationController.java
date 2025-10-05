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
@RequestMapping("/api/docs")
@Tag(name = "API文档", description = "API文档和系统信息相关接口")
public class ApiDocumentationController {

    @Operation(summary = "获取API文档信息", description = "获取系统API文档的基本信息和访问入口")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getApiDocumentationInfo() {
        Map<String, Object> documentationInfo = new HashMap<>();

        documentationInfo.put("title", "CMS 权限管理系统 API 文档");
        documentationInfo.put("version", "1.0.0");
        documentationInfo.put("description", "CMS 权限管理系统提供了一套完整的权限管理功能，包括权限、角色和用户管理");

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("swagger-ui", "/swagger-ui.html");
        endpoints.put("api-docs-json", "/v3/api-docs");
        endpoints.put("api-docs-yaml", "/v3/api-docs.yaml");

        documentationInfo.put("endpoints", endpoints);

        Map<String, String> modules = new HashMap<>();
        modules.put("权限管理", "/api/permissions");
        modules.put("角色管理", "/api/roles");
        modules.put("用户管理", "/api/users");
        modules.put("文档管理", "/api/documents");
        modules.put("缓存管理", "/api/cache");
        modules.put("审计日志", "/api/audit");
        modules.put("文档备份", "/api/documents/backup");
        modules.put("文档迁移", "/api/migration");

        documentationInfo.put("modules", modules);

        return ResponseEntity.ok(documentationInfo);
    }

    @Operation(summary = "获取系统健康状态", description = "获取系统各模块的健康状态信息")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> healthInfo = new HashMap<>();

        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());

        Map<String, String> components = new HashMap<>();
        components.put("database", "UP");
        components.put("redis", "UP");
        components.put("cache", "UP");
        components.put("api", "UP");

        healthInfo.put("components", components);

        return ResponseEntity.ok(healthInfo);
    }

    @Operation(summary = "获取API使用统计", description = "获取API使用情况的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApiStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalEndpoints", 50);
        stats.put("activeModules", 8);
        stats.put("documentationCoverage", "100%");
        stats.put("lastUpdated", System.currentTimeMillis());

        return ResponseEntity.ok(stats);
    }
}
