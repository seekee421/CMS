package com.cms.permissions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 文档锁定实体
 * 用于防止多用户同时编辑同一文档
 */
@Entity
@Table(name = "document_lock")
public class DocumentLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "document_id", unique = true)
    private Long documentId;
    
    @NotNull
    @Column(name = "locked_by")
    private Long lockedBy;
    
    @NotNull
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type")
    private LockType lockType = LockType.EDIT;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // 关联到文档
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;
    
    // 关联到用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by", insertable = false, updatable = false)
    private User user;
    
    public DocumentLock() {}
    
    public DocumentLock(Long documentId, Long lockedBy, String sessionId) {
        this.documentId = documentId;
        this.lockedBy = lockedBy;
        this.sessionId = sessionId;
        this.lockedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(2); // 默认2小时过期
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public Long getLockedBy() {
        return lockedBy;
    }
    
    public void setLockedBy(Long lockedBy) {
        this.lockedBy = lockedBy;
    }
    
    public LocalDateTime getLockedAt() {
        return lockedAt;
    }
    
    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LockType getLockType() {
        return lockType;
    }
    
    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void setDocument(Document document) {
        this.document = document;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * 检查锁是否已过期
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * 延长锁的过期时间
     */
    public void extendLock(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }
    
    public enum LockType {
        EDIT,       // 编辑锁
        READ_ONLY,  // 只读锁
        EXCLUSIVE   // 独占锁
    }
}