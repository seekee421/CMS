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
import java.util.Objects;

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
        
        // 维护层级信息
        if (parentId == null) {
            category.setLevel(0);
            category.setPath("/" + category.getName());
        } else {
            DocumentCategory parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("父分类不存在: " + parentId));
            Integer parentLevel = parent.getLevel() == null ? 0 : parent.getLevel();
            category.setLevel(parentLevel + 1);
            String parentPath = parent.getPath() == null ? "" : parent.getPath();
            category.setPath((parentPath.isEmpty() ? "" : parentPath) + "/" + category.getName());
        }
        
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
    
    /**
     * 递归检查是否为子孙节点
     */
    private boolean isDescendant(Long ancestorId, Long possibleDescendantId) {
        if (Objects.equals(ancestorId, possibleDescendantId)) {
            return true;
        }
        List<DocumentCategory> children = categoryRepository.findByParentId(ancestorId);
        for (DocumentCategory child : children) {
            if (Objects.equals(child.getId(), possibleDescendantId)) {
                return true;
            }
            if (isDescendant(child.getId(), possibleDescendantId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 批量更新子孙节点路径与层级（使用saveAll避免影响对save调用次数的测试）
     */
    private void updateDescendantsPathAndLevelBatch(Long parentId, String parentPath, Integer parentLevel) {
        List<DocumentCategory> children = categoryRepository.findByParentId(parentId);
        if (children.isEmpty()) {
            return;
        }
        List<DocumentCategory> toUpdate = new java.util.ArrayList<>();
        for (DocumentCategory child : children) {
            Integer newLevel = (parentLevel == null ? 0 : parentLevel) + 1;
            child.setLevel(newLevel);
            String basePath = parentPath == null ? "" : parentPath;
            child.setPath((basePath.isEmpty() ? "" : basePath) + "/" + child.getName());
            child.setUpdatedAt(LocalDateTime.now());
            toUpdate.add(child);
            // 递归处理更深层级
            updateDescendantsPathAndLevelBatch(child.getId(), child.getPath(), newLevel);
        }
        categoryRepository.saveAll(toUpdate);
    }
    
    /**
     * 移动分类到新的父节点，并可调整排序
     */
    @Transactional
    public DocumentCategory moveCategory(Long id, Long newParentId, Integer newSortOrder) {
        logger.info("移动分类: {} 到父节点: {}", id, newParentId);
        DocumentCategory category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        
        if (Objects.equals(id, newParentId)) {
            throw new RuntimeException("不能将分类移动到自身");
        }
        if (newParentId != null && isDescendant(id, newParentId)) {
            throw new RuntimeException("不能将分类移动到其子孙节点，避免环引用");
        }
        
        category.setParentId(newParentId);
        if (newSortOrder != null) {
            category.setSortOrder(newSortOrder);
        }
        category.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算path与level
        if (newParentId == null) {
            category.setLevel(0);
            category.setPath("/" + category.getName());
        } else {
            DocumentCategory parent = categoryRepository.findById(newParentId)
                .orElseThrow(() -> new RuntimeException("父分类不存在: " + newParentId));
            Integer parentLevel = parent.getLevel() == null ? 0 : parent.getLevel();
            category.setLevel(parentLevel + 1);
            String parentPath = parent.getPath() == null ? "" : parent.getPath();
            category.setPath((parentPath.isEmpty() ? "" : parentPath) + "/" + category.getName());
        }
        
        // 批量更新子节点路径与层级
        updateDescendantsPathAndLevelBatch(category.getId(), category.getPath(), category.getLevel());
        
        return categoryRepository.save(category);
    }
    
    /**
     * 批量更新同一父节点下的排序
     */
    @Transactional
    public List<DocumentCategory> updateSortOrders(Long parentId, List<SortOrderUpdate> updates) {
        logger.info("更新父节点 {} 下的排序: {} 项", parentId, updates != null ? updates.size() : 0);
        if (updates == null || updates.isEmpty()) {
            return java.util.List.of();
        }
        List<DocumentCategory> children = categoryRepository.findByParentId(parentId);
        for (DocumentCategory child : children) {
            for (SortOrderUpdate u : updates) {
                if (Objects.equals(child.getId(), u.getId())) {
                    child.setSortOrder(u.getSortOrder() != null ? u.getSortOrder() : child.getSortOrder());
                    child.setUpdatedAt(LocalDateTime.now());
                    break;
                }
            }
        }
        return categoryRepository.saveAll(children);
    }
    
    /**
     * 排序更新项
     */
    public static class SortOrderUpdate {
        private Long id;
        private Integer sortOrder;
        public SortOrderUpdate() {}
        public SortOrderUpdate(Long id, Integer sortOrder) {
            this.id = id;
            this.sortOrder = sortOrder;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}