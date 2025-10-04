package com.cms.permissions.dto;

import com.cms.permissions.entity.DocumentBackup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "文档备份请求")
public class BackupRequest {
    
    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", example = "1")
    private Long documentId;
    
    @Schema(description = "备份类型", example = "MANUAL")
    private DocumentBackup.BackupType backupType = DocumentBackup.BackupType.MANUAL;
    
    @Schema(description = "备份原因", example = "手动备份")
    private String reason;
    
    public BackupRequest() {}
    
    public BackupRequest(Long documentId, DocumentBackup.BackupType backupType, String reason) {
        this.documentId = documentId;
        this.backupType = backupType;
        this.reason = reason;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public DocumentBackup.BackupType getBackupType() {
        return backupType;
    }
    
    public void setBackupType(DocumentBackup.BackupType backupType) {
        this.backupType = backupType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}