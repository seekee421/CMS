package com.cms.permissions.controller;

import com.cms.permissions.config.TestSecurityConfig;
import com.cms.permissions.entity.DocumentFeedback;
import com.cms.permissions.entity.Permission;
import com.cms.permissions.entity.Role;
import com.cms.permissions.entity.User;
import com.cms.permissions.security.CustomUserDetails;
import com.cms.permissions.service.DocumentFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = FeedbackController.class)
@Import(TestSecurityConfig.class)
class FeedbackControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentFeedbackService feedbackService;

    private Authentication buildAuthWithPermissions(Long userId, String roleName, String... permissionCodes) {
        User user = new User();
        user.setId(userId);
        user.setUsername("tester" + userId);
        user.setPassword("nopass");
        Role role = new Role();
        role.setName(roleName);
        for (String code : permissionCodes) {
            Permission p = new Permission();
            p.setCode(code);
            role.getPermissions().add(p);
        }
        user.getRoles().add(role);
        CustomUserDetails cud = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
    }

    @Test
    @DisplayName("未认证用户提交反馈返回401")
    void submitFeedback_Unauthenticated_401() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(new SubmitPayload(1L, DocumentFeedback.FeedbackType.CONTENT_MISSING, "desc", "contact"));
        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无FEEDBACK:SUBMIT权限提交反馈返回403")
    void submitFeedback_WithoutPermission_403() throws Exception {
        Authentication auth = buildAuthWithPermissions(101L, "ROLE_USER"); // no permissions
        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(new SubmitPayload(2L, DocumentFeedback.FeedbackType.DESCRIPTION_UNCLEAR, "desc", "contact"));
        mockMvc.perform(post("/api/feedback")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("具有FEEDBACK:SUBMIT权限提交反馈返回200并包含反馈ID")
    void submitFeedback_WithPermission_200() throws Exception {
        Authentication auth = buildAuthWithPermissions(102L, "ROLE_USER", "FEEDBACK:SUBMIT");
        DocumentFeedback saved = new DocumentFeedback(2L, 102L, DocumentFeedback.FeedbackType.CONTENT_INCORRECT, "desc", "contact");
        saved.setId(999L);
        saved.setCreatedAt(LocalDateTime.now());
        when(feedbackService.submitFeedback(anyLong(), anyLong(), any(), any(), any())).thenReturn(saved);

        ObjectMapper om = new ObjectMapper();
        String body = om.writeValueAsString(new SubmitPayload(2L, DocumentFeedback.FeedbackType.CONTENT_INCORRECT, "desc", "contact"));
        mockMvc.perform(post("/api/feedback")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(999))
                .andExpect(jsonPath("$.documentId").value(2))
                .andExpect(jsonPath("$.userId").value(102));
    }

    @Test
    @DisplayName("具有FEEDBACK:READ权限查看文档反馈返回200")
    void listByDocument_WithPermission_200() throws Exception {
        DocumentFeedback f = new DocumentFeedback(3L, 200L, DocumentFeedback.FeedbackType.OTHER_SUGGESTION, "desc", "contact");
        f.setId(1L);
        when(feedbackService.getFeedbacksForDocument(eq(3L))).thenReturn(List.of(f));
        mockMvc.perform(get("/api/feedback/document/{documentId}", 3L)
                        .with(authentication(buildAuthWithPermissions(200L, "ROLE_USER", "FEEDBACK:READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].documentId").value(3));
    }

    @Test
    @DisplayName("无FEEDBACK:READ权限查看文档反馈返回403")
    void listByDocument_WithoutPermission_403() throws Exception {
        mockMvc.perform(get("/api/feedback/document/{documentId}", 3L)
                        .with(authentication(buildAuthWithPermissions(200L, "ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("具有FEEDBACK:READ权限查看用户反馈返回200")
    void listByUser_WithPermission_200() throws Exception {
        DocumentFeedback f = new DocumentFeedback(5L, 300L, DocumentFeedback.FeedbackType.CONTENT_MISSING, "desc", "contact");
        f.setId(2L);
        when(feedbackService.getFeedbacksByUser(eq(300L))).thenReturn(List.of(f));
        mockMvc.perform(get("/api/feedback/user/{userId}", 300L)
                        .with(authentication(buildAuthWithPermissions(300L, "ROLE_USER", "FEEDBACK:READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].userId").value(300));
    }

    @Test
    @DisplayName("处理反馈需要FEEDBACK:PROCESS权限，授权后返回200并标记processed")
    void processFeedback_WithPermission_200() throws Exception {
        Authentication auth = buildAuthWithPermissions(400L, "ROLE_ADMIN", "FEEDBACK:PROCESS");
        DocumentFeedback processed = new DocumentFeedback(7L, 500L, DocumentFeedback.FeedbackType.CONTENT_INCORRECT, "desc", "contact");
        processed.setId(7L);
        processed.setProcessed(true);
        processed.setProcessedAt(LocalDateTime.now());
        processed.setProcessedBy(400L);
        when(feedbackService.processFeedback(eq(7L), eq(400L))).thenReturn(processed);

        mockMvc.perform(post("/api/feedback/process/{id}", 7L)
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.processed").value(true))
                .andExpect(jsonPath("$.processedBy").value(400));
    }

    @Test
    @DisplayName("无FEEDBACK:PROCESS权限处理反馈返回403")
    void processFeedback_WithoutPermission_403() throws Exception {
        mockMvc.perform(post("/api/feedback/process/{id}", 7L)
                        .with(authentication(buildAuthWithPermissions(401L, "ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    // 辅助payload类，仅用于构造请求体
    static class SubmitPayload {
        public Long documentId;
        public DocumentFeedback.FeedbackType feedbackType;
        public String description;
        public String contactInfo;
        SubmitPayload(Long d, DocumentFeedback.FeedbackType t, String desc, String contact) {
            this.documentId = d; this.feedbackType = t; this.description = desc; this.contactInfo = contact;
        }
    }
}