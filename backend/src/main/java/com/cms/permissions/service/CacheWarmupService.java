package com.cms.permissions.service;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.User;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存预热服务
 */
@Service
public class CacheWarmupService {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * 预热所有用户权限缓存
     */
    @Async
    public CompletableFuture<WarmupResult> warmupUserPermissions() {
        logger.info("开始预热用户权限缓存");
        WarmupResult result = new WarmupResult();
        result.setStartTime(LocalDateTime.now());
        
        try {
            List<User> users = userRepository.findAll();
            int successCount = 0;
            int failureCount = 0;
            
            for (User user : users) {
                try {
                    permissionCacheService.getUserPermissions(user.getUsername());
                    successCount++;
                } catch (Exception e) {
                    logger.error("预热用户 {} 的权限缓存失败: {}", user.getUsername(), e.getMessage());
                    failureCount++;
                }
            }
            
            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            result.setTotalCount(users.size());
            result.setSuccess(true);
            
            logger.info("用户权限缓存预热完成: 成功 {}, 失败 {}, 总计 {}", 
                       successCount, failureCount, users.size());
            
        } catch (Exception e) {
            logger.error("用户权限缓存预热失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(LocalDateTime.now());
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 预热文档公开状态缓存
     */
    @Async
    public CompletableFuture<WarmupResult> warmupDocumentPublicStatus() {
        logger.info("开始预热文档公开状态缓存");
        WarmupResult result = new WarmupResult();
        result.setStartTime(LocalDateTime.now());
        
        try {
            List<Document> documents = documentRepository.findAll();
            int successCount = 0;
            int failureCount = 0;
            
            for (Document document : documents) {
                try {
                    permissionCacheService.isDocumentPublic(document.getId());
                    successCount++;
                } catch (Exception e) {
                    logger.error("预热文档 {} 的公开状态缓存失败: {}", document.getId(), e.getMessage());
                    failureCount++;
                }
            }
            
            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            result.setTotalCount(documents.size());
            result.setSuccess(true);
            
            logger.info("文档公开状态缓存预热完成: 成功 {}, 失败 {}, 总计 {}", 
                       successCount, failureCount, documents.size());
            
        } catch (Exception e) {
            logger.error("文档公开状态缓存预热失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(LocalDateTime.now());
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 预热用户文档分配缓存（热门文档）
     */
    @Async
    public CompletableFuture<WarmupResult> warmupPopularDocumentAssignments() {
        logger.info("开始预热热门文档分配缓存");
        WarmupResult result = new WarmupResult();
        result.setStartTime(LocalDateTime.now());
        
        try {
            // 获取所有文档和用户（简化版本）
            List<Document> recentDocuments = documentRepository.findAll();
            List<User> activeUsers = userRepository.findAll();
            
            // 限制数量以避免过多操作
            if (recentDocuments.size() > 50) {
                recentDocuments = recentDocuments.subList(0, 50);
            }
            if (activeUsers.size() > 100) {
                activeUsers = activeUsers.subList(0, 100);
            }
            
            int successCount = 0;
            int failureCount = 0;
            int totalOperations = Math.min(recentDocuments.size() * activeUsers.size(), 1000); // 限制操作数量
            
            for (Document document : recentDocuments) {
                for (User user : activeUsers) {
                    if (successCount + failureCount >= totalOperations) {
                        break;
                    }
                    
                    try {
                        permissionCacheService.getUserDocumentAssignments(user.getId(), document.getId());
                        successCount++;
                    } catch (Exception e) {
                        logger.error("预热用户 {} 对文档 {} 的分配缓存失败: {}", 
                                   user.getId(), document.getId(), e.getMessage());
                        failureCount++;
                    }
                }
                
                if (successCount + failureCount >= totalOperations) {
                    break;
                }
            }
            
            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            result.setTotalCount(totalOperations);
            result.setSuccess(true);
            
            logger.info("热门文档分配缓存预热完成: 成功 {}, 失败 {}, 总计 {}", 
                       successCount, failureCount, totalOperations);
            
        } catch (Exception e) {
            logger.error("热门文档分配缓存预热失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(LocalDateTime.now());
        return CompletableFuture.completedFuture(result);
    }

    /**
     * 执行完整的缓存预热
     */
    @Async
    public CompletableFuture<CompleteWarmupResult> performCompleteWarmup() {
        logger.info("开始执行完整缓存预热");
        CompleteWarmupResult completeResult = new CompleteWarmupResult();
        completeResult.setStartTime(LocalDateTime.now());
        
        try {
            // 并行执行所有预热操作
            CompletableFuture<WarmupResult> userPermissionsFuture = warmupUserPermissions();
            CompletableFuture<WarmupResult> documentPublicFuture = warmupDocumentPublicStatus();
            CompletableFuture<WarmupResult> documentAssignmentsFuture = warmupPopularDocumentAssignments();
            
            // 等待所有操作完成
            CompletableFuture.allOf(userPermissionsFuture, documentPublicFuture, documentAssignmentsFuture).join();
            
            completeResult.setUserPermissionsResult(userPermissionsFuture.get());
            completeResult.setDocumentPublicResult(documentPublicFuture.get());
            completeResult.setDocumentAssignmentsResult(documentAssignmentsFuture.get());
            
            // 计算总体结果
            int totalSuccess = completeResult.getUserPermissionsResult().getSuccessCount() +
                              completeResult.getDocumentPublicResult().getSuccessCount() +
                              completeResult.getDocumentAssignmentsResult().getSuccessCount();
            
            int totalFailure = completeResult.getUserPermissionsResult().getFailureCount() +
                              completeResult.getDocumentPublicResult().getFailureCount() +
                              completeResult.getDocumentAssignmentsResult().getFailureCount();
            
            completeResult.setTotalSuccessCount(totalSuccess);
            completeResult.setTotalFailureCount(totalFailure);
            completeResult.setSuccess(totalFailure == 0);
            
            logger.info("完整缓存预热完成: 总成功 {}, 总失败 {}", totalSuccess, totalFailure);
            
        } catch (Exception e) {
            logger.error("完整缓存预热失败", e);
            completeResult.setSuccess(false);
            completeResult.setErrorMessage(e.getMessage());
        }
        
        completeResult.setEndTime(LocalDateTime.now());
        return CompletableFuture.completedFuture(completeResult);
    }

    /**
     * 预热结果
     */
    public static class WarmupResult {
        private boolean success;
        private int successCount;
        private int failureCount;
        private int totalCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }

        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }

    /**
     * 完整预热结果
     */
    public static class CompleteWarmupResult {
        private boolean success;
        private WarmupResult userPermissionsResult;
        private WarmupResult documentPublicResult;
        private WarmupResult documentAssignmentsResult;
        private int totalSuccessCount;
        private int totalFailureCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String errorMessage;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public WarmupResult getUserPermissionsResult() { return userPermissionsResult; }
        public void setUserPermissionsResult(WarmupResult userPermissionsResult) { this.userPermissionsResult = userPermissionsResult; }

        public WarmupResult getDocumentPublicResult() { return documentPublicResult; }
        public void setDocumentPublicResult(WarmupResult documentPublicResult) { this.documentPublicResult = documentPublicResult; }

        public WarmupResult getDocumentAssignmentsResult() { return documentAssignmentsResult; }
        public void setDocumentAssignmentsResult(WarmupResult documentAssignmentsResult) { this.documentAssignmentsResult = documentAssignmentsResult; }

        public int getTotalSuccessCount() { return totalSuccessCount; }
        public void setTotalSuccessCount(int totalSuccessCount) { this.totalSuccessCount = totalSuccessCount; }

        public int getTotalFailureCount() { return totalFailureCount; }
        public void setTotalFailureCount(int totalFailureCount) { this.totalFailureCount = totalFailureCount; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
    }
}