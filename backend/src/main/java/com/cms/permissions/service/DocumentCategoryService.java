package com.cms.permissions.service;

import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.repository.DocumentCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentCategoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentCategoryService.class);
    
    @Autowired
    private DocumentCategoryRepository categoryRepository;
    
    /**
     * 创建分类
     */
    @Transactional
    public DocumentCategory createCategory(String name, String description, Long parentId, Integer sortOrder) {
        logger.info("创建文档分类: {}", name);
        
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在: " + name);
        }
        
        DocumentCategory category = new DocumentCategory();
        category.setName(name);
        category.setDescription(description);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public DocumentCategory updateCategory(Long id, String name, String description, 
                                         Long parentId, Integer sortOrder, Boolean isActive) {
        logger.info("更新文档分类: {}", id);
        
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        
        // 检查名称是否与其他分类冲突
        if (name != null && !name.equals(category.getName()) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类名称已存在: " + name);
        }
        
        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (parentId != null) category.setParentId(parentId);
        if (sortOrder != null) category.setSortOrder(sortOrder);
        if (isActive != null) category.setIsActive(isActive);
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        logger.info("删除文档分类: {}", id);
        
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        
        // 检查是否有子分类
        List<DocumentCategory> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("存在子分类，无法删除");
        }
        
        categoryRepository.delete(category);
    }
    
    /**
     * 获取分类详情
     */
    public Optional<DocumentCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    /**
     * 根据名称查找分类
     */
    public Optional<DocumentCategory> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    /**
     * 获取所有活跃分类
     */
    public List<DocumentCategory> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrder();
    }
    
    /**
     * 获取顶级分类
     */
    public List<DocumentCategory> getTopLevelCategories() {
        return categoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
    }
    
    /**
     * 获取子分类
     */
    public List<DocumentCategory> getChildCategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(parentId);
    }
    
    /**
     * 获取分类树结构
     */
    public List<CategoryTreeNode> getCategoryTree() {
        List<DocumentCategory> topCategories = getTopLevelCategories();
        return buildCategoryTree(topCategories);
    }
    
    /**
     * 构建分类树
     */
    private List<CategoryTreeNode> buildCategoryTree(List<DocumentCategory> categories) {
        return categories.stream()
                .map(category -> {
                    CategoryTreeNode node = new CategoryTreeNode(category);
                    List<DocumentCategory> children = getChildCategories(category.getId());
                    if (!children.isEmpty()) {
                        node.setChildren(buildCategoryTree(children));
                    }
                    return node;
                })
                .toList();
    }
    
    /**
     * 分类树节点
     */
    public static class CategoryTreeNode {
        private DocumentCategory category;
        private List<CategoryTreeNode> children;
        
        public CategoryTreeNode(DocumentCategory category) {
            this.category = category;
        }
        
        public DocumentCategory getCategory() { return category; }
        public void setCategory(DocumentCategory category) { this.category = category; }
        
        public List<CategoryTreeNode> getChildren() { return children; }
        public void setChildren(List<CategoryTreeNode> children) { this.children = children; }
    }
}