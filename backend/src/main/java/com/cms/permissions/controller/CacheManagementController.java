package com.cms.permissions.controller;

import com.cms.permissions.service.CacheHealthService;
import com.cms.permissions.service.CachePerformanceAnalyzer;
import com.cms.permissions.service.CacheWarmupService;
import com.cms.permissions.service.PermissionCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
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

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Operation(
        summary = "获取缓存健康状态",
        description = "获取缓存系统的健康状态信息"
    )
    @GetMapping("/management/health")
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

    @Operation(
        summary = "清理并预热用户权限缓存（含分配）并返回统计",
        description = "依次执行：清除所有用户权限缓存 + 清除所有用户文档分配缓存 + 预热所有用户权限缓存；返回预热结果与缓存统计用于验证"
    )
    @PostMapping("/refresh/user-permissions-sequence")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<RefreshSequenceResult> refreshUserPermissionsSequence() {
        LocalDateTime start = LocalDateTime.now();
        PermissionCacheService.CacheStats preStats = permissionCacheService.getCacheStats();

        // 清理缓存
        permissionCacheService.evictAllUserPermissions();
        permissionCacheService.evictAllUserDocumentAssignments();

        PermissionCacheService.CacheStats midStats = permissionCacheService.getCacheStats();

        // 预热缓存
        CacheWarmupService.WarmupResult warmupResult;
        try {
            warmupResult = cacheWarmupService.warmupUserPermissions().get();
        } catch (Exception e) {
            RefreshSequenceResult failed = new RefreshSequenceResult();
            failed.setSuccess(false);
            failed.setMessage("Warmup failed: " + e.getMessage());
            failed.setStartTime(start);
            failed.setEndTime(LocalDateTime.now());
            failed.setPreStats(preStats);
            failed.setMidStats(midStats);
            failed.setPostStats(permissionCacheService.getCacheStats());
            return ResponseEntity.ok(failed);
        }

        PermissionCacheService.CacheStats postStats = permissionCacheService.getCacheStats();

        RefreshSequenceResult result = new RefreshSequenceResult();
        result.setSuccess(true);
        result.setMessage("Clear + warmup + verify completed");
        result.setStartTime(start);
        result.setEndTime(LocalDateTime.now());
        result.setWarmupResult(warmupResult);
        result.setPreStats(preStats);
        result.setMidStats(midStats);
        result.setPostStats(postStats);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "清理全部缓存并执行完整预热，返回统计",
        description = "依次执行：清除用户权限、用户文档分配、文档公开状态等缓存 + 执行完整预热（用户权限/文档公开/热门分配）；返回预热结果与缓存统计用于验证"
    )
    @PostMapping("/refresh/full-sequence")
    @PreAuthorize("hasAuthority('CACHE:MANAGE')")
    public ResponseEntity<RefreshSequenceResult> refreshFullSequence() {
        LocalDateTime start = LocalDateTime.now();
        PermissionCacheService.CacheStats preStats = permissionCacheService.getCacheStats();

        // 清理缓存
        permissionCacheService.evictAllUserPermissions();
        permissionCacheService.evictAllUserDocumentAssignments();
        permissionCacheService.evictAllDocumentPublicStatus();

        PermissionCacheService.CacheStats midStats = permissionCacheService.getCacheStats();

        CacheWarmupService.CompleteWarmupResult warmupResult;
        try {
            warmupResult = cacheWarmupService.performCompleteWarmup().get();
        } catch (Exception e) {
            RefreshSequenceResult failed = new RefreshSequenceResult();
            failed.setSuccess(false);
            failed.setMessage("Full warmup failed: " + e.getMessage());
            failed.setStartTime(start);
            failed.setEndTime(LocalDateTime.now());
            failed.setPreStats(preStats);
            failed.setMidStats(midStats);
            failed.setPostStats(permissionCacheService.getCacheStats());
            return ResponseEntity.ok(failed);
        }

        PermissionCacheService.CacheStats postStats = permissionCacheService.getCacheStats();

        RefreshSequenceResult result = new RefreshSequenceResult();
        result.setSuccess(warmupResult.isSuccess());
        result.setMessage("Full clear + warmup + verify completed");
        result.setStartTime(start);
        result.setEndTime(LocalDateTime.now());
        result.setCompleteWarmupResult(warmupResult);
        result.setPreStats(preStats);
        result.setMidStats(midStats);
        result.setPostStats(postStats);
        return ResponseEntity.ok(result);
    }

    public static class RefreshSequenceResult {
        private boolean success;
        private String message;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private PermissionCacheService.CacheStats preStats;
        private PermissionCacheService.CacheStats midStats;
        private PermissionCacheService.CacheStats postStats;
        private CacheWarmupService.WarmupResult warmupResult;
        private CacheWarmupService.CompleteWarmupResult completeWarmupResult;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public PermissionCacheService.CacheStats getPreStats() { return preStats; }
        public void setPreStats(PermissionCacheService.CacheStats preStats) { this.preStats = preStats; }
        public PermissionCacheService.CacheStats getMidStats() { return midStats; }
        public void setMidStats(PermissionCacheService.CacheStats midStats) { this.midStats = midStats; }
        public PermissionCacheService.CacheStats getPostStats() { return postStats; }
        public void setPostStats(PermissionCacheService.CacheStats postStats) { this.postStats = postStats; }
        public CacheWarmupService.WarmupResult getWarmupResult() { return warmupResult; }
        public void setWarmupResult(CacheWarmupService.WarmupResult warmupResult) { this.warmupResult = warmupResult; }
        public CacheWarmupService.CompleteWarmupResult getCompleteWarmupResult() { return completeWarmupResult; }
        public void setCompleteWarmupResult(CacheWarmupService.CompleteWarmupResult completeWarmupResult) { this.completeWarmupResult = completeWarmupResult; }
    }
}
