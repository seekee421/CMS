package com.cms.permissions.controller;

import com.cms.permissions.config.TestSecurityConfig;
import com.cms.permissions.entity.Document;
import com.cms.permissions.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = DocumentController.class)
@Import(TestSecurityConfig.class)
class DocumentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    @DisplayName("分页获取文档列表需要DOC:VIEW:LIST权限")
    @WithMockUser(username = "user", authorities = {"DOC:VIEW:LIST"})
    void getDocumentsPage_requiresPermission() throws Exception {
        org.springframework.data.domain.Page<Document> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(new Document()));
        when(documentService.searchDocumentsForUser(anyLong(), any(), anyString(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/documents/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("批量操作需要DOC:BATCH权限")
    @WithMockUser(username = "admin", authorities = {"DOC:BATCH"})
    void batchOperate_requiresPermission() throws Exception {
        String body = "{\"operation\":\"DELETE\",\"documentIds\":[1,2,3]}";
        when(documentService.getDocumentsForUser(anyLong())).thenReturn(List.of());

        mockMvc.perform(post("/api/documents/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("导出需要DOC:EXPORT权限")
    @WithMockUser(username = "user", authorities = {"DOC:EXPORT"})
    void export_requiresPermission() throws Exception {
        when(documentService.exportDocuments(anyList())).thenReturn(List.of(new Document()));
        mockMvc.perform(get("/api/documents/export").param("ids", "1,2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("导入需要DOC:IMPORT权限")
    @WithMockUser(username = "user", authorities = {"DOC:IMPORT"})
    void import_requiresPermission() throws Exception {
        when(documentService.importDocuments(anyList(), anyLong())).thenReturn(List.of(new Document()));
        String body = "[{\"title\":\"t\",\"content\":\"c\"}]";
        mockMvc.perform(post("/api/documents/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("无DOC:VIEW:LIST权限访问分页接口返回403")
    @WithMockUser(username = "user")
    void getDocumentsPage_withoutPermission_forbidden() throws Exception {
        mockMvc.perform(get("/api/documents/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("无DOC:BATCH权限执行批量操作返回403")
    @WithMockUser(username = "user")
    void batchOperate_withoutPermission_forbidden() throws Exception {
        String body = "{\"operation\":\"DELETE\",\"documentIds\":[1,2,3]}";
        mockMvc.perform(post("/api/documents/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("无DOC:EXPORT权限访问导出接口返回403")
    @WithMockUser(username = "user")
    void export_withoutPermission_forbidden() throws Exception {
        mockMvc.perform(get("/api/documents/export").param("ids", "1,2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("无DOC:IMPORT权限访问导入接口返回403")
    @WithMockUser(username = "user")
    void import_withoutPermission_forbidden() throws Exception {
        String body = "[{\"title\":\"t\",\"content\":\"c\"}]";
        mockMvc.perform(post("/api/documents/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("批量UPDATE_STATUS操作在有DOC:BATCH权限下返回200")
    @WithMockUser(username = "admin", authorities = {"DOC:BATCH"})
    void batchOperate_updateStatus_ok() throws Exception {
        String body = "{\"operation\":\"UPDATE_STATUS\",\"documentIds\":[10,20],\"status\":\"PUBLISHED\"}";
        mockMvc.perform(post("/api/documents/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}