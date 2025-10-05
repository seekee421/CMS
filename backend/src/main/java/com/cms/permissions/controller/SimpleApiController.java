package com.cms.permissions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 简单API控制器 - 提供基础的API端点
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SimpleApiController {

    /**
     * 获取版本列表
     */
    @GetMapping("/versions/products")
    public ResponseEntity<Map<String, Object>> getVersions() {
        List<Map<String, Object>> versions = Arrays.asList(
            createVersion("1", "DM8", "8.1.3.128", "当前版本", true),
            createVersion("2", "DM8", "8.1.2.117", "稳定版本", false),
            createVersion("3", "DM7", "7.6.0.197", "历史版本", false)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", versions);
        response.put("total", versions.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 搜索功能
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        
        List<Map<String, Object>> results = Arrays.asList(
            createSearchResult("1", "数据库安装指南", "详细介绍DM数据库的安装步骤", "/docs/install"),
            createSearchResult("2", "SQL语法参考", "DM数据库SQL语法完整参考", "/docs/sql"),
            createSearchResult("3", "性能优化指南", "数据库性能调优最佳实践", "/docs/performance")
        );
        
        // 简单的搜索过滤
        if (query != null && !query.trim().isEmpty()) {
            results = results.stream()
                .filter(result -> result.get("title").toString().toLowerCase().contains(query.toLowerCase()) ||
                                result.get("content").toString().toLowerCase().contains(query.toLowerCase()))
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", results);
        response.put("total", results.size());
        response.put("query", query);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam(defaultValue = "week") String timeRange) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDocuments", 156);
        statistics.put("totalViews", 12450);
        statistics.put("totalDownloads", 3280);
        statistics.put("activeUsers", 89);
        
        List<Map<String, Object>> popularDocs = Arrays.asList(
            createPopularDoc("数据库安装指南", 1250, 340),
            createPopularDoc("SQL语法参考", 980, 220),
            createPopularDoc("备份恢复指南", 756, 180),
            createPopularDoc("性能优化指南", 645, 150)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", statistics);
        response.put("popularDocuments", popularDocs);
        response.put("timeRange", timeRange);
        response.put("lastUpdated", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 记录统计数据
     */
    @PostMapping("/statistics/track")
    public ResponseEntity<Map<String, Object>> trackStatistics(@RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        String documentId = (String) request.get("documentId");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "统计记录成功");
        response.put("action", action);
        response.put("documentId", documentId);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "CMS Documentation Center");
        
        return ResponseEntity.ok(response);
    }

    // 辅助方法
    private Map<String, Object> createVersion(String id, String product, String version, String description, boolean isActive) {
        Map<String, Object> versionMap = new HashMap<>();
        versionMap.put("id", id);
        versionMap.put("product", product);
        versionMap.put("version", version);
        versionMap.put("description", description);
        versionMap.put("isActive", isActive);
        versionMap.put("releaseDate", LocalDateTime.now().minusDays(30));
        return versionMap;
    }

    private Map<String, Object> createSearchResult(String id, String title, String content, String url) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("title", title);
        result.put("content", content);
        result.put("url", url);
        result.put("type", "document");
        result.put("lastModified", LocalDateTime.now().minusDays(5));
        return result;
    }

    private Map<String, Object> createPopularDoc(String title, int views, int downloads) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", title);
        doc.put("views", views);
        doc.put("downloads", downloads);
        doc.put("trend", "up");
        return doc;
    }
}