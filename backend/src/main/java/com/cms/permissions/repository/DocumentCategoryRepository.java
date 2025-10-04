package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {
    
    /**
     * 根据名称查找分类
     */
    Optional<DocumentCategory> findByName(String name);
    
    /**
     * 查找所有激活的分类
     */
    List<DocumentCategory> findByIsActiveTrueOrderBySortOrderAsc();
    
    /**
     * 根据父分类ID查找子分类
     */
    List<DocumentCategory> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(Long parentId);
    
    /**
     * 查找顶级分类（没有父分类）
     */
    List<DocumentCategory> findByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    
    /**
     * 检查分类名称是否已存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM DocumentCategory c WHERE c.name = :name AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);
    
    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据父分类ID查找子分类
     */
    List<DocumentCategory> findByParentId(Long parentId);

    /**
     * 查找所有激活的分类，按排序字段排序
     */
    List<DocumentCategory> findByIsActiveTrueOrderBySortOrder();

    /**
     * 查找顶级分类（父分类为空且激活），按排序字段排序
     */
    List<DocumentCategory> findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();

    /**
     * 根据父分类ID查找激活的子分类，按排序字段排序
     */
    List<DocumentCategory> findByParentIdAndIsActiveTrueOrderBySortOrder(Long parentId);
}