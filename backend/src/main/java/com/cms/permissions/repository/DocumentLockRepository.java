package com.cms.permissions.repository;

import com.cms.permissions.entity.DocumentLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentLockRepository extends JpaRepository<DocumentLock, Long> {
    
    /**
     * 根据文档ID查找锁定记录
     */
    Optional<DocumentLock> findByDocumentIdAndIsActiveTrue(Long documentId);
    
    /**
     * 根据文档ID和用户ID查找锁定记录
     */
    Optional<DocumentLock> findByDocumentIdAndLockedByAndIsActiveTrue(Long documentId, Long userId);
    
    /**
     * 查找用户锁定的所有文档
     */
    List<DocumentLock> findByLockedByAndIsActiveTrueOrderByLockedAtDesc(Long userId);
    
    /**
     * 查找过期的锁定记录
     */
    List<DocumentLock> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime currentTime);
    
    /**
     * 根据会话ID查找锁定记录
     */
    List<DocumentLock> findBySessionIdAndIsActiveTrue(String sessionId);
    
    /**
     * 检查文档是否被锁定
     */
    @Query("SELECT COUNT(l) > 0 FROM DocumentLock l WHERE l.documentId = :documentId AND l.isActive = true AND l.expiresAt > :currentTime")
    boolean isDocumentLocked(@Param("documentId") Long documentId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 检查文档是否被指定用户锁定
     */
    @Query("SELECT COUNT(l) > 0 FROM DocumentLock l WHERE l.documentId = :documentId AND l.lockedBy = :userId AND l.isActive = true AND l.expiresAt > :currentTime")
    boolean isDocumentLockedByUser(@Param("documentId") Long documentId, @Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 释放文档锁定
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.isActive = false WHERE l.documentId = :documentId AND l.lockedBy = :userId")
    void unlockDocumentByUser(@Param("documentId") Long documentId, @Param("userId") Long userId);
    
    /**
     * 释放过期的锁定
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.isActive = false WHERE l.expiresAt < :currentTime AND l.isActive = true")
    void unlockExpiredLocks(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 释放用户的所有锁定
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.isActive = false WHERE l.lockedBy = :userId AND l.isActive = true")
    void unlockAllByUser(@Param("userId") Long userId);
    
    /**
     * 释放会话的所有锁定
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.isActive = false WHERE l.sessionId = :sessionId AND l.isActive = true")
    void unlockAllBySession(@Param("sessionId") String sessionId);
    
    /**
     * 强制释放文档锁定（管理员操作）
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.isActive = false WHERE l.documentId = :documentId")
    void forceUnlockDocument(@Param("documentId") Long documentId);
    
    /**
     * 延长锁定时间
     */
    @Modifying
    @Query("UPDATE DocumentLock l SET l.expiresAt = :newExpiresAt WHERE l.documentId = :documentId AND l.lockedBy = :userId AND l.isActive = true")
    void extendLock(@Param("documentId") Long documentId, @Param("userId") Long userId, @Param("newExpiresAt") LocalDateTime newExpiresAt);
    
    /**
     * 统计活跃锁定数量
     */
    @Query("SELECT COUNT(l) FROM DocumentLock l WHERE l.isActive = true AND l.expiresAt > :currentTime")
    long countActiveLocks(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 统计用户的活跃锁定数量
     */
    @Query("SELECT COUNT(l) FROM DocumentLock l WHERE l.lockedBy = :userId AND l.isActive = true AND l.expiresAt > :currentTime")
    long countActiveLocksForUser(@Param("userId") Long userId, @Param("currentTime") LocalDateTime currentTime);
}