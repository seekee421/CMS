package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文档索引Repository
 * 提供全文搜索相关的数据访问方法
 */
@Repository
public interface DocumentIndexRepository extends JpaRepository<DocumentIndex, Long> {
    
    /**
     * 根据文档ID查找所有索引
     */
    List<DocumentIndex> findByDocumentIdAndIsActiveTrue(Long documentId);
    
    /**
     * 根据文档ID和内容类型查找索引
     */
    List<DocumentIndex> findByDocumentIdAndContentTypeAndIsActiveTrue(
        Long documentId, 
        DocumentIndex.ContentType contentType
    );
    
    /**
     * 全文搜索 - MySQL FULLTEXT
     */
    @Query(value = """
        SELECT di.* FROM document_index di 
        WHERE di.is_active = true 
        AND (:isPublicOnly = false OR di.is_public = true)
        AND (:language IS NULL OR di.language = :language)
        AND (:contentType IS NULL OR di.content_type = :contentType)
        AND MATCH(di.title, di.content, di.plain_text) AGAINST(:query IN NATURAL LANGUAGE MODE)
        ORDER BY MATCH(di.title, di.content, di.plain_text) AGAINST(:query IN NATURAL LANGUAGE MODE) DESC
        """, nativeQuery = true)
    Page<DocumentIndex> searchFullText(
        @Param("query") String query,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        @Param("contentType") String contentType,
        Pageable pageable
    );
    
    /**
     * 布尔模式全文搜索
     */
    @Query(value = """
        SELECT di.* FROM document_index di 
        WHERE di.is_active = true 
        AND (:isPublicOnly = false OR di.is_public = true)
        AND (:language IS NULL OR di.language = :language)
        AND MATCH(di.title, di.content, di.plain_text) AGAINST(:query IN BOOLEAN MODE)
        ORDER BY MATCH(di.title, di.content, di.plain_text) AGAINST(:query IN BOOLEAN MODE) DESC
        """, nativeQuery = true)
    Page<DocumentIndex> searchBooleanMode(
        @Param("query") String query,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 标题搜索
     */
    @Query("SELECT di FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND (:language IS NULL OR di.language = :language) " +
           "AND LOWER(di.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY di.searchWeight DESC, di.createdAt DESC")
    Page<DocumentIndex> searchByTitle(
        @Param("query") String query,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 内容搜索
     */
    @Query("SELECT di FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND (:language IS NULL OR di.language = :language) " +
           "AND (LOWER(di.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(di.plainText) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY di.searchWeight DESC, di.createdAt DESC")
    Page<DocumentIndex> searchByContent(
        @Param("query") String query,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 标签搜索
     */
    @Query("SELECT di FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND (:language IS NULL OR di.language = :language) " +
           "AND di.tags IS NOT NULL " +
           "AND LOWER(di.tags) LIKE LOWER(CONCAT('%', :tag, '%')) " +
           "ORDER BY di.searchWeight DESC, di.createdAt DESC")
    Page<DocumentIndex> searchByTag(
        @Param("tag") String tag,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 分类路径搜索
     */
    @Query("SELECT di FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND (:language IS NULL OR di.language = :language) " +
           "AND di.categoryPath IS NOT NULL " +
           "AND LOWER(di.categoryPath) LIKE LOWER(CONCAT('%', :categoryPath, '%')) " +
           "ORDER BY di.searchWeight DESC, di.createdAt DESC")
    Page<DocumentIndex> searchByCategory(
        @Param("categoryPath") String categoryPath,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 高级搜索 - 组合条件
     */
    @Query("SELECT di FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND (:language IS NULL OR di.language = :language) " +
           "AND (:title IS NULL OR LOWER(di.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "AND (:content IS NULL OR LOWER(di.plainText) LIKE LOWER(CONCAT('%', :content, '%'))) " +
           "AND (:tags IS NULL OR LOWER(di.tags) LIKE LOWER(CONCAT('%', :tags, '%'))) " +
           "AND (:categoryPath IS NULL OR LOWER(di.categoryPath) LIKE LOWER(CONCAT('%', :categoryPath, '%'))) " +
           "AND (:contentType IS NULL OR di.contentType = :contentType) " +
           "AND (:startDate IS NULL OR di.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR di.createdAt <= :endDate) " +
           "ORDER BY di.searchWeight DESC, di.createdAt DESC")
    Page<DocumentIndex> advancedSearch(
        @Param("title") String title,
        @Param("content") String content,
        @Param("tags") String tags,
        @Param("categoryPath") String categoryPath,
        @Param("contentType") DocumentIndex.ContentType contentType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("language") String language,
        Pageable pageable
    );
    
    /**
     * 获取搜索建议
     */
    @Query("SELECT DISTINCT di.title FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND LOWER(di.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY di.title LIMIT 10")
    List<String> findSearchSuggestions(
        @Param("query") String query,
        @Param("isPublicOnly") Boolean isPublicOnly
    );
    
    /**
     * 获取热门标签
     */
    @Query(value = """
        SELECT tag, COUNT(*) as count FROM (
            SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(di.tags, ',', numbers.n), ',', -1)) as tag
            FROM document_index di
            CROSS JOIN (
                SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
                UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
            ) numbers
            WHERE di.is_active = true 
            AND (:isPublicOnly = false OR di.is_public = true)
            AND di.tags IS NOT NULL 
            AND CHAR_LENGTH(di.tags) - CHAR_LENGTH(REPLACE(di.tags, ',', '')) >= numbers.n - 1
        ) tag_list
        WHERE tag != ''
        GROUP BY tag
        ORDER BY count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findPopularTags(
        @Param("isPublicOnly") Boolean isPublicOnly,
        @Param("limit") Integer limit
    );
    
    /**
     * 获取分类统计
     */
    @Query("SELECT di.categoryPath, COUNT(di) FROM DocumentIndex di " +
           "WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true) " +
           "AND di.categoryPath IS NOT NULL " +
           "GROUP BY di.categoryPath " +
           "ORDER BY COUNT(di) DESC")
    List<Object[]> findCategoryStatistics(@Param("isPublicOnly") Boolean isPublicOnly);
    
    /**
     * 根据文档ID删除索引
     */
    @Modifying
    @Query("DELETE FROM DocumentIndex di WHERE di.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);
    
    /**
     * 批量更新索引状态
     */
    @Modifying
    @Query("UPDATE DocumentIndex di SET di.isActive = :isActive WHERE di.documentId IN :documentIds")
    void updateActiveStatusByDocumentIds(
        @Param("documentIds") List<Long> documentIds,
        @Param("isActive") Boolean isActive
    );
    
    /**
     * 重建文档索引
     */
    @Modifying
    @Query("UPDATE DocumentIndex di SET di.indexVersion = :version, di.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE di.documentId = :documentId")
    void updateIndexVersion(
        @Param("documentId") Long documentId,
        @Param("version") String version
    );
    
    /**
     * 统计索引数量
     */
    @Query("SELECT COUNT(di) FROM DocumentIndex di WHERE di.isActive = true " +
           "AND (:isPublicOnly = false OR di.isPublic = true)")
    Long countActiveIndexes(@Param("isPublicOnly") Boolean isPublicOnly);
    
    /**
     * 查找需要重建索引的文档
     */
    @Query("SELECT DISTINCT di.documentId FROM DocumentIndex di " +
           "WHERE di.indexVersion != :currentVersion OR di.indexVersion IS NULL")
    List<Long> findDocumentsNeedingReindex(@Param("currentVersion") String currentVersion);
}