package com.cms.permissions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_configuration")
public class BackupConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "config_name", unique = true)
    private String configName;

    @Column(name = "auto_backup_enabled")
    private Boolean autoBackupEnabled = true;

    @Column(name = "backup_interval_hours")
    private Integer backupIntervalHours = 24;

    @Column(name = "max_backup_versions")
    private Integer maxBackupVersions = 10;

    @Column(name = "backup_retention_days")
    private Integer backupRetentionDays = 30;

    @Column(name = "backup_storage_path")
    private String backupStoragePath;

    @Column(name = "compression_enabled")
    private Boolean compressionEnabled = true;

    @Column(name = "encryption_enabled")
    private Boolean encryptionEnabled = false;

    @Column(name = "backup_on_update")
    private Boolean backupOnUpdate = true;

    @Column(name = "backup_on_delete")
    private Boolean backupOnDelete = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "is_active")
    private Boolean isActive = true;

    // 构造函数
    public BackupConfiguration() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public Boolean getAutoBackupEnabled() { return autoBackupEnabled; }
    public void setAutoBackupEnabled(Boolean autoBackupEnabled) { this.autoBackupEnabled = autoBackupEnabled; }

    public Integer getBackupIntervalHours() { return backupIntervalHours; }
    public void setBackupIntervalHours(Integer backupIntervalHours) { this.backupIntervalHours = backupIntervalHours; }

    public Integer getMaxBackupVersions() { return maxBackupVersions; }
    public void setMaxBackupVersions(Integer maxBackupVersions) { this.maxBackupVersions = maxBackupVersions; }

    public Integer getBackupRetentionDays() { return backupRetentionDays; }
    public void setBackupRetentionDays(Integer backupRetentionDays) { this.backupRetentionDays = backupRetentionDays; }

    public String getBackupStoragePath() { return backupStoragePath; }
    public void setBackupStoragePath(String backupStoragePath) { this.backupStoragePath = backupStoragePath; }

    public Boolean getCompressionEnabled() { return compressionEnabled; }
    public void setCompressionEnabled(Boolean compressionEnabled) { this.compressionEnabled = compressionEnabled; }

    public Boolean getEncryptionEnabled() { return encryptionEnabled; }
    public void setEncryptionEnabled(Boolean encryptionEnabled) { this.encryptionEnabled = encryptionEnabled; }

    public Boolean getBackupOnUpdate() { return backupOnUpdate; }
    public void setBackupOnUpdate(Boolean backupOnUpdate) { this.backupOnUpdate = backupOnUpdate; }

    public Boolean getBackupOnDelete() { return backupOnDelete; }
    public void setBackupOnDelete(Boolean backupOnDelete) { this.backupOnDelete = backupOnDelete; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}