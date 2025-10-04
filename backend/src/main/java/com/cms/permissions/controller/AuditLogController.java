package com.cms.permissions.controller;

import com.cms.permissions.entity.AuditLog;
import com.cms.permissions.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAuthority('AUDIT:READ')")
@Tag(name = "审计日志", description = "审计日志管理 API")
public class AuditLogController {

    @Autowired
    private AuditService auditService;

    @Operation(
        summary = "查询审计日志",
        description = "根据各种条件查询审计日志"
    )
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
        @Parameter(description = "页码，从0开始", example = "0") @RequestParam(
            defaultValue = "0"
        ) int page,
        @Parameter(description = "每页大小", example = "10") @RequestParam(
            defaultValue = "10"
        ) int size,
        @Parameter(
            description = "排序字段",
            example = "timestamp"
        ) @RequestParam(defaultValue = "timestamp") String sortBy,
        @Parameter(description = "排序方向", example = "desc") @RequestParam(
            defaultValue = "desc"
        ) String sortDir,
        @Parameter(description = "用户名") @RequestParam(
            required = false
        ) String username,
        @Parameter(description = "操作类型") @RequestParam(
            required = false
        ) String operationType,
        @Parameter(description = "资源类型") @RequestParam(
            required = false
        ) String resourceType,
        @Parameter(description = "开始时间") @RequestParam(
            required = false
        ) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime startDate,
        @Parameter(description = "结束时间") @RequestParam(
            required = false
        ) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        ) LocalDateTime endDate,
        @Parameter(description = "结果状态") @RequestParam(
            required = false
        ) String result
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> logs = auditService.findByFilters(
            username,
            operationType,
            resourceType,
            startDate,
            endDate,
            result,
            pageable
        );

        return ResponseEntity.ok(logs);
    }

    @Operation(
        summary = "根据用户名查询审计日志",
        description = "根据指定用户名查询审计日志"
    )
    @GetMapping("/logs/user/{username}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(
        @Parameter(description = "用户名") @PathVariable String username,
        @Parameter(description = "页码，从0开始", example = "0") @RequestParam(
            defaultValue = "0"
        ) int page,
        @Parameter(description = "每页大小", example = "10") @RequestParam(
            defaultValue = "10"
        ) int size,
        @Parameter(
            description = "排序字段",
            example = "timestamp"
        ) @RequestParam(defaultValue = "timestamp") String sortBy,
        @Parameter(description = "排序方向", example = "desc") @RequestParam(
            defaultValue = "desc"
        ) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> logs = auditService.findByUsername(username, pageable);

        return ResponseEntity.ok(logs);
    }

    @Operation(
        summary = "根据资源类型查询审计日志",
        description = "根据指定资源类型查询审计日志"
    )
    @GetMapping("/logs/type/{resourceType}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByResourceType(
        @Parameter(description = "资源类型") @PathVariable String resourceType,
        @Parameter(description = "页码，从0开始", example = "0") @RequestParam(
            defaultValue = "0"
        ) int page,
        @Parameter(description = "每页大小", example = "10") @RequestParam(
            defaultValue = "10"
        ) int size,
        @Parameter(
            description = "排序字段",
            example = "timestamp"
        ) @RequestParam(defaultValue = "timestamp") String sortBy,
        @Parameter(description = "排序方向", example = "desc") @RequestParam(
            defaultValue = "desc"
        ) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AuditLog> logs = auditService.findByResourceType(
            resourceType,
            pageable
        );

        return ResponseEntity.ok(logs);
    }

    @Operation(
        summary = "根据操作类型查询审计日志",
        description = "根据指定操作类型查询审计日志"
    )
    @GetMapping("/logs/operation/{operationType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByOperationType(
        @Parameter(description = "操作类型") @PathVariable String operationType
    ) {
        List<AuditLog> logs = auditService.findByOperationType(operationType);

        return ResponseEntity.ok(logs);
    }

    @Operation(
        summary = "查询最近的审计日志",
        description = "查询指定小时数内的审计日志"
    )
    @GetMapping("/logs/recent/{hours}")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs(
        @Parameter(
            description = "小时数",
            example = "24"
        ) @PathVariable int hours
    ) {
        List<AuditLog> logs = auditService.findRecentLogs(hours);

        return ResponseEntity.ok(logs);
    }

    @Operation(
        summary = "获取审计统计信息",
        description = "获取审计日志的统计信息"
    )
    @GetMapping("/stats")
    public ResponseEntity<Object> getAuditStats() {
        Object stats = getAuditStatistics();
        return ResponseEntity.ok(stats);
    }

    private AuditStats getAuditStatistics() {
        Page<AuditLog> page = auditService.findAll(PageRequest.of(0, 1));
        long totalLogs = page.getTotalElements();

        return new AuditStats(
            totalLogs,
            auditService.countByOperationType("CREATE"),
            auditService.countByOperationType("READ"),
            auditService.countByOperationType("UPDATE"),
            auditService.countByOperationType("DELETE"),
            auditService.countByResourceType("PERMISSION"),
            auditService.countByResourceType("ROLE"),
            auditService.countByResourceType("USER"),
            auditService.countByResult("SUCCESS"),
            auditService.countByResult("FAILURE")
        );
    }

    // Static class to hold audit statistics
    public static class AuditStats {

        public long totalLogs;
        public long createOperations;
        public long readOperations;
        public long updateOperations;
        public long deleteOperations;
        public long permissionLogs;
        public long roleLogs;
        public long userLogs;
        public long successCount;
        public long failureCount;

        public AuditStats(
            long totalLogs,
            long createOperations,
            long readOperations,
            long updateOperations,
            long deleteOperations,
            long permissionLogs,
            long roleLogs,
            long userLogs,
            long successCount,
            long failureCount
        ) {
            this.totalLogs = totalLogs;
            this.createOperations = createOperations;
            this.readOperations = readOperations;
            this.updateOperations = updateOperations;
            this.deleteOperations = deleteOperations;
            this.permissionLogs = permissionLogs;
            this.roleLogs = roleLogs;
            this.userLogs = userLogs;
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
    }

    @Operation(
        summary = "获取所有唯一用户名",
        description = "获取审计日志中出现的所有唯一用户名"
    )
    @GetMapping("/distinct/usernames")
    public ResponseEntity<List<String>> getDistinctUsernames() {
        List<String> usernames = auditService.findDistinctUsernames();
        return ResponseEntity.ok(usernames);
    }

    @Operation(
        summary = "获取所有操作类型",
        description = "获取审计日志中出现的所有操作类型"
    )
    @GetMapping("/distinct/operation-types")
    public ResponseEntity<List<String>> getDistinctOperationTypes() {
        List<String> operationTypes = auditService.findDistinctOperationTypes();
        return ResponseEntity.ok(operationTypes);
    }

    @Operation(
        summary = "获取所有资源类型",
        description = "获取审计日志中出现的所有资源类型"
    )
    @GetMapping("/distinct/resource-types")
    public ResponseEntity<List<String>> getDistinctResourceTypes() {
        List<String> resourceTypes = auditService.findDistinctResourceTypes();
        return ResponseEntity.ok(resourceTypes);
    }
}
