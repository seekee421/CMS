package com.cms.permissions.service;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.MigrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class DocumentMigrationServiceTest {
    
    @Mock
    private WebCrawlerService webCrawlerService;
    
    @Mock
    private ContentParserService contentParserService;
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private MigrationLogRepository migrationLogRepository;
    
    @InjectMocks
    private DocumentMigrationService migrationService;
    
    private String testUrl;
    private Long testUserId;
    private WebCrawlerService.CrawlResult mockCrawlResult;
    private ContentParserService.ParsedContent mockParsedContent;
    
    @BeforeEach
    void setUp() {
        testUrl = "https://example.com/test-article";
        testUserId = 1L;
        
        // 模拟爬取结果
        mockCrawlResult = new WebCrawlerService.CrawlResult();
        mockCrawlResult.setSuccess(true);
        mockCrawlResult.setTitle("Test Title");
        mockCrawlResult.setContent("<h1>Test Title</h1><p>Test content</p>");
        mockCrawlResult.setErrorMessage(null);
        
        // 模拟解析结果
        mockParsedContent = new ContentParserService.ParsedContent();
        mockParsedContent.setTitle("Test Title");
        mockParsedContent.setContent("# Test Title\n\nTest content");
        mockParsedContent.setSourceUrl(testUrl);
        mockParsedContent.setCategory("技术");
        mockParsedContent.setTags("测试,单元测试");
        mockParsedContent.setVersion("1.0");
        mockParsedContent.setOriginalId("test-123");
        mockParsedContent.setWordCount(100);
    }
    
    @Test
    void testMigrateDocument_Success() {
        // 准备测试数据
        when(documentRepository.existsBySourceUrl(testUrl)).thenReturn(false);
        when(webCrawlerService.crawlPage(testUrl)).thenReturn(mockCrawlResult);
        when(contentParserService.parseContent(mockCrawlResult)).thenReturn(mockParsedContent);
        
        MigrationLog savedLog = new MigrationLog(testUrl, testUserId);
        savedLog.setId(1L);
        when(migrationLogRepository.save(any(MigrationLog.class))).thenReturn(savedLog);
        
        Document savedDocument = new Document();
        savedDocument.setId(1L);
        savedDocument.setTitle("Test Title");
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        
        // 执行测试
        DocumentMigrationService.MigrationResult result = migrationService.migrateDocument(testUrl, testUserId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("迁移成功", result.getMessage());
        assertEquals(1L, result.getDocumentId());
        
        // 验证方法调用
        verify(documentRepository).existsBySourceUrl(testUrl);
        verify(webCrawlerService).crawlPage(testUrl);
        verify(contentParserService).parseContent(mockCrawlResult);
        verify(documentRepository).save(any(Document.class));
        verify(migrationLogRepository, times(2)).save(any(MigrationLog.class));
    }
    
    @Test
    void testMigrateDocument_DocumentAlreadyExists() {
        // 准备测试数据
        when(documentRepository.existsBySourceUrl(testUrl)).thenReturn(true);
        
        // 执行测试
        DocumentMigrationService.MigrationResult result = migrationService.migrateDocument(testUrl, testUserId);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("文档已存在", result.getMessage());
        assertNull(result.getDocumentId());
        
        // 验证方法调用
        verify(documentRepository).existsBySourceUrl(testUrl);
        verify(webCrawlerService, never()).crawlPage(any());
        verify(contentParserService, never()).parseContent(any());
        verify(documentRepository, never()).save(any(Document.class));
    }
    
    @Test
    void testMigrateDocument_CrawlFailure() {
        // 准备测试数据
        when(documentRepository.existsBySourceUrl(testUrl)).thenReturn(false);
        
        WebCrawlerService.CrawlResult failedCrawlResult = new WebCrawlerService.CrawlResult();
        failedCrawlResult.setSuccess(false);
        failedCrawlResult.setTitle(null);
        failedCrawlResult.setContent(null);
        failedCrawlResult.setErrorMessage("网络连接失败");
        when(webCrawlerService.crawlPage(testUrl)).thenReturn(failedCrawlResult);
        
        MigrationLog savedLog = new MigrationLog(testUrl, testUserId);
        savedLog.setId(1L);
        when(migrationLogRepository.save(any(MigrationLog.class))).thenReturn(savedLog);
        
        // 执行测试
        DocumentMigrationService.MigrationResult result = migrationService.migrateDocument(testUrl, testUserId);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("迁移失败"));
        assertNull(result.getDocumentId());
        
        // 验证方法调用
        verify(documentRepository).existsBySourceUrl(testUrl);
        verify(webCrawlerService).crawlPage(testUrl);
        verify(contentParserService, never()).parseContent(any());
        verify(documentRepository, never()).save(any(Document.class));
        verify(migrationLogRepository, times(2)).save(any(MigrationLog.class)); // 初始保存和失败更新
    }
    
    @Test
    void testMigrateBatch_Success() {
        // 准备测试数据
        List<String> urls = Arrays.asList(
            "https://example.com/article1",
            "https://example.com/article2"
        );
        
        when(documentRepository.existsBySourceUrl(anyString())).thenReturn(false);
        when(webCrawlerService.crawlPage(anyString())).thenReturn(mockCrawlResult);
        when(contentParserService.parseContent(any())).thenReturn(mockParsedContent);
        
        MigrationLog savedLog = new MigrationLog("test", testUserId);
        savedLog.setId(1L);
        when(migrationLogRepository.save(any(MigrationLog.class))).thenReturn(savedLog);
        
        Document savedDocument = new Document();
        savedDocument.setId(1L);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        
        // 执行测试
        DocumentMigrationService.BatchMigrationResult result = migrationService.migrateBatch(urls, testUserId);
        
        // 验证结果
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(2, result.getSuccessUrls().size());
        assertEquals(0, result.getFailureUrls().size());
        
        // 验证方法调用次数
        verify(documentRepository, times(2)).existsBySourceUrl(anyString());
        verify(webCrawlerService, times(2)).crawlPage(anyString());
        verify(contentParserService, times(2)).parseContent(any());
        verify(documentRepository, times(2)).save(any(Document.class));
    }
    
    @Test
    void testGetMigrationStatistics() {
        // 准备测试数据
        Object[][] statusCounts = {
            {MigrationLog.MigrationStatus.PENDING, 5L},
            {MigrationLog.MigrationStatus.IN_PROGRESS, 2L},
            {MigrationLog.MigrationStatus.COMPLETED, 10L},
            {MigrationLog.MigrationStatus.FAILED, 3L}
        };
        
        when(migrationLogRepository.countByStatus()).thenReturn(Arrays.asList(statusCounts));
        
        // 执行测试
        DocumentMigrationService.MigrationStatistics stats = migrationService.getMigrationStatistics();
        
        // 验证结果
        assertEquals(5, stats.getPendingCount());
        assertEquals(2, stats.getInProgressCount());
        assertEquals(10, stats.getCompletedCount());
        assertEquals(3, stats.getFailedCount());
        assertEquals(20, stats.getTotalCount());
        
        // 验证方法调用
        verify(migrationLogRepository).countByStatus();
    }
    
    @Test
    void testRetryFailedMigration_Success() {
        // 准备测试数据
        Long migrationLogId = 1L;
        MigrationLog failedLog = new MigrationLog(testUrl, testUserId);
        failedLog.setId(migrationLogId);
        failedLog.setStatus(MigrationLog.MigrationStatus.FAILED);
        
        when(migrationLogRepository.findById(migrationLogId)).thenReturn(Optional.of(failedLog));
        when(documentRepository.existsBySourceUrl(testUrl)).thenReturn(false);
        when(webCrawlerService.crawlPage(testUrl)).thenReturn(mockCrawlResult);
        when(contentParserService.parseContent(mockCrawlResult)).thenReturn(mockParsedContent);
        
        MigrationLog newLog = new MigrationLog(testUrl, testUserId);
        newLog.setId(2L);
        when(migrationLogRepository.save(any(MigrationLog.class))).thenReturn(newLog);
        
        Document savedDocument = new Document();
        savedDocument.setId(1L);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        
        // 执行测试
        DocumentMigrationService.MigrationResult result = migrationService.retryFailedMigration(migrationLogId, testUserId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("迁移成功", result.getMessage());
        assertEquals(1L, result.getDocumentId());
        
        // 验证方法调用
        verify(migrationLogRepository).findById(migrationLogId);
        verify(documentRepository).existsBySourceUrl(testUrl);
        verify(webCrawlerService).crawlPage(testUrl);
        verify(contentParserService).parseContent(mockCrawlResult);
        verify(documentRepository).save(any(Document.class));
    }
    
    @Test
    void testRetryFailedMigration_NotFailedStatus() {
        // 准备测试数据
        Long migrationLogId = 1L;
        MigrationLog completedLog = new MigrationLog(testUrl, testUserId);
        completedLog.setId(migrationLogId);
        completedLog.setStatus(MigrationLog.MigrationStatus.COMPLETED);
        
        when(migrationLogRepository.findById(migrationLogId)).thenReturn(Optional.of(completedLog));
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            migrationService.retryFailedMigration(migrationLogId, testUserId);
        });
        
        assertEquals("只能重试失败的迁移任务", exception.getMessage());
        
        // 验证方法调用
        verify(migrationLogRepository).findById(migrationLogId);
        verify(documentRepository, never()).existsBySourceUrl(any());
    }
    
    @Test
    void testRetryFailedMigration_LogNotFound() {
        // 准备测试数据
        Long migrationLogId = 999L;
        when(migrationLogRepository.findById(migrationLogId)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            migrationService.retryFailedMigration(migrationLogId, testUserId);
        });
        
        assertEquals("迁移日志不存在", exception.getMessage());
        
        // 验证方法调用
        verify(migrationLogRepository).findById(migrationLogId);
    }
}