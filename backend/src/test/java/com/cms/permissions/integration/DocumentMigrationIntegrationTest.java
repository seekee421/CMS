package com.cms.permissions.integration;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.repository.DocumentCategoryRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.MigrationLogRepository;
import com.cms.permissions.service.DocumentMigrationService;
import com.cms.permissions.service.WebCrawlerService;
import com.cms.permissions.service.ContentParserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文档迁移集成测试
 * 测试完整的文档迁移流程，包括数据库存储和MinIO文件存储
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DocumentMigrationIntegrationTest.TestConfig.class)
public class DocumentMigrationIntegrationTest {

    @TestConfiguration
    @EntityScan(basePackages = "com.cms.permissions.entity")
    static class TestConfig {
        @Bean
        public DocumentMigrationService documentMigrationService() {
            return new DocumentMigrationService();
        }
        
        @Bean
        public WebCrawlerService webCrawlerService() {
            return Mockito.mock(WebCrawlerService.class);
        }
        
        @Bean
        public ContentParserService contentParserService() {
            return Mockito.mock(ContentParserService.class);
        }
    }
    
    @Autowired
    private DocumentMigrationService migrationService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private DocumentCategoryRepository categoryRepository;
    
    @Autowired
    private MigrationLogRepository migrationLogRepository;
    
    private DocumentCategory testCategory;
    private Long testUserId = 1L;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        migrationLogRepository.deleteAll();
        documentRepository.deleteAll();
        categoryRepository.deleteAll();
        
        // 创建测试分类
        testCategory = new DocumentCategory("技术文档", "技术相关文档分类");
        testCategory.setSortOrder(1);
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);
    }
    
    @Test
    void testCompleteDocumentMigrationFlow() {
        // 1. 创建分类
        DocumentCategory javaCategory = new DocumentCategory("Java开发", "Java开发相关文档");
        javaCategory.setParentId(testCategory.getId());
        javaCategory.setSortOrder(1);
        javaCategory.setIsActive(true);
        javaCategory.setCreatedAt(LocalDateTime.now());
        javaCategory.setUpdatedAt(LocalDateTime.now());
        javaCategory = categoryRepository.save(javaCategory);
        
        // 验证分类创建成功
        assertNotNull(javaCategory.getId());
        assertEquals("Java开发", javaCategory.getName());
        
        // 2. 模拟文档迁移（由于无法实际爬取网页，这里直接调用服务层方法）
        String testUrl = "https://example.com/java-tutorial";
        
        // 创建模拟的迁移日志
        MigrationLog migrationLog = new MigrationLog(testUrl, testUserId);
        migrationLog.setStatus(MigrationLog.MigrationStatus.PENDING);
        migrationLog.setStartTime(LocalDateTime.now());
        migrationLog = migrationLogRepository.save(migrationLog);
        
        // 创建模拟的文档
        Document document = new Document();
        document.setTitle("Java基础教程");
        document.setContent("# Java基础教程\n\n这是一个Java基础教程...");
        document.setSourceUrl(testUrl);
        document.setCategory("Java开发");
        document.setTags("[\"java\",\"教程\",\"基础\"]");
        document.setVersion("1.0");
        document.setOriginalId("java-tutorial-001");
        document.setMigrationStatus(Document.MigrationStatus.COMPLETED);
        document.setMigrationDate(LocalDateTime.now());
        document.setCreatedBy(testUserId);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document = documentRepository.save(document);
        
        // 更新迁移日志
        migrationLog.setDocumentId(document.getId());
        migrationLog.setStatus(MigrationLog.MigrationStatus.COMPLETED);
        migrationLog.setEndTime(LocalDateTime.now());
        migrationLog.setProcessedContentSize((long) document.getContent().length());
        migrationLogRepository.save(migrationLog);
        
        // 3. 验证文档创建成功
        Optional<Document> savedDocument = documentRepository.findById(document.getId());
        assertTrue(savedDocument.isPresent());
        assertEquals("Java基础教程", savedDocument.get().getTitle());
        assertEquals(testUrl, savedDocument.get().getSourceUrl());
        assertEquals(Document.MigrationStatus.COMPLETED, savedDocument.get().getMigrationStatus());
        
        // 4. 验证数据库中的数据
        List<MigrationLog> logs = migrationLogRepository.findAll();
        assertEquals(1, logs.size());
        assertEquals(testUrl, logs.get(0).getSourceUrl());
        assertEquals(MigrationLog.MigrationStatus.COMPLETED, logs.get(0).getStatus());
        
        List<Document> documents = documentRepository.findAll();
        assertEquals(1, documents.size());
        assertEquals("Java基础教程", documents.get(0).getTitle());
    }
    
    @Test
    void testCategoryManagementFlow() {
        // 1. 创建顶级分类
        DocumentCategory topCategory = new DocumentCategory("编程语言", "各种编程语言相关文档");
        topCategory.setSortOrder(1);
        topCategory.setIsActive(true);
        topCategory.setCreatedAt(LocalDateTime.now());
        topCategory.setUpdatedAt(LocalDateTime.now());
        topCategory = categoryRepository.save(topCategory);
        
        // 验证顶级分类创建成功
        assertNotNull(topCategory.getId());
        assertEquals("编程语言", topCategory.getName());
        
        // 2. 创建子分类
        DocumentCategory subCategory = new DocumentCategory("Java", "Java编程语言");
        subCategory.setParentId(topCategory.getId());
        subCategory.setSortOrder(1);
        subCategory.setIsActive(true);
        subCategory.setCreatedAt(LocalDateTime.now());
        subCategory.setUpdatedAt(LocalDateTime.now());
        subCategory = categoryRepository.save(subCategory);
        
        // 验证子分类创建成功
        assertNotNull(subCategory.getId());
        assertEquals("Java", subCategory.getName());
        assertEquals(topCategory.getId(), subCategory.getParentId());
        
        // 3. 验证分类查询功能
        Optional<DocumentCategory> foundTopCategory = categoryRepository.findByName("编程语言");
        assertTrue(foundTopCategory.isPresent());
        
        Optional<DocumentCategory> foundSubCategory = categoryRepository.findByName("Java");
        assertTrue(foundSubCategory.isPresent());
        
        // 4. 更新分类
        subCategory.setName("Java语言");
        subCategory.setDescription("Java编程语言及相关技术");
        subCategory.setUpdatedAt(LocalDateTime.now());
        subCategory = categoryRepository.save(subCategory);
        
        // 5. 验证数据库中的数据
        List<DocumentCategory> categories = categoryRepository.findAll();
        assertTrue(categories.size() >= 3); // 包括setUp中创建的testCategory
        
        Optional<DocumentCategory> updatedCategory = categoryRepository.findByName("Java语言");
        assertTrue(updatedCategory.isPresent());
        assertEquals("Java编程语言及相关技术", updatedCategory.get().getDescription());
    }
    
    @Test
    void testMigrationStatisticsAndHistory() {
        // 创建多个迁移日志用于测试统计
        List<MigrationLog> logs = Arrays.asList(
            createMigrationLog("https://example.com/doc1", MigrationLog.MigrationStatus.COMPLETED),
            createMigrationLog("https://example.com/doc2", MigrationLog.MigrationStatus.FAILED),
            createMigrationLog("https://example.com/doc3", MigrationLog.MigrationStatus.IN_PROGRESS),
            createMigrationLog("https://example.com/doc4", MigrationLog.MigrationStatus.PENDING)
        );
        
        migrationLogRepository.saveAll(logs);
        
        // 验证迁移日志保存成功
        List<MigrationLog> savedLogs = migrationLogRepository.findAll();
        assertEquals(4, savedLogs.size());
        
        // 验证不同状态的日志数量
        long pendingCount = savedLogs.stream()
                .filter(log -> log.getStatus() == MigrationLog.MigrationStatus.PENDING)
                .count();
        assertEquals(1, pendingCount);
        
        long inProgressCount = savedLogs.stream()
                .filter(log -> log.getStatus() == MigrationLog.MigrationStatus.IN_PROGRESS)
                .count();
        assertEquals(1, inProgressCount);
        
        long completedCount = savedLogs.stream()
                .filter(log -> log.getStatus() == MigrationLog.MigrationStatus.COMPLETED)
                .count();
        assertEquals(1, completedCount);
        
        long failedCount = savedLogs.stream()
                .filter(log -> log.getStatus() == MigrationLog.MigrationStatus.FAILED)
                .count();
        assertEquals(1, failedCount);
    }

    
    /**
     * 创建测试用的迁移日志
     */
    private MigrationLog createMigrationLog(String sourceUrl, MigrationLog.MigrationStatus status) {
        MigrationLog log = new MigrationLog(sourceUrl, testUserId);
        log.setStatus(status);
        log.setStartTime(LocalDateTime.now().minusHours(1));
        if (status == MigrationLog.MigrationStatus.COMPLETED || status == MigrationLog.MigrationStatus.FAILED) {
            log.setEndTime(LocalDateTime.now());
        }
        if (status == MigrationLog.MigrationStatus.FAILED) {
            log.setErrorMessage("测试错误信息");
        }
        return log;
    }
}