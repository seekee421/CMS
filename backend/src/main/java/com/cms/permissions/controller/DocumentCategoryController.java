package com.cms.permissions.controller;

import com.cms.permissions.entity.DocumentCategory;
import com.cms.permissions.service.DocumentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "文档分类", description = "文档分类管理API")
public class DocumentCategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentCategoryController.class);
    
    @Autowired
    private DocumentCategoryService categoryService;
    
    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的文档分类")
    @PreAuthorize("hasAuthority('CAT:CREATE') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentCategory>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        
        logger.info("创建分类请求: {}", request.getName());
        
        try {
            DocumentCategory category = categoryService.createCategory(
                request.getName(),
                request.getDescription(),
                request.getParentId(),
                request.getSortOrder()
            );
            
            return ResponseEntity.ok(ApiResponse.success(category, "分类创建成功"));
            
        } catch (Exception e) {
            logger.error("创建分类失败: {}", request.getName(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "创建分类失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新指定的文档分类")
    @PreAuthorize("hasAuthority('CAT:UPDATE') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentCategory>> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        logger.info("更新分类请求: {}", id);
        
        try {
            DocumentCategory category = categoryService.updateCategory(
                id,
                request.getName(),
                request.getDescription(),
                request.getParentId(),
                request.getSortOrder(),
                request.getIsActive()
            );
            
            return ResponseEntity.ok(ApiResponse.success(category, "分类更新成功"));
            
        } catch (Exception e) {
            logger.error("更新分类失败: {}", id, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "更新分类失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除指定的文档分类")
    @PreAuthorize("hasAuthority('CAT:DELETE') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        logger.info("删除分类请求: {}", id);
        
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success(null, "分类删除成功"));
            
        } catch (Exception e) {
            logger.error("删除分类失败: {}", id, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "删除分类失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取分类详细信息")
    public ResponseEntity<ApiResponse<DocumentCategory>> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        try {
            return categoryService.getCategoryById(id)
                .map(category -> ResponseEntity.ok(ApiResponse.success(category, "获取分类成功")))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            logger.error("获取分类详情失败: {}", id, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有活跃分类
     */
    @GetMapping
    @Operation(summary = "获取所有分类", description = "获取所有活跃的文档分类")
    public ResponseEntity<ApiResponse<List<DocumentCategory>>> getActiveCategories() {
        
        try {
            List<DocumentCategory> categories = categoryService.getActiveCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "获取分类列表成功"));
            
        } catch (Exception e) {
            logger.error("获取分类列表失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树", description = "获取层级结构的分类树")
    public ResponseEntity<ApiResponse<List<DocumentCategoryService.CategoryTreeNode>>> getCategoryTree() {
        
        try {
            List<DocumentCategoryService.CategoryTreeNode> tree = categoryService.getCategoryTree();
            return ResponseEntity.ok(ApiResponse.success(tree, "获取分类树成功"));
            
        } catch (Exception e) {
            logger.error("获取分类树失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取顶级分类
     */
    @GetMapping("/top-level")
    @Operation(summary = "获取顶级分类", description = "获取所有顶级分类")
    public ResponseEntity<ApiResponse<List<DocumentCategory>>> getTopLevelCategories() {
        
        try {
            List<DocumentCategory> categories = categoryService.getTopLevelCategories();
            return ResponseEntity.ok(ApiResponse.success(categories, "获取顶级分类成功"));
            
        } catch (Exception e) {
            logger.error("获取顶级分类失败", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 获取子分类
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子分类", description = "获取指定分类的所有子分类")
    public ResponseEntity<ApiResponse<List<DocumentCategory>>> getChildCategories(
            @Parameter(description = "父分类ID") @PathVariable Long parentId) {
        
        try {
            List<DocumentCategory> categories = categoryService.getChildCategories(parentId);
            return ResponseEntity.ok(ApiResponse.success(categories, "获取子分类成功"));
            
        } catch (Exception e) {
            logger.error("获取子分类失败: {}", parentId, e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(null, "系统异常: " + e.getMessage()));
        }
    }
    
    /**
     * 移动分类
     */
    @PostMapping("/{id}/move")
    @Operation(summary = "移动分类", description = "将分类移动到新的父节点并可调整排序")
    @PreAuthorize("hasAuthority('CAT:MOVE') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DocumentCategory>> moveCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody MoveCategoryRequest request) {
        try {
            DocumentCategory updated = categoryService.moveCategory(id, request.getNewParentId(), request.getNewSortOrder());
            return ResponseEntity.ok(ApiResponse.success(updated, "分类移动成功"));
        } catch (Exception e) {
            logger.error("移动分类失败: {} -> {}", id, request.getNewParentId(), e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "移动分类失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量更新排序
     */
    @PutMapping("/{parentId}/sort")
    @Operation(summary = "批量排序", description = "批量更新同一父节点下的分类排序")
    @PreAuthorize("hasAuthority('CAT:SORT') OR hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DocumentCategory>>> updateSortOrders(
            @Parameter(description = "父分类ID") @PathVariable Long parentId,
            @Valid @RequestBody List<SortOrderUpdateRequest> updatesRequest) {
        try {
            List<DocumentCategoryService.SortOrderUpdate> updates = new java.util.ArrayList<>();
            for (SortOrderUpdateRequest r : updatesRequest) {
                updates.add(new DocumentCategoryService.SortOrderUpdate(r.getId(), r.getSortOrder()));
            }
            List<DocumentCategory> result = categoryService.updateSortOrders(parentId, updates);
            return ResponseEntity.ok(ApiResponse.success(result, "排序更新成功"));
        } catch (Exception e) {
            logger.error("更新排序失败: parent={}", parentId, e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(null, "更新排序失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建分类请求DTO
     */
    public static class CreateCategoryRequest {
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 100, message = "分类名称长度不能超过100字符")
        private String name;
        
        @Size(max = 500, message = "分类描述长度不能超过500字符")
        private String description;
        
        private Long parentId;
        private Integer sortOrder;
        
        // Getter和Setter方法
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
    
    /**
     * 更新分类请求DTO
     */
    public static class UpdateCategoryRequest {
        @Size(max = 100, message = "分类名称长度不能超过100字符")
        private String name;
        
        @Size(max = 500, message = "分类描述长度不能超过500字符")
        private String description;
        
        private Long parentId;
        private Integer sortOrder;
        private Boolean isActive;
        
        // Getter和Setter方法
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    /**
     * 通用API响应包装类
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        private ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, message, data);
        }
        
        public static <T> ApiResponse<T> error(T data, String message) {
            return new ApiResponse<>(false, message, data);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
    
    /** 移动分类请求DTO */
    public static class MoveCategoryRequest {
        private Long newParentId;
        private Integer newSortOrder;
        public Long getNewParentId() { return newParentId; }
        public void setNewParentId(Long newParentId) { this.newParentId = newParentId; }
        public Integer getNewSortOrder() { return newSortOrder; }
        public void setNewSortOrder(Integer newSortOrder) { this.newSortOrder = newSortOrder; }
    }

    /** 排序更新请求DTO */
    public static class SortOrderUpdateRequest {
        private Long id;
        private Integer sortOrder;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}