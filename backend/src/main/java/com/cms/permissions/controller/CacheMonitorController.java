package com.cms.permissions.controller;

import com.cms.permissions.cache.CacheMemoryOptimizer;
import com.cms.permissions.cache.CacheWarmupStrategy;
import com.cms.permissions.service.CacheHealthService;
import com.cms.permissions.service.CacheWarmupService;
import com.cms.permissions.service.PermissionCacheService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
public class CacheMonitorController {

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private CacheHealthService cacheHealthService;

    @Autowired
    private CacheWarmupService cacheWarmupService;

    @Autowired
    private CacheMemoryOptimizer memoryOptimizer;

    @Autowired
    private CacheWarmupStrategy warmupStrategy;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<PermissionCacheService.CacheStats> getCacheStats() {
        PermissionCacheService.CacheStats stats =
            permissionCacheService.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/clear/user/{userId}")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> clearUserCache(
        @PathVariable Long userId
    ) {
        permissionCacheService.evictUserPermissions(userId);
        permissionCacheService.evictUserDocumentAssignments(userId);

        Map<String, String> response = new HashMap<>();
        response.put(
            "message",
            "User cache cleared successfully for user ID: " + userId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear/document/{documentId}")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> clearDocumentCache(
        @PathVariable Long documentId
    ) {
        permissionCacheService.evictDocumentPublicStatus(documentId);

        Map<String, String> response = new HashMap<>();
        response.put(
            "message",
            "Document cache cleared successfully for document ID: " + documentId
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear/all/users")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> clearAllUserCaches() {
        permissionCacheService.evictAllUserPermissions();
        permissionCacheService.evictAllUserDocumentAssignments();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All user caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear/all")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        permissionCacheService.evictAllUserPermissions();
        permissionCacheService.evictAllUserDocumentAssignments();
        permissionCacheService.evictAllDocumentPublicStatus();

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monitor/health")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<
        CacheHealthService.CacheHealthStatus
    > getCacheHealth() {
        try {
            CacheHealthService.CacheHealthStatus healthStatus =
                cacheHealthService.checkCacheHealth();

            if (healthStatus.isHealthy()) {
                return ResponseEntity.ok(healthStatus);
            } else {
                return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE
                ).body(healthStatus);
            }
        } catch (Exception e) {
            CacheHealthService.CacheHealthStatus errorStatus =
                new CacheHealthService.CacheHealthStatus();
            errorStatus.setHealthy(false);
            errorStatus.setErrorMessage(e.getMessage());
            errorStatus.setLastCheckTime(LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                errorStatus
            );
        }
    }

    @GetMapping("/info")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("cacheProvider", "Redis");
        info.put(
            "cacheNames",
            List.of(
                "userPermissions",
                "documentPublic",
                "userDocumentPermissions"
            )
        );
        info.put("defaultTTL", "10 minutes");
        info.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(info);
    }

    @PostMapping("/warmup/users")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<String> warmupUserPermissions() {
        try {
            cacheWarmupService.warmupUserPermissions();
            return ResponseEntity.ok("用户权限缓存预热已启动");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "缓存预热启动失败: " + e.getMessage()
            );
        }
    }

    @PostMapping("/warmup/documents")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<String> warmupDocumentPublicStatus() {
        try {
            cacheWarmupService.warmupDocumentPublicStatus();
            return ResponseEntity.ok("文档公开状态缓存预热已启动");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "缓存预热启动失败: " + e.getMessage()
            );
        }
    }

    @PostMapping("/warmup/assignments")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<String> warmupDocumentAssignments() {
        try {
            cacheWarmupService.warmupPopularDocumentAssignments();
            return ResponseEntity.ok("文档分配缓存预热已启动");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "缓存预热启动失败: " + e.getMessage()
            );
        }
    }

    @PostMapping("/warmup/all")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<String> warmupAllCaches() {
        try {
            CacheWarmupService.CompleteWarmupResult result = cacheWarmupService
                .performCompleteWarmup()
                .get();
            return ResponseEntity.ok(
                "All caches warmed up successfully. " + result.toString()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                "Failed to warm up all caches: " + e.getMessage()
            );
        }
    }

    // ==================== 内存优化端点 ====================

    @GetMapping("/memory/usage")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<
        CacheMemoryOptimizer.MemoryUsageInfo
    > getMemoryUsage() {
        try {
            CacheMemoryOptimizer.MemoryUsageInfo memoryInfo =
                memoryOptimizer.getMemoryUsageInfo();
            return ResponseEntity.ok(memoryInfo);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/memory/cleanup")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheMemoryOptimizer.CleanupResult>
    > performMemoryCleanup() {
        try {
            CompletableFuture<CacheMemoryOptimizer.CleanupResult> result =
                memoryOptimizer.performCleanup();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/memory/recommendations")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<
        List<CacheMemoryOptimizer.OptimizationRecommendation>
    > getOptimizationRecommendations() {
        try {
            List<
                CacheMemoryOptimizer.OptimizationRecommendation
            > recommendations =
                memoryOptimizer.getOptimizationRecommendations();
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/memory/stats")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getMemoryOptimizationStats() {
        try {
            Map<String, Object> stats = memoryOptimizer.getOptimizationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    // ==================== 智能预热端点 ====================

    @PostMapping("/warmup/smart")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheWarmupStrategy.WarmupExecution>
    > executeSmartWarmup() {
        try {
            CompletableFuture<CacheWarmupStrategy.WarmupExecution> result =
                warmupStrategy.executeSmartWarmup();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/warmup/patterns")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<
        Map<String, CacheWarmupStrategy.AccessPattern>
    > getAccessPatterns() {
        try {
            Map<String, CacheWarmupStrategy.AccessPattern> patterns =
                warmupStrategy.getAccessPatterns();
            return ResponseEntity.ok(patterns);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/warmup/history")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<
        List<CacheWarmupStrategy.WarmupExecution>
    > getWarmupHistory() {
        try {
            List<CacheWarmupStrategy.WarmupExecution> history =
                warmupStrategy.getWarmupHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @GetMapping("/warmup/strategy/stats")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, Object>> getWarmupStrategyStats() {
        try {
            Map<String, Object> stats = warmupStrategy.getStrategyStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/warmup/patterns/reset")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> resetAccessPatterns() {
        try {
            warmupStrategy.resetAccessPatterns();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Access patterns reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/warmup/history/clear")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<Map<String, String>> clearWarmupHistory() {
        try {
            warmupStrategy.clearWarmupHistory();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Warmup history cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/access/record")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, String>> recordCacheAccess(
        @RequestParam String cacheType,
        @RequestParam String key
    ) {
        try {
            warmupStrategy.recordCacheAccess(cacheType, key);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache access recorded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    @PostMapping("/hitrate/update")
    @PreAuthorize("hasAuthority('CACHE:MONITOR')")
    public ResponseEntity<Map<String, String>> updateCacheHitRate(
        @RequestParam String cacheType,
        @RequestParam double hitRate
    ) {
        try {
            warmupStrategy.updateCacheHitRate(cacheType, hitRate);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache hit rate updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }
}
