package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentBackup;
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
public interface DocumentBackupRepository extends JpaRepository<DocumentBackup, Long> {
    
    // 根据文档ID查找所有备份
    List<DocumentBackup> findByDocumentIdOrderByBackupCreatedAtDesc(Long documentId);
    
    // 根据文档ID和备份类型查找备份
    List<DocumentBackup> findByDocumentIdAndBackupTypeOrderByBackupCreatedAtDesc(
        Long documentId, DocumentBackup.BackupType backupType);
    
    // 查找最新的备份
    Optional<DocumentBackup> findTopByDocumentIdOrderByBackupCreatedAtDesc(Long documentId);
    
    // 根据备份版本查找
    Optional<DocumentBackup> findByDocumentIdAndBackupVersion(Long documentId, String backupVersion);
    
    // 查找指定时间范围内的备份
    List<DocumentBackup> findByBackupCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // 查找过期的备份
    @Query("SELECT db FROM DocumentBackup db WHERE db.backupCreatedAt < :cutoffDate")
    List<DocumentBackup> findExpiredBackups(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 统计文档的备份数量
    long countByDocumentId(Long documentId);
    
    // 查找需要清理的备份（超过最大版本数）
    @Query("SELECT db FROM DocumentBackup db WHERE db.documentId = :documentId " +
           "ORDER BY db.backupCreatedAt DESC")
    List<DocumentBackup> findBackupsForCleanup(@Param("documentId") Long documentId, Pageable pageable);
    
    // 根据备份状态查找
    List<DocumentBackup> findByBackupStatus(DocumentBackup.BackupStatus status);
    
    // 分页查询所有备份
    Page<DocumentBackup> findAllByOrderByBackupCreatedAtDesc(Pageable pageable);
    
    // 根据创建者查找备份
    List<DocumentBackup> findByCreatedByOrderByBackupCreatedAtDesc(Long createdBy);
    
    // 查找失败的备份
    List<DocumentBackup> findByBackupStatusAndBackupCreatedAtAfter(
        DocumentBackup.BackupStatus status, LocalDateTime after);
}