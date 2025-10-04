package com.cms.permissions.controller;

import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.service.DocumentCategoryService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentCategoryController.class)
@ActiveProfiles("test")
class DocumentCategoryControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DocumentCategoryService categoryService;
    
    private DocumentCategory testCategory;
    private DocumentCategory parentCategory;
    private DocumentCategory childCategory;
    
    @BeforeEach
    void setUp() {
        parentCategory = new DocumentCategory("技术文档", "技术相关文档分类");
        parentCategory.setId(1L);
        parentCategory.setSortOrder(1);
        parentCategory.setIsActive(true);
        parentCategory.setCreatedAt(LocalDateTime.now());
        parentCategory.setUpdatedAt(LocalDateTime.now());
        
        testCategory = new DocumentCategory("Java开发", "Java开发相关文档");
        testCategory.setId(2L);
        testCategory.setParentId(1L);
        testCategory.setSortOrder(1);
        testCategory.setIsActive(true);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        
        childCategory = new DocumentCategory("Spring框架", "Spring框架相关文档");
        childCategory.setId(3L);
        childCategory.setParentId(2L);
        childCategory.setSortOrder(1);
        childCategory.setIsActive(true);
        childCategory.setCreatedAt(LocalDateTime.now());
        childCategory.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategory_Success() throws Exception {
        // 准备测试数据
        when(categoryService.createCategory(eq("Java开发"), eq("Java开发相关文档"), eq(1L), eq(1))).thenReturn(testCategory);
        
        DocumentCategoryController.CreateCategoryRequest request = new DocumentCategoryController.CreateCategoryRequest();
        request.setName("Java开发");
        request.setDescription("Java开发相关文档");
        request.setParentId(1L);
        request.setSortOrder(1);
        
        // 执行测试
        mockMvc.perform(post("/api/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("分类创建成功"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("Java开发"))
                .andExpect(jsonPath("$.data.description").value("Java开发相关文档"))
                .andExpect(jsonPath("$.data.parentId").value(1))
                .andExpect(jsonPath("$.data.sortOrder").value(1))
                .andExpect(jsonPath("$.data.isActive").value(true));
        
        // 验证方法调用
        verify(categoryService).createCategory(eq("Java开发"), eq("Java开发相关文档"), eq(1L), eq(1));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategory_InvalidName() throws Exception {
        // 准备测试数据 - 空名称
        DocumentCategoryController.CreateCategoryRequest request = new DocumentCategoryController.CreateCategoryRequest();
        request.setName("");
        request.setDescription("测试描述");
        
        // 执行测试
        mockMvc.perform(post("/api/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        
        // 验证方法未被调用
        verify(categoryService, never()).createCategory(any(), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testCreateCategory_InsufficientPermission() throws Exception {
        // 准备测试数据
        DocumentCategoryController.CreateCategoryRequest request = new DocumentCategoryController.CreateCategoryRequest();
        request.setName("测试分类");
        request.setDescription("测试描述");
        
        // 执行测试
        mockMvc.perform(post("/api/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
        
        // 验证方法未被调用
        verify(categoryService, never()).createCategory(any(), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_Success() throws Exception {
        // 准备测试数据
        DocumentCategory updatedCategory = new DocumentCategory("Java高级开发", "Java高级开发相关文档");
        updatedCategory.setId(2L);
        updatedCategory.setParentId(1L);
        updatedCategory.setSortOrder(2);
        updatedCategory.setIsActive(true);
        
        when(categoryService.updateCategory(eq(2L), eq("Java高级开发"), eq("Java高级开发相关文档"), eq(1L), eq(2), eq(true))).thenReturn(updatedCategory);
        
        DocumentCategoryController.UpdateCategoryRequest request = new DocumentCategoryController.UpdateCategoryRequest();
        request.setName("Java高级开发");
        request.setDescription("Java高级开发相关文档");
        request.setParentId(1L);
        request.setSortOrder(2);
        
        // 执行测试
        mockMvc.perform(put("/api/categories/{id}", 2L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("分类更新成功"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("Java高级开发"))
                .andExpect(jsonPath("$.data.description").value("Java高级开发相关文档"))
                .andExpect(jsonPath("$.data.sortOrder").value(2));
        
        // 验证方法调用
        verify(categoryService).updateCategory(eq(2L), eq("Java高级开发"), eq("Java高级开发相关文档"), eq(1L), eq(2), eq(true));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCategory_NotFound() throws Exception {
        // 准备测试数据
        when(categoryService.updateCategory(eq(999L), eq("测试分类"), eq("测试描述"), any(), any(), any()))
            .thenThrow(new RuntimeException("分类不存在"));
        
        DocumentCategoryController.UpdateCategoryRequest request = new DocumentCategoryController.UpdateCategoryRequest();
        request.setName("测试分类");
        request.setDescription("测试描述");
        
        // 执行测试
        mockMvc.perform(put("/api/categories/{id}", 999L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统异常: 分类不存在"));
        
        // 验证方法调用
        verify(categoryService).updateCategory(eq(999L), eq("测试分类"), eq("测试描述"), any(), any(), any());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCategory_Success() throws Exception {
        // 准备测试数据
        doNothing().when(categoryService).deleteCategory(2L);
        
        // 执行测试
        mockMvc.perform(delete("/api/categories/{id}", 2L)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("分类删除成功"));
        
        // 验证方法调用
        verify(categoryService).deleteCategory(2L);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCategory_NotFound() throws Exception {
        // 准备测试数据
        doThrow(new RuntimeException("分类不存在")).when(categoryService).deleteCategory(999L);
        
        // 执行测试
        mockMvc.perform(delete("/api/categories/{id}", 999L)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("系统异常: 分类不存在"));
        
        // 验证方法调用
        verify(categoryService).deleteCategory(999L);
    }
    
    @Test
    @WithMockUser
    void testGetCategoryById_Success() throws Exception {
        // 准备测试数据
        when(categoryService.getCategoryById(2L)).thenReturn(Optional.of(testCategory));
        
        // 执行测试
        mockMvc.perform(get("/api/categories/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取分类成功"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("Java开发"))
                .andExpect(jsonPath("$.data.description").value("Java开发相关文档"));
        
        // 验证方法调用
        verify(categoryService).getCategoryById(2L);
    }
    
    @Test
    @WithMockUser
    void testGetCategoryById_NotFound() throws Exception {
        // 准备测试数据
        when(categoryService.getCategoryById(999L)).thenReturn(Optional.empty());
        
        // 执行测试
        mockMvc.perform(get("/api/categories/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("分类不存在"));
        
        // 验证方法调用
        verify(categoryService).getCategoryById(999L);
    }
    
    @Test
    @WithMockUser
    void testGetAllActiveCategories_Success() throws Exception {
        // 准备测试数据
        List<DocumentCategory> categories = Arrays.asList(parentCategory, testCategory, childCategory);
        when(categoryService.getActiveCategories()).thenReturn(categories);
        
        // 执行测试
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取分类列表成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("技术文档"))
                .andExpect(jsonPath("$.data[1].name").value("Java开发"))
                .andExpect(jsonPath("$.data[2].name").value("Spring框架"));
        
        // 验证方法调用
        verify(categoryService).getActiveCategories();
    }
    
    @Test
    @WithMockUser
    void testGetCategoryTree_Success() throws Exception {
        // 准备测试数据
        DocumentCategoryService.CategoryTreeNode node1 = new DocumentCategoryService.CategoryTreeNode(parentCategory);
        DocumentCategoryService.CategoryTreeNode node2 = new DocumentCategoryService.CategoryTreeNode(testCategory);
        
        List<DocumentCategoryService.CategoryTreeNode> tree = Arrays.asList(node1, node2);
        when(categoryService.getCategoryTree()).thenReturn(tree);
        
        // 执行测试
        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取分类树成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        
        // 验证方法调用
        verify(categoryService).getCategoryTree();
    }
    
    @Test
    @WithMockUser
    void testGetTopLevelCategories_Success() throws Exception {
        // 准备测试数据
        List<DocumentCategory> topCategories = Arrays.asList(parentCategory);
        when(categoryService.getTopLevelCategories()).thenReturn(topCategories);
        
        // 执行测试
        mockMvc.perform(get("/api/categories/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取顶级分类成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("技术文档"));
        
        // 验证方法调用
        verify(categoryService).getTopLevelCategories();
    }
    
    @Test
    @WithMockUser
    void testGetChildCategories_Success() throws Exception {
        // 准备测试数据
        List<DocumentCategory> childCategories = Arrays.asList(testCategory);
        when(categoryService.getChildCategories(1L)).thenReturn(childCategories);
        
        // 执行测试
        mockMvc.perform(get("/api/categories/{parentId}/children", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取子分类成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Java开发"));
        
        // 验证方法调用
        verify(categoryService).getChildCategories(1L);
    }
    
    @Test
    @WithMockUser
    void testGetChildCategories_EmptyResult() throws Exception {
        // 准备测试数据
        when(categoryService.getChildCategories(999L)).thenReturn(Arrays.asList());
        
        // 执行测试
        mockMvc.perform(get("/api/categories/{parentId}/children", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取子分类成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
        
        // 验证方法调用
        verify(categoryService).getChildCategories(999L);
    }
}