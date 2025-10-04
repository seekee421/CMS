package com.cms.permissions.repository;

import com.cms.permissions.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Find documents that are published or assigned to the user
    @Query("SELECT d FROM Document d WHERE d.status = com.cms.permissions.entity.Document$DocumentStatus.PUBLISHED OR d.id IN " +
           "(SELECT da.documentId FROM DocumentAssignment da WHERE da.userId = :userId)")
    List<Document> findDocumentsForUser(@Param("userId") Long userId);

    // Find documents assigned to a specific user with a specific role
    @Query("SELECT d FROM Document d WHERE d.id IN " +
           "(SELECT da.documentId FROM DocumentAssignment da WHERE da.userId = :userId AND da.assignmentType = :assignmentType)")
    List<Document> findDocumentsAssignedToUser(@Param("userId") Long userId,
                                               @Param("assignmentType") com.cms.permissions.entity.DocumentAssignment.AssignmentType assignmentType);

    // 迁移相关查询方法
    
    /**
     * 根据源URL查找文档
     */
    @Query("SELECT d FROM Document d WHERE d.sourceUrl = :sourceUrl")
    List<Document> findBySourceUrl(@Param("sourceUrl") String sourceUrl);
    
    /**
     * 根据原始ID查找文档
     */
    @Query("SELECT d FROM Document d WHERE d.originalId = :originalId")
    List<Document> findByOriginalId(@Param("originalId") String originalId);
    
    /**
     * 根据迁移状态查找文档
     */
    @Query("SELECT d FROM Document d WHERE d.migrationStatus = :status ORDER BY d.migrationDate DESC")
    List<Document> findByMigrationStatus(@Param("status") Document.MigrationStatus status);
    
    /**
     * 根据分类查找文档
     */
    @Query("SELECT d FROM Document d WHERE d.category = :category ORDER BY d.createdAt DESC")
    List<Document> findByCategory(@Param("category") String category);
    
    /**
     * 根据版本查找文档
     */
    @Query("SELECT d FROM Document d WHERE d.version = :version ORDER BY d.createdAt DESC")
    List<Document> findByVersion(@Param("version") String version);
    
    /**
     * 查找已迁移的文档
     */
    @Query("SELECT d FROM Document d WHERE d.migrationStatus = com.cms.permissions.entity.Document$MigrationStatus.COMPLETED ORDER BY d.migrationDate DESC")
    List<Document> findMigratedDocuments();
    
    /**
     * 检查源URL是否已存在
     */
    boolean existsBySourceUrl(String sourceUrl);
    
    /**
     * 检查原始ID是否已存在
     */
    boolean existsByOriginalId(String originalId);
}