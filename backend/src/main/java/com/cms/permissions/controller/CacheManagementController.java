package com.cms.permissions.controller;

import com.cms.permissions.service.CacheHealthService;
import com.cms.permissions.service.CachePerformanceAnalyzer;
import com.cms.permissions.service.CacheWarmupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasAuthority('CACHE:READ')")
@Tag(name = "缓存管理", description = "缓存管理 API")
public class CacheManagementController {

    @Autowired
    private CacheHealthService cacheHealthService;

    @Autowired
    private CachePerformanceAnalyzer cachePerformanceAnalyzer;

    @Autowired
    private CacheWarmupService cacheWarmupService;

    @Operation(
        summary = "获取缓存健康状态",
        description = "获取缓存系统的健康状态信息"
    )
    @GetMapping("/health")
    public ResponseEntity<
        CacheHealthService.CacheHealthStatus
    > getCacheHealth() {
        CacheHealthService.CacheHealthStatus healthStatus =
            cacheHealthService.checkCacheHealth();
        return ResponseEntity.ok(healthStatus);
    }

    @Operation(
        summary = "获取缓存性能报告",
        description = "获取缓存系统的性能分析报告"
    )
    @GetMapping("/performance")
    public ResponseEntity<
        CachePerformanceAnalyzer.PerformanceReport
    > getPerformanceReport() {
        CachePerformanceAnalyzer.PerformanceReport report =
            cachePerformanceAnalyzer.getPerformanceReport();
        return ResponseEntity.ok(report);
    }

    @Operation(
        summary = "预热用户权限缓存",
        description = "预热所有用户的权限缓存"
    )
    @PostMapping("/warmup/user-permissions")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheWarmupService.WarmupResult>
    > warmupUserPermissions() {
        CompletableFuture<CacheWarmupService.WarmupResult> result =
            cacheWarmupService.warmupUserPermissions();
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "预热文档公开状态缓存",
        description = "预热文档公开状态缓存"
    )
    @PostMapping("/warmup/document-public-status")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheWarmupService.WarmupResult>
    > warmupDocumentPublicStatus() {
        CompletableFuture<CacheWarmupService.WarmupResult> result =
            cacheWarmupService.warmupDocumentPublicStatus();
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "预热热门文档分配缓存",
        description = "预热热门文档分配缓存"
    )
    @PostMapping("/warmup/popular-document-assignments")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheWarmupService.WarmupResult>
    > warmupPopularDocumentAssignments() {
        CompletableFuture<CacheWarmupService.WarmupResult> result =
            cacheWarmupService.warmupPopularDocumentAssignments();
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "执行完整缓存预热",
        description = "执行完整的缓存预热操作"
    )
    @PostMapping("/warmup/full")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<
        CompletableFuture<CacheWarmupService.CompleteWarmupResult>
    > performCompleteWarmup() {
        CompletableFuture<CacheWarmupService.CompleteWarmupResult> result =
            cacheWarmupService.performCompleteWarmup();
        return ResponseEntity.ok(result);
    }
}
