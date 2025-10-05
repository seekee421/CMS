package com.cms.permissions.controller;

import com.cms.permissions.TestConfig;
import com.cms.permissions.entity.Role;
import com.cms.permissions.entity.User;
import com.cms.permissions.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
@AutoConfigureMockMvc
class EditorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    private Authentication buildAuthWithRole(String roleName) {
        User user = new User();
        user.setId(100L);
        user.setUsername("tester");
        user.setPassword("nopass");
        Role role = new Role();
        role.setName(roleName);
        user.getRoles().add(role);
        CustomUserDetails cud = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
    }

    @Test
    @DisplayName("未认证用户访问 /api/editor/draft/{id} 应返回 401")
    void getDraft_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/editor/draft/{documentId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无权限角色访问 /api/editor/draft/{id} 应返回 403")
    void getDraft_GuestRole_ShouldReturn403() throws Exception {
        Authentication guestAuth = buildAuthWithRole("ROLE_GUEST");
        mockMvc.perform(get("/api/editor/draft/{documentId}", 1L)
                        .with(authentication(guestAuth)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ROLE_USER 访问 /api/editor/autosave 和 /api/editor/draft/{id} 成功并返回内容与时间戳")
    void autosaveAndGetDraft_UserRole_ShouldReturn200AndContent() throws Exception {
        Authentication userAuth = buildAuthWithRole("ROLE_USER");
        ObjectMapper om = new ObjectMapper();
        Map<String, Object> payload = new HashMap<>();
        payload.put("documentId", 1L);
        payload.put("title", "Security Test Draft");
        payload.put("content", "Hello from security test");
        payload.put("changeLog", "Testing autosave and draft retrieval");
        payload.put("isDraft", true);
        payload.put("autoSave", true);
        payload.put("autoSaveInterval", 5);

        // autosave
        mockMvc.perform(post("/api/editor/autosave")
                        .with(authentication(userAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.savedAt").exists());

        // get draft
        mockMvc.perform(get("/api/editor/draft/{documentId}", 1L)
                        .with(authentication(userAuth)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documentId").value(1))
                .andExpect(jsonPath("$.title").value("Security Test Draft"))
                .andExpect(jsonPath("$.content").value("Hello from security test"))
                .andExpect(jsonPath("$.isDraft").value(true))
                .andExpect(jsonPath("$.lastSaved").exists());
    }
}