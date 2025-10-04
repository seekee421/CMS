package com.cms.permissions.controller;

import com.cms.permissions.entity.MigrationLog;
import com.cms.permissions.service.DocumentMigrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentMigrationController.class)
@ActiveProfiles("test")
class DocumentMigrationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DocumentMigrationService migrationService;
    
    private String testUrl;
    private DocumentMigrationService.MigrationResult successResult;
    private DocumentMigrationService.MigrationResult failureResult;
    
    @BeforeEach
    void setUp() {
        testUrl = "https://example.com/test-article";
        successResult = new DocumentMigrationService.MigrationResult(true, "迁移成功", 1L);
        failureResult = new DocumentMigrationService.MigrationResult(false, "迁移失败", null);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testMigrateDocument_Success() throws Exception {
        // 准备测试数据
        when(migrationService.migrateDocument(eq(testUrl), anyLong())).thenReturn(successResult);
        
        DocumentMigrationController.MigrationRequest request = new DocumentMigrationController.MigrationRequest();
        request.setUrl(testUrl);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文档迁移成功"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("迁移成功"))
                .andExpect(jsonPath("$.data.documentId").value(1));
        
        // 验证方法调用
        verify(migrationService).migrateDocument(eq(testUrl), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testMigrateDocument_Failure() throws Exception {
        // 准备测试数据
        when(migrationService.migrateDocument(eq(testUrl), anyLong())).thenReturn(failureResult);
        
        DocumentMigrationController.MigrationRequest request = new DocumentMigrationController.MigrationRequest();
        request.setUrl(testUrl);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文档迁移失败"))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.message").value("迁移失败"));
        
        // 验证方法调用
        verify(migrationService).migrateDocument(eq(testUrl), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testMigrateDocument_InvalidUrl() throws Exception {
        // 准备测试数据 - 空URL
        DocumentMigrationController.MigrationRequest request = new DocumentMigrationController.MigrationRequest();
        request.setUrl("");
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        // 验证方法未被调用
        verify(migrationService, never()).migrateDocument(any(), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testMigrateDocument_InsufficientPermission() throws Exception {
        // 准备测试数据
        DocumentMigrationController.MigrationRequest request = new DocumentMigrationController.MigrationRequest();
        request.setUrl(testUrl);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        // 验证方法未被调用
        verify(migrationService, never()).migrateDocument(any(), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testMigrateBatch_Success() throws Exception {
        // 准备测试数据
        List<String> urls = Arrays.asList(
            "https://example.com/article1",
            "https://example.com/article2"
        );
        
        DocumentMigrationService.BatchMigrationResult batchResult = 
            new DocumentMigrationService.BatchMigrationResult();
        batchResult.addResult(urls.get(0), successResult);
        batchResult.addResult(urls.get(1), successResult);
        
        when(migrationService.migrateBatch(eq(urls), anyLong())).thenReturn(batchResult);
        
        DocumentMigrationController.BatchMigrationRequest request = 
            new DocumentMigrationController.BatchMigrationRequest();
        request.setUrls(urls);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量迁移完成"))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failureCount").value(0));
        
        // 验证方法调用
        verify(migrationService).migrateBatch(eq(urls), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testMigrateBatch_TooManyUrls() throws Exception {
        // 准备测试数据 - 超过50个URL
        List<String> urls = Arrays.asList(new String[51]);
        for (int i = 0; i < 51; i++) {
            urls.set(i, "https://example.com/article" + i);
        }
        
        DocumentMigrationController.BatchMigrationRequest request = 
            new DocumentMigrationController.BatchMigrationRequest();
        request.setUrls(urls);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/migrate/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        // 验证方法未被调用
        verify(migrationService, never()).migrateBatch(any(), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatistics_Success() throws Exception {
        // 准备测试数据
        DocumentMigrationService.MigrationStatistics statistics = 
            new DocumentMigrationService.MigrationStatistics();
        statistics.setPendingCount(5);
        statistics.setInProgressCount(2);
        statistics.setCompletedCount(10);
        statistics.setFailedCount(3);
        
        when(migrationService.getMigrationStatistics()).thenReturn(statistics);
        
        // 执行测试
        mockMvc.perform(get("/api/migration/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取统计信息成功"))
                .andExpect(jsonPath("$.data.pendingCount").value(5))
                .andExpect(jsonPath("$.data.inProgressCount").value(2))
                .andExpect(jsonPath("$.data.completedCount").value(10))
                .andExpect(jsonPath("$.data.failedCount").value(3))
                .andExpect(jsonPath("$.data.totalCount").value(20));
        
        // 验证方法调用
        verify(migrationService).getMigrationStatistics();
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetHistory_Success() throws Exception {
        // 准备测试数据
        List<MigrationLog> history = Arrays.asList(
            createMigrationLog(1L, "https://example.com/article1", MigrationLog.MigrationStatus.COMPLETED),
            createMigrationLog(2L, "https://example.com/article2", MigrationLog.MigrationStatus.FAILED)
        );
        
        when(migrationService.getMigrationHistory(anyLong(), eq(0), eq(20))).thenReturn(history);
        
        // 执行测试
        mockMvc.perform(get("/api/migration/history")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取迁移历史成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        
        // 验证方法调用
        verify(migrationService).getMigrationHistory(anyLong(), eq(0), eq(20));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testRetryMigration_Success() throws Exception {
        // 准备测试数据
        Long migrationLogId = 1L;
        when(migrationService.retryFailedMigration(eq(migrationLogId), anyLong())).thenReturn(successResult);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/retry/{migrationLogId}", migrationLogId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("重试迁移成功"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.documentId").value(1));
        
        // 验证方法调用
        verify(migrationService).retryFailedMigration(eq(migrationLogId), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testRetryMigration_Failure() throws Exception {
        // 准备测试数据
        Long migrationLogId = 1L;
        when(migrationService.retryFailedMigration(eq(migrationLogId), anyLong())).thenReturn(failureResult);
        
        // 执行测试
        mockMvc.perform(post("/api/migration/retry/{migrationLogId}", migrationLogId)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("重试迁移失败"))
                .andExpect(jsonPath("$.data.success").value(false));
        
        // 验证方法调用
        verify(migrationService).retryFailedMigration(eq(migrationLogId), anyLong());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testRetryMigration_Exception() throws Exception {
        // 准备测试数据
        Long migrationLogId = 999L;
        when(migrationService.retryFailedMigration(eq(migrationLogId), anyLong()))
            .thenThrow(new RuntimeException("迁移日志不存在"));
        
        // 执行测试
        mockMvc.perform(post("/api/migration/retry/{migrationLogId}", migrationLogId)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统异常: 迁移日志不存在"));
        
        // 验证方法调用
        verify(migrationService).retryFailedMigration(eq(migrationLogId), anyLong());
    }
    
    /**
     * 创建测试用的迁移日志
     */
    private MigrationLog createMigrationLog(Long id, String sourceUrl, MigrationLog.MigrationStatus status) {
        MigrationLog log = new MigrationLog(sourceUrl, 1L);
        log.setId(id);
        log.setStatus(status);
        log.setStartTime(LocalDateTime.now().minusHours(1));
        log.setEndTime(LocalDateTime.now());
        return log;
    }
}