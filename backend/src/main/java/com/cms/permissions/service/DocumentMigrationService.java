package com.cms.permissions.service;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.MigrationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DocumentMigrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentMigrationService.class);
    
    @Autowired
    private WebCrawlerService webCrawlerService;
    
    @Autowired
    private ContentParserService contentParserService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private MigrationLogRepository migrationLogRepository;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    /**
     * 单个URL迁移
     */
    @Transactional
    public MigrationResult migrateDocument(String url, Long userId) {
        logger.info("开始迁移文档: {}", url);
        
        // 检查是否已经迁移过
        if (documentRepository.existsBySourceUrl(url)) {
            logger.warn("文档已存在，跳过迁移: {}", url);
            return new MigrationResult(false, "文档已存在", null);
        }
        
        // 创建迁移日志
        MigrationLog migrationLog = new MigrationLog(url, userId);
        migrationLog.setStartTime(LocalDateTime.now());
        migrationLog.setStatus(MigrationLog.MigrationStatus.IN_PROGRESS);
        migrationLog = migrationLogRepository.save(migrationLog);
        
        try {
            // 1. 爬取网页内容
            WebCrawlerService.CrawlResult crawlResult = webCrawlerService.crawlPage(url);
            if (!crawlResult.isSuccess()) {
                throw new RuntimeException("网页爬取失败: " + crawlResult.getErrorMessage());
            }
            
            // 2. 解析内容
            ContentParserService.ParsedContent parsedContent = contentParserService.parseContent(crawlResult);
            
            // 3. 创建文档实体
            Document document = createDocumentFromParsedContent(parsedContent, userId);
            document = documentRepository.save(document);
            
            // 4. 更新迁移日志
            migrationLog.setDocumentId(document.getId());
            migrationLog.setStatus(MigrationLog.MigrationStatus.COMPLETED);
            migrationLog.setEndTime(LocalDateTime.now());
            migrationLog.setProcessedContentSize((long) parsedContent.getContentLength());
            migrationLogRepository.save(migrationLog);
            
            logger.info("文档迁移成功: {} -> 文档ID: {}", url, document.getId());
            return new MigrationResult(true, "迁移成功", document.getId());
            
        } catch (Exception e) {
            logger.error("文档迁移失败: {}, 错误: {}", url, e.getMessage(), e);
            
            // 更新迁移日志为失败状态
            migrationLog.setStatus(MigrationLog.MigrationStatus.FAILED);
            migrationLog.setEndTime(LocalDateTime.now());
            migrationLog.setErrorMessage(e.getMessage());
            migrationLogRepository.save(migrationLog);
            
            return new MigrationResult(false, "迁移失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 批量URL迁移
     */
    public BatchMigrationResult migrateBatch(List<String> urls, Long userId) {
        logger.info("开始批量迁移，URL数量: {}", urls.size());
        
        BatchMigrationResult batchResult = new BatchMigrationResult();
        List<CompletableFuture<MigrationResult>> futures = new ArrayList<>();
        
        // 并发处理迁移任务
        for (String url : urls) {
            CompletableFuture<MigrationResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return migrateDocument(url, userId);
                } catch (Exception e) {
                    logger.error("批量迁移中单个URL处理失败: {}", url, e);
                    return new MigrationResult(false, "处理失败: " + e.getMessage(), null);
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 收集结果
        for (int i = 0; i < urls.size(); i++) {
            try {
                MigrationResult result = futures.get(i).get();
                batchResult.addResult(urls.get(i), result);
            } catch (Exception e) {
                logger.error("获取迁移结果失败: {}", urls.get(i), e);
                batchResult.addResult(urls.get(i), new MigrationResult(false, "结果获取失败", null));
            }
        }
        
        logger.info("批量迁移完成，成功: {}, 失败: {}", 
                   batchResult.getSuccessCount(), batchResult.getFailureCount());
        
        return batchResult;
    }
    
    /**
     * 从解析内容创建文档实体
     */
    private Document createDocumentFromParsedContent(ContentParserService.ParsedContent parsedContent, Long userId) {
        Document document = new Document();
        document.setTitle(parsedContent.getTitle());
        document.setContent(parsedContent.getContent());
        document.setStatus(Document.DocumentStatus.PUBLISHED); // 迁移的文档默认为已发布
        document.setIsPublic(true); // 迁移的文档默认为公开
        document.setCreatedBy(userId);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        
        // 设置迁移相关字段
        document.setSourceUrl(parsedContent.getSourceUrl());
        document.setCategory(parsedContent.getCategory());
        document.setTags(parsedContent.getTags());
        document.setVersion(parsedContent.getVersion());
        document.setOriginalId(parsedContent.getOriginalId());
        document.setMigrationStatus(Document.MigrationStatus.COMPLETED);
        document.setMigrationDate(LocalDateTime.now());
        
        return document;
    }
    
    /**
     * 获取迁移统计信息
     */
    public MigrationStatistics getMigrationStatistics() {
        List<Object[]> statusCounts = migrationLogRepository.countByStatus();
        
        MigrationStatistics stats = new MigrationStatistics();
        for (Object[] row : statusCounts) {
            MigrationLog.MigrationStatus status = (MigrationLog.MigrationStatus) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case PENDING:
                    stats.setPendingCount(count.intValue());
                    break;
                case IN_PROGRESS:
                    stats.setInProgressCount(count.intValue());
                    break;
                case COMPLETED:
                    stats.setCompletedCount(count.intValue());
                    break;
                case FAILED:
                    stats.setFailedCount(count.intValue());
                    break;
            }
        }
        
        return stats;
    }
    
    /**
     * 获取迁移历史
     */
    public List<MigrationLog> getMigrationHistory(Long userId, int page, int size) {
        // 这里简化实现，实际应该使用分页
        return migrationLogRepository.findByCreatedByOrderByCreatedAtDesc(userId, 
                org.springframework.data.domain.PageRequest.of(page, size)).getContent();
    }
    
    /**
     * 重试失败的迁移
     */
    @Transactional
    public MigrationResult retryFailedMigration(Long migrationLogId, Long userId) {
        MigrationLog migrationLog = migrationLogRepository.findById(migrationLogId)
                .orElseThrow(() -> new RuntimeException("迁移日志不存在"));
        
        if (migrationLog.getStatus() != MigrationLog.MigrationStatus.FAILED) {
            throw new RuntimeException("只能重试失败的迁移任务");
        }
        
        return migrateDocument(migrationLog.getSourceUrl(), userId);
    }
    
    /**
     * 迁移结果类
     */
    public static class MigrationResult {
        private boolean success;
        private String message;
        private Long documentId;
        
        public MigrationResult(boolean success, String message, Long documentId) {
            this.success = success;
            this.message = message;
            this.documentId = documentId;
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getDocumentId() { return documentId; }
    }
    
    /**
     * 批量迁移结果类
     */
    public static class BatchMigrationResult {
        private List<String> successUrls = new ArrayList<>();
        private List<String> failureUrls = new ArrayList<>();
        private List<String> messages = new ArrayList<>();
        
        public void addResult(String url, MigrationResult result) {
            if (result.isSuccess()) {
                successUrls.add(url);
            } else {
                failureUrls.add(url);
            }
            messages.add(url + ": " + result.getMessage());
        }
        
        public int getSuccessCount() { return successUrls.size(); }
        public int getFailureCount() { return failureUrls.size(); }
        public List<String> getSuccessUrls() { return successUrls; }
        public List<String> getFailureUrls() { return failureUrls; }
        public List<String> getMessages() { return messages; }
    }
    
    /**
     * 迁移统计类
     */
    public static class MigrationStatistics {
        private int pendingCount;
        private int inProgressCount;
        private int completedCount;
        private int failedCount;
        
        // Getter和Setter方法
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        
        public int getInProgressCount() { return inProgressCount; }
        public void setInProgressCount(int inProgressCount) { this.inProgressCount = inProgressCount; }
        
        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
        
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        
        public int getTotalCount() {
            return pendingCount + inProgressCount + completedCount + failedCount;
        }
    }
}