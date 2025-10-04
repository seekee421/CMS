package com.cms.permissions.service;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.entity.Document.MigrationStatus;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.DocumentCategoryRepository;
import com.cms.permissions.repository.MigrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文档迁移数据库存储测试
 * 专门测试文档和迁移日志在MySQL中的存储功能
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class DocumentMigrationDatabaseTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentCategoryRepository categoryRepository;

    @Autowired
    private MigrationLogRepository migrationLogRepository;

    private DocumentCategory testCategory;

    @BeforeEach
    void setUp() {
        // 创建测试分类
        testCategory = new DocumentCategory();
        testCategory.setName("测试分类");
        testCategory.setDescription("用于测试的文档分类");
        testCategory.setIsActive(true);
        testCategory.setSortOrder(1);
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void testSaveDocumentToDatabase() {
        // 创建测试文档
        Document document = new Document();
        document.setTitle("测试文档标题");
        document.setContent("这是测试文档的内容");
        document.setSourceUrl("https://example.com/test-doc");
        document.setCategory(testCategory.getName());
        document.setTags("[\"测试\",\"文档\",\"迁移\"]");
        document.setVersion("1.0");
        document.setOriginalId("test-doc-001");
        document.setMigrationStatus(MigrationStatus.COMPLETED);
        document.setMigrationDate(LocalDateTime.now());

        // 保存文档到数据库
        Document savedDocument = documentRepository.save(document);

        // 验证文档保存成功
        assertThat(savedDocument.getId()).isNotNull();
        assertThat(savedDocument.getTitle()).isEqualTo("测试文档标题");
        assertThat(savedDocument.getContent()).isEqualTo("这是测试文档的内容");
        assertThat(savedDocument.getSourceUrl()).isEqualTo("https://example.com/test-doc");
        assertThat(savedDocument.getCategory()).isEqualTo(testCategory.getName());
        assertThat(savedDocument.getTags()).isEqualTo("[\"测试\",\"文档\",\"迁移\"]");
        assertThat(savedDocument.getVersion()).isEqualTo("1.0");
        assertThat(savedDocument.getOriginalId()).isEqualTo("test-doc-001");
        assertThat(savedDocument.getMigrationStatus()).isEqualTo(MigrationStatus.COMPLETED);
        assertThat(savedDocument.getMigrationDate()).isNotNull();
    }

    @Test
    void testSaveMigrationLogToDatabase() {
        // 创建迁移日志
        MigrationLog migrationLog = new MigrationLog();
        migrationLog.setSourceUrl("https://example.com/test-doc");
        migrationLog.setStatus(com.cms.permissions.entity.MigrationLog.MigrationStatus.COMPLETED);
        migrationLog.setStartTime(LocalDateTime.now().minusMinutes(5));
        migrationLog.setEndTime(LocalDateTime.now());
        migrationLog.setErrorMessage(null);
        migrationLog.setDocumentId(1L);

        // 保存迁移日志到数据库
        MigrationLog savedLog = migrationLogRepository.save(migrationLog);

        // 验证迁移日志保存成功
        assertThat(savedLog.getId()).isNotNull();
        assertThat(savedLog.getSourceUrl()).isEqualTo("https://example.com/test-doc");
        assertThat(savedLog.getStatus()).isEqualTo(com.cms.permissions.entity.MigrationLog.MigrationStatus.COMPLETED);
        assertThat(savedLog.getStartTime()).isNotNull();
        assertThat(savedLog.getEndTime()).isNotNull();
        assertThat(savedLog.getErrorMessage()).isNull();
        assertThat(savedLog.getDocumentId()).isEqualTo(1L);
    }

    @Test
    void testQueryDocumentsByCategory() {
        // 创建多个测试文档
        Document doc1 = createTestDocument("文档1", "内容1", "https://example.com/doc1");
        Document doc2 = createTestDocument("文档2", "内容2", "https://example.com/doc2");
        Document doc3 = createTestDocument("文档3", "内容3", "https://example.com/doc3");

        documentRepository.saveAll(List.of(doc1, doc2, doc3));

        // 查询指定分类的文档
        List<Document> documents = documentRepository.findByCategory(testCategory.getName());

        // 验证查询结果
        assertThat(documents).hasSize(3);
        assertThat(documents).extracting(Document::getTitle)
                .containsExactlyInAnyOrder("文档1", "文档2", "文档3");
    }

    @Test
    void testQueryMigrationLogsByStatus() {
        // 创建不同状态的迁移日志
        MigrationLog completedLog = createMigrationLog("https://example.com/completed", com.cms.permissions.entity.MigrationLog.MigrationStatus.COMPLETED);
        MigrationLog failedLog = createMigrationLog("https://example.com/failed", com.cms.permissions.entity.MigrationLog.MigrationStatus.FAILED);
        MigrationLog inProgressLog = createMigrationLog("https://example.com/progress", com.cms.permissions.entity.MigrationLog.MigrationStatus.IN_PROGRESS);

        migrationLogRepository.saveAll(List.of(completedLog, failedLog, inProgressLog));

        // 查询已完成的迁移日志
        List<MigrationLog> completedLogs = migrationLogRepository.findByStatusOrderByCreatedAtDesc(com.cms.permissions.entity.MigrationLog.MigrationStatus.COMPLETED);
        assertThat(completedLogs).hasSize(1);
        assertThat(completedLogs.get(0).getSourceUrl()).isEqualTo("https://example.com/completed");

        // 查询失败的迁移日志
        List<MigrationLog> failedLogs = migrationLogRepository.findByStatusOrderByCreatedAtDesc(com.cms.permissions.entity.MigrationLog.MigrationStatus.FAILED);
        assertThat(failedLogs).hasSize(1);
        assertThat(failedLogs.get(0).getSourceUrl()).isEqualTo("https://example.com/failed");
    }

    @Test
    void testDocumentMigrationStatusUpdate() {
        // 创建初始状态为IN_PROGRESS的文档
        Document document = createTestDocument("测试文档", "测试内容", "https://example.com/test");
        document.setMigrationStatus(MigrationStatus.IN_PROGRESS);
        Document savedDocument = documentRepository.save(document);

        // 更新迁移状态为COMPLETED
        savedDocument.setMigrationStatus(MigrationStatus.COMPLETED);
        savedDocument.setMigrationDate(LocalDateTime.now());
        Document updatedDocument = documentRepository.save(savedDocument);

        // 验证状态更新成功
        Optional<Document> retrievedDocument = documentRepository.findById(updatedDocument.getId());
        assertThat(retrievedDocument).isPresent();
        assertThat(retrievedDocument.get().getMigrationStatus()).isEqualTo(MigrationStatus.COMPLETED);
        assertThat(retrievedDocument.get().getMigrationDate()).isNotNull();
    }

    private Document createTestDocument(String title, String content, String sourceUrl) {
        Document document = new Document();
        document.setTitle(title);
        document.setContent(content);
        document.setSourceUrl(sourceUrl);
        document.setCategory(testCategory.getName());
        document.setTags("[\"测试\"]");
        document.setVersion("1.0");
        document.setOriginalId("test-" + System.currentTimeMillis());
        document.setMigrationStatus(MigrationStatus.COMPLETED);
        document.setMigrationDate(LocalDateTime.now());
        return document;
    }

    private MigrationLog createMigrationLog(String sourceUrl, com.cms.permissions.entity.MigrationLog.MigrationStatus status) {
        MigrationLog log = new MigrationLog();
        log.setSourceUrl(sourceUrl);
        log.setStatus(status);
        log.setStartTime(LocalDateTime.now().minusMinutes(5));
        if (status == com.cms.permissions.entity.MigrationLog.MigrationStatus.COMPLETED || 
            status == com.cms.permissions.entity.MigrationLog.MigrationStatus.FAILED) {
            log.setEndTime(LocalDateTime.now());
        }
        if (status == com.cms.permissions.entity.MigrationLog.MigrationStatus.FAILED) {
            log.setErrorMessage("测试错误信息");
        }
        return log;
    }
}