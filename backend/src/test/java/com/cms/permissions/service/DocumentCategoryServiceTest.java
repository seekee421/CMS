package com.cms.permissions.service;

import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.repository.DocumentCategoryRepository;
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
class DocumentCategoryServiceTest {
    
    @Mock
    private DocumentCategoryRepository categoryRepository;
    
    @InjectMocks
    private DocumentCategoryService categoryService;
    
    private DocumentCategory parentCategory;
    private DocumentCategory testCategory;
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
    void testCreateCategory_Success() {
        // 准备测试数据
        DocumentCategory newCategory = new DocumentCategory("新分类", "新分类描述");
        DocumentCategory savedCategory = new DocumentCategory("新分类", "新分类描述");
        savedCategory.setId(4L);
        savedCategory.setIsActive(true);
        savedCategory.setCreatedAt(LocalDateTime.now());
        savedCategory.setUpdatedAt(LocalDateTime.now());
        
        when(categoryRepository.existsByName("新分类")).thenReturn(false);
        when(categoryRepository.save(any(DocumentCategory.class))).thenReturn(savedCategory);
        
        // 执行测试
        DocumentCategory result = categoryService.createCategory(newCategory.getName(), 
            newCategory.getDescription(), null, 0);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("新分类", result.getName());
        assertEquals("新分类描述", result.getDescription());
        assertTrue(result.getIsActive());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        // 验证方法调用
        verify(categoryRepository).existsByName("新分类");
        verify(categoryRepository).save(any(DocumentCategory.class));
    }
    
    @Test
    void testCreateCategory_DuplicateName() {
        // 准备测试数据
        DocumentCategory newCategory = new DocumentCategory("Java开发", "重复名称");
        
        when(categoryRepository.existsByName("Java开发")).thenReturn(true);
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.createCategory(newCategory.getName(), newCategory.getDescription(), null, 0);
        });
        
        assertEquals("分类名称已存在: Java开发", exception.getMessage());
        
        // 验证方法调用
        verify(categoryRepository).existsByName("Java开发");
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void testUpdateCategory_Success() {
        // 准备测试数据
        DocumentCategory updateData = new DocumentCategory("Java高级开发", "Java高级开发相关文档");
        updateData.setParentId(1L);
        updateData.setSortOrder(2);
        
        DocumentCategory updatedCategory = new DocumentCategory("Java高级开发", "Java高级开发相关文档");
        updatedCategory.setId(2L);
        updatedCategory.setParentId(1L);
        updatedCategory.setSortOrder(2);
        updatedCategory.setIsActive(true);
        updatedCategory.setCreatedAt(testCategory.getCreatedAt());
        updatedCategory.setUpdatedAt(LocalDateTime.now());
        
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("Java高级开发")).thenReturn(false);
        when(categoryRepository.save(any(DocumentCategory.class))).thenReturn(updatedCategory);
        
        // 执行测试
        DocumentCategory result = categoryService.updateCategory(2L, updateData.getName(), 
            updateData.getDescription(), updateData.getParentId(), updateData.getSortOrder(), true);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Java高级开发", result.getName());
        assertEquals("Java高级开发相关文档", result.getDescription());
        assertEquals(1L, result.getParentId());
        assertEquals(2, result.getSortOrder());
        assertTrue(result.getIsActive());
        
        // 验证方法调用
        verify(categoryRepository).findById(2L);
        verify(categoryRepository).existsByName("Java高级开发");
        verify(categoryRepository).save(any(DocumentCategory.class));
    }
    
    @Test
    void testUpdateCategory_NotFound() {
        // 准备测试数据
        DocumentCategory updateData = new DocumentCategory("新名称", "新描述");
        
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategory(999L, updateData.getName(), updateData.getDescription(), 
                updateData.getParentId(), updateData.getSortOrder(), updateData.getIsActive());
        });
        
        assertEquals("分类不存在: 999", exception.getMessage());
        
        // 验证方法调用
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void testUpdateCategory_DuplicateName() {
        // 准备测试数据
        DocumentCategory updateData = new DocumentCategory("技术文档", "重复名称");
        
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName("技术文档")).thenReturn(true);
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.updateCategory(2L, updateData.getName(), updateData.getDescription(), 
                updateData.getParentId(), updateData.getSortOrder(), updateData.getIsActive());
        });
        
        assertEquals("分类名称已存在: 技术文档", exception.getMessage());
        
        // 验证方法调用
        verify(categoryRepository).findById(2L);
        verify(categoryRepository).existsByName("技术文档");
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void testDeleteCategory_Success() {
        // 准备测试数据
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByParentId(2L)).thenReturn(Arrays.asList());
        doNothing().when(categoryRepository).delete(any(DocumentCategory.class));
        
        // 执行测试
        assertDoesNotThrow(() -> categoryService.deleteCategory(2L));
        
        // 验证方法调用
        verify(categoryRepository).findById(2L);
        verify(categoryRepository).findByParentId(2L);
        verify(categoryRepository).delete(testCategory);
    }
    
    @Test
    void testDeleteCategory_NotFound() {
        // 准备测试数据
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategory(999L);
        });
        
        assertEquals("分类不存在: 999", exception.getMessage());
        
        // 验证方法调用
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).delete(any());
    }
    
    @Test
    void testDeleteCategory_HasChildren() {
        // 准备测试数据
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByParentId(2L)).thenReturn(Arrays.asList(childCategory));
        
        // 执行测试并验证异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.deleteCategory(2L);
        });
        
        assertEquals("存在子分类，无法删除", exception.getMessage());
        
        // 验证方法调用
        verify(categoryRepository).findById(2L);
        verify(categoryRepository).findByParentId(2L);
        verify(categoryRepository, never()).deleteById(any());
    }
    
    @Test
    void testGetCategoryById_Success() {
        // 准备测试数据
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        
        // 执行测试
        Optional<DocumentCategory> result = categoryService.getCategoryById(2L);
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testCategory, result.get());
        
        // 验证方法调用
        verify(categoryRepository).findById(2L);
    }
    
    @Test
    void testGetCategoryById_NotFound() {
        // 准备测试数据
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // 执行测试
        Optional<DocumentCategory> result = categoryService.getCategoryById(999L);
        
        // 验证结果
        assertFalse(result.isPresent());
        
        // 验证方法调用
        verify(categoryRepository).findById(999L);
    }
    
    @Test
    void testGetCategoryByName_Success() {
        // 准备测试数据
        when(categoryRepository.findByName("Java开发")).thenReturn(Optional.of(testCategory));
        
        // 执行测试
        Optional<DocumentCategory> result = categoryService.getCategoryByName("Java开发");
        
        // 验证结果
        assertTrue(result.isPresent());
        assertEquals(testCategory, result.get());
        
        // 验证方法调用
        verify(categoryRepository).findByName("Java开发");
    }
    
    @Test
    void testGetAllActiveCategories_Success() {
        // 准备测试数据
        List<DocumentCategory> categories = Arrays.asList(parentCategory, testCategory, childCategory);
        when(categoryRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(categories);
        
        // 执行测试
        List<DocumentCategory> result = categoryService.getActiveCategories();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(parentCategory, result.get(0));
        assertEquals(testCategory, result.get(1));
        assertEquals(childCategory, result.get(2));
        
        // 验证方法调用
        verify(categoryRepository).findByIsActiveTrueOrderBySortOrder();
    }
    
    @Test
    void testGetTopLevelCategories_Success() {
        // 准备测试数据
        List<DocumentCategory> topCategories = Arrays.asList(parentCategory);
        when(categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder()).thenReturn(topCategories);
        
        // 执行测试
        List<DocumentCategory> result = categoryService.getTopLevelCategories();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(parentCategory, result.get(0));
        
        // 验证方法调用
        verify(categoryRepository).findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
    }
    
    @Test
    void testGetChildCategories_Success() {
        // 准备测试数据
        List<DocumentCategory> childCategories = Arrays.asList(testCategory);
        when(categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(1L)).thenReturn(childCategories);
        
        // 执行测试
        List<DocumentCategory> result = categoryService.getChildCategories(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategory, result.get(0));
        
        // 验证方法调用
        verify(categoryRepository).findByParentIdAndIsActiveTrueOrderBySortOrder(1L);
    }
    
    @Test
    void testGetCategoryTree_Success() {
        // 准备测试数据
        List<DocumentCategory> topCategories = Arrays.asList(parentCategory);
        when(categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder()).thenReturn(topCategories);
        when(categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(any())).thenReturn(Arrays.asList());
        
        // 执行测试
        List<DocumentCategoryService.CategoryTreeNode> result = categoryService.getCategoryTree();
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // 验证方法调用
        verify(categoryRepository).findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
    }

    // === 新增：moveCategory 路径与层级批量更新正确性 ===
    @Test
    void testMoveCategory_UpdatePathAndLevel_ForRootParent() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(DocumentCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // 子节点为空，避免批量更新递归产生空指针
        when(categoryRepository.findByParentId(anyLong())).thenReturn(Arrays.asList());
        DocumentCategory result = categoryService.moveCategory(2L, null, 7);
        assertNotNull(result);
        assertEquals(0, result.getLevel());
        assertEquals("/" + result.getName(), result.getPath());
        assertEquals(7, result.getSortOrder());
        verify(categoryRepository).save(any(DocumentCategory.class));
    }

    @Test
    void testMoveCategory_UpdatePathAndLevel_ForChildParent() {
        parentCategory.setLevel(0);
        parentCategory.setPath("/" + parentCategory.getName());
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(DocumentCategory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // 子节点为空，避免批量更新递归产生空指针
        when(categoryRepository.findByParentId(anyLong())).thenReturn(Arrays.asList());
        DocumentCategory result = categoryService.moveCategory(2L, 1L, null);
        assertNotNull(result);
        assertEquals(1, result.getLevel());
        assertEquals(parentCategory.getPath() + "/" + result.getName(), result.getPath());
        verify(categoryRepository).save(any(DocumentCategory.class));
    }

    // === 新增：moveCategory 环引用防护测试 ===
    @Test
    void testMoveCategory_CyclePrevention_ShouldThrow() {
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByParentId(2L)).thenReturn(Arrays.asList(childCategory));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            categoryService.moveCategory(2L, 3L, null);
        });
        assertTrue(ex.getMessage().contains("环引用"));
        verify(categoryRepository, never()).save(any());
    }

    // === 新增：updateSortOrders 批量排序更新测试 ===
    @Test
    void testUpdateSortOrders_BatchUpdate() {
        DocumentCategory c1 = new DocumentCategory("A", ""); c1.setId(10L); c1.setParentId(1L); c1.setSortOrder(1);
        DocumentCategory c2 = new DocumentCategory("B", ""); c2.setId(11L); c2.setParentId(1L); c2.setSortOrder(2);
        when(categoryRepository.findByParentId(1L)).thenReturn(Arrays.asList(c1, c2));
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        List<DocumentCategoryService.SortOrderUpdate> updates = Arrays.asList(
                new DocumentCategoryService.SortOrderUpdate(10L, 3),
                new DocumentCategoryService.SortOrderUpdate(11L, 1)
        );
        List<DocumentCategory> result = categoryService.updateSortOrders(1L, updates);
        assertEquals(2, result.size());
        DocumentCategory rc1 = result.stream().filter(x -> x.getId().equals(10L)).findFirst().orElseThrow();
        DocumentCategory rc2 = result.stream().filter(x -> x.getId().equals(11L)).findFirst().orElseThrow();
        assertEquals(3, rc1.getSortOrder());
        assertEquals(1, rc2.getSortOrder());
        verify(categoryRepository).saveAll(anyList());
    }
}