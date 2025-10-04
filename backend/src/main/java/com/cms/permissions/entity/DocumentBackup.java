package com.cms.permissions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_backup")
public class DocumentBackup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "document_id")
    private Long documentId;

    @NotNull
    @Column(name = "backup_version")
    private String backupVersion;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DocumentStatus status;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;

    @Column(name = "backup_created_at")
    private LocalDateTime backupCreatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_type")
    private BackupType backupType;

    @Column(name = "backup_reason")
    private String backupReason;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum")
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "backup_status")
    private BackupStatus backupStatus = BackupStatus.PENDING;

    // 构造函数
    public DocumentBackup() {}

    public DocumentBackup(Document document, BackupType backupType, String backupReason) {
        this.documentId = document.getId();
        this.title = document.getTitle();
        this.content = document.getContent();
        // 转换Document.DocumentStatus到DocumentBackup.DocumentStatus
        this.status = convertDocumentStatus(document.getStatus());
        this.isPublic = document.getIsPublic();
        this.createdBy = document.getCreatedBy();
        this.originalCreatedAt = document.getCreatedAt();
        this.originalUpdatedAt = document.getUpdatedAt();
        this.backupCreatedAt = LocalDateTime.now();
        this.backupType = backupType;
        this.backupReason = backupReason;
        this.backupVersion = generateBackupVersion();
    }

    private DocumentStatus convertDocumentStatus(Document.DocumentStatus originalStatus) {
        if (originalStatus == null) {
            return DocumentStatus.DRAFT;
        }
        switch (originalStatus) {
            case DRAFT: return DocumentStatus.DRAFT;
            case PENDING_APPROVAL: return DocumentStatus.PENDING_APPROVAL;
            case PUBLISHED: return DocumentStatus.PUBLISHED;
            case REJECTED: return DocumentStatus.REJECTED;
            default: return DocumentStatus.DRAFT;
        }
    }

    private String generateBackupVersion() {
        return "v" + System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getBackupVersion() { return backupVersion; }
    public void setBackupVersion(String backupVersion) { this.backupVersion = backupVersion; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getOriginalCreatedAt() { return originalCreatedAt; }
    public void setOriginalCreatedAt(LocalDateTime originalCreatedAt) { this.originalCreatedAt = originalCreatedAt; }

    public LocalDateTime getOriginalUpdatedAt() { return originalUpdatedAt; }
    public void setOriginalUpdatedAt(LocalDateTime originalUpdatedAt) { this.originalUpdatedAt = originalUpdatedAt; }

    public LocalDateTime getBackupCreatedAt() { return backupCreatedAt; }
    public void setBackupCreatedAt(LocalDateTime backupCreatedAt) { this.backupCreatedAt = backupCreatedAt; }

    public BackupType getBackupType() { return backupType; }
    public void setBackupType(BackupType backupType) { this.backupType = backupType; }

    public String getBackupReason() { return backupReason; }
    public void setBackupReason(String backupReason) { this.backupReason = backupReason; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public BackupStatus getBackupStatus() { return backupStatus; }
    public void setBackupStatus(BackupStatus backupStatus) { this.backupStatus = backupStatus; }

    // 枚举定义
    public enum BackupType {
        AUTOMATIC,      // 自动备份
        MANUAL,         // 手动备份
        BEFORE_UPDATE,  // 更新前备份
        BEFORE_DELETE,  // 删除前备份
        SCHEDULED       // 定时备份
    }

    public enum BackupStatus {
        PENDING,        // 待处理
        IN_PROGRESS,    // 备份中
        COMPLETED,      // 已完成
        FAILED,         // 失败
        CORRUPTED       // 损坏
    }

    public enum DocumentStatus {
        DRAFT, PENDING_APPROVAL, PUBLISHED, REJECTED
    }
}