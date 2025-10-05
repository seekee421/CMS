package com.cms.permissions.controller;

import com.cms.permissions.config.TestSecurityConfig;
import com.cms.permissions.entity.DocumentIndex;
import com.cms.permissions.service.SearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = SearchController.class)
@Import(TestSecurityConfig.class)
class SearchControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @Test
    @DisplayName("未授权用户访问全文搜索返回403")
    void fulltext_withoutPermission_403() throws Exception {
        mockMvc.perform(get("/api/search/fulltext").param("q", "dm"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("具有SEARCH:QUERY权限访问全文搜索返回200")
    @WithMockUser(authorities = {"SEARCH:QUERY"})
    void fulltext_withPermission_200() throws Exception {
        Page<DocumentIndex> page = new PageImpl<>(List.of(new DocumentIndex(1L, "t", "c", DocumentIndex.ContentType.FULL_CONTENT)));
        when(searchService.fullTextSearch(anyString(), any(), any(), any(), anyInt(), anyInt(), anyBoolean())).thenReturn(page);
        mockMvc.perform(get("/api/search/fulltext").param("q", "dm").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("搜索建议需要SEARCH:SUGGEST权限")
    @WithMockUser(authorities = {"SEARCH:SUGGEST"})
    void suggest_withPermission_200() throws Exception {
        when(searchService.getSearchSuggestions(eq("dm"), any())).thenReturn(List.of("达梦数据库", "DM SQL"));
        mockMvc.perform(get("/api/search/suggest").param("q", "dm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("达梦数据库"));
    }

    @Test
    @DisplayName("无SEARCH:SUGGEST权限访问建议返回403")
    void suggest_withoutPermission_403() throws Exception {
        mockMvc.perform(get("/api/search/suggest").param("q", "dm"))
                .andExpect(status().isForbidden());
    }
}