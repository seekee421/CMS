package com.cms.permissions.repository;

import com.cms.permissions.entity.MigrationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MigrationLogRepository extends JpaRepository<MigrationLog, Long> {
    
    /**
     * 根据源URL查找迁移日志
     */
    Optional<MigrationLog> findBySourceUrl(String sourceUrl);
    
    /**
     * 根据文档ID查找迁移日志
     */
    Optional<MigrationLog> findByDocumentId(Long documentId);
    
    /**
     * 根据状态查找迁移日志
     */
    List<MigrationLog> findByStatusOrderByCreatedAtDesc(MigrationLog.MigrationStatus status);
    
    /**
     * 根据创建者查找迁移日志
     */
    Page<MigrationLog> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);
    
    /**
     * 查找指定时间范围内的迁移日志
     */
    @Query("SELECT m FROM MigrationLog m WHERE m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt DESC")
    List<MigrationLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计各状态的迁移数量
     */
    @Query("SELECT m.status, COUNT(m) FROM MigrationLog m GROUP BY m.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找失败的迁移日志
     */
    List<MigrationLog> findByStatusAndErrorMessageIsNotNullOrderByCreatedAtDesc(MigrationLog.MigrationStatus status);
    
    /**
     * 检查URL是否已经迁移过
     */
    boolean existsBySourceUrlAndStatus(String sourceUrl, MigrationLog.MigrationStatus status);
}