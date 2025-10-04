package com.cms.permissions.cache;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.cms.permissions.controller.CacheMonitorController;
import com.cms.permissions.service.CachePerformanceAnalyzer;
import com.cms.permissions.service.PermissionCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CacheMonitorController.class)
class CacheMonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PermissionCacheService permissionCacheService;

    @MockitoBean
    private CachePerformanceAnalyzer performanceAnalyzer;

    // @Autowired
    // private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup mock data
    }

    @Test
    @WithMockUser(authorities = "CACHE:MONITOR")
    void testGetCacheStats_Success() throws Exception {
        // Given
        PermissionCacheService.CacheStats mockStats =
            new PermissionCacheService.CacheStats();
        when(permissionCacheService.getCacheStats()).thenReturn(mockStats);

        // When & Then
        mockMvc
            .perform(
                get("/api/cache/stats").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void testGetCacheStats_Unauthorized() throws Exception {
        mockMvc
            .perform(get("/api/cache/stats"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    void testGetCacheStats_Forbidden() throws Exception {
        mockMvc
            .perform(get("/api/cache/stats"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "CACHE:MANAGE")
    void testClearUserCache_Success() throws Exception {
        // Given
        doNothing()
            .when(permissionCacheService)
            .evictUserPermissions(anyLong());
        doNothing()
            .when(permissionCacheService)
            .evictUserDocumentAssignments(anyLong());

        // When & Then
        mockMvc
            .perform(
                post("/api/cache/clear/user/123")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(authorities = "CACHE:MONITOR")
    void testGetCacheInfo_Success() throws Exception {
        // When & Then
        mockMvc
            .perform(
                get("/api/cache/info").contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cacheProvider").value("Redis"))
            .andExpect(jsonPath("$.cacheNames").isArray());
    }
}
