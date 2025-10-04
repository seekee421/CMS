package com.cms.permissions.controller;

import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.service.DocumentMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/migration")
@Tag(name = "文档迁移", description = "文档迁移相关API")
public class DocumentMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentMigrationController.class);
    
    @Autowired
    private DocumentMigrationService migrationService;
    
    /**
     * 单个URL迁移
     */
    @PostMapping("/migrate")
    @Operation(summary = "迁移单个文档", description = "从指定URL迁移文档到系统中")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse<MigrationResponse>> migrateDocument(
            @Valid @RequestBody MigrationRequest request) {
        
        logger.info("收到文档迁移请求: {}", request.getUrl());
        
        try {
            // 这里应该从认证上下文获取用户ID，暂时使用固定值
            Long userId = getCurrentUserId();
            
            DocumentMigrationService.MigrationResult result = 
                migrationService.migrateDocument(request.getUrl(), userId);
            
            MigrationResponse response = new MigrationResponse(
                result.isSuccess(),
                result.getMessage(),
                result.getDocumentId()
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response, "文档迁移成功"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response, "文档迁移失败"));
            }
            
        } catch (Exception e) {
            logger.error("文档迁移异常: {}", request.getUrl(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 批量URL迁移
     */
    @PostMapping("/migrate/batch")
    @Operation(summary = "批量迁移文档", description = "从多个URL批量迁移文档到系统中")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse<BatchMigrationResponse>> migrateBatch(
            @Valid @RequestBody BatchMigrationRequest request) {
        
        logger.info("收到批量文档迁移请求，URL数量: {}", request.getUrls().size());
        
        try {
            Long userId = getCurrentUserId();
            
            DocumentMigrationService.BatchMigrationResult result = 
                migrationService.migrateBatch(request.getUrls(), userId);
            
            BatchMigrationResponse response = new BatchMigrationResponse(
                result.getSuccessCount(),
                result.getFailureCount(),
                result.getSuccessUrls(),
                result.getFailureUrls(),
                result.getMessages()
            );
            
            return ResponseEntity.ok(ApiResponse.success(response, "批量迁移完成"));
            
        } catch (Exception e) {
            logger.error("批量文档迁移异常", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取迁移统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取迁移统计", description = "获取文档迁移的统计信息")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse<DocumentMigrationService.MigrationStatistics>> getStatistics() {
        
        try {
            DocumentMigrationService.MigrationStatistics statistics = 
                migrationService.getMigrationStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics, "获取统计信息成功"));
            
        } catch (Exception e) {
            logger.error("获取迁移统计信息异常", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取迁移历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取迁移历史", description = "获取用户的文档迁移历史记录")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse<List<MigrationLog>>> getHistory(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Long userId = getCurrentUserId();
            
            List<MigrationLog> history = migrationService.getMigrationHistory(userId, page, size);
            
            return ResponseEntity.ok(ApiResponse.success(history, "获取迁移历史成功"));
            
        } catch (Exception e) {
            logger.error("获取迁移历史异常", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 重试失败的迁移
     */
    @PostMapping("/retry/{migrationLogId}")
    @Operation(summary = "重试失败迁移", description = "重新执行失败的文档迁移任务")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ResponseEntity<ApiResponse<MigrationResponse>> retryMigration(
            @Parameter(description = "迁移日志ID") @PathVariable Long migrationLogId) {
        
        logger.info("收到重试迁移请求，迁移日志ID: {}", migrationLogId);
        
        try {
            Long userId = getCurrentUserId();
            
            DocumentMigrationService.MigrationResult result = 
                migrationService.retryFailedMigration(migrationLogId, userId);
            
            MigrationResponse response = new MigrationResponse(
                result.isSuccess(),
                result.getMessage(),
                result.getDocumentId()
            );
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response, "重试迁移成功"));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(response, "重试迁移失败"));
            }
            
        } catch (Exception e) {
            logger.error("重试迁移异常，迁移日志ID: {}", migrationLogId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户ID（临时实现）
     */
    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        // 这里暂时返回固定值，实际应该从认证信息中获取
        return 1L;
    }
    
    /**
     * 单个迁移请求DTO
     */
    public static class MigrationRequest {
        @NotBlank(message = "URL不能为空")
        private String url;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
    
    /**
     * 批量迁移请求DTO
     */
    public static class BatchMigrationRequest {
        @NotEmpty(message = "URL列表不能为空")
        @Size(max = 50, message = "单次最多迁移50个URL")
        private List<@NotBlank(message = "URL不能为空") String> urls;
        
        public List<String> getUrls() { return urls; }
        public void setUrls(List<String> urls) { this.urls = urls; }
    }
    
    /**
     * 迁移响应DTO
     */
    public static class MigrationResponse {
        private boolean success;
        private String message;
        private Long documentId;
        
        public MigrationResponse(boolean success, String message, Long documentId) {
            this.success = success;
            this.message = message;
            this.documentId = documentId;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getDocumentId() { return documentId; }
    }
    
    /**
     * 批量迁移响应DTO
     */
    public static class BatchMigrationResponse {
        private int successCount;
        private int failureCount;
        private List<String> successUrls;
        private List<String> failureUrls;
        private List<String> messages;
        
        public BatchMigrationResponse(int successCount, int failureCount, 
                                    List<String> successUrls, List<String> failureUrls, 
                                    List<String> messages) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.successUrls = successUrls;
            this.failureUrls = failureUrls;
            this.messages = messages;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getSuccessUrls() { return successUrls; }
        public List<String> getFailureUrls() { return failureUrls; }
        public List<String> getMessages() { return messages; }
    }
    
    /**
     * 通用API响应包装类
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        private ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, message, data);
        }
        
        public static <T> ApiResponse<T> error(T data, String message) {
            return new ApiResponse<>(false, message, data);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}