package com.cms.permissions.dto;

import java.time.LocalDateTime;

/**
 * 编辑器内容响应DTO
 */
public class EditorContentResponse {
    
    private Long documentId;
    private String title;
    private String content;
    private String status;
    private Boolean isDraft;
    private LocalDateTime lastModified;
    private LocalDateTime lastSaved;
    private String version;
    private Long createdBy;
    private String createdByName;
    private Boolean hasUnsavedChanges;
    private String changeLog;
    
    public EditorContentResponse() {}
    
    public EditorContentResponse(Long documentId, String title, String content) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
    }
    
    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsDraft() {
        return isDraft;
    }
    
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public LocalDateTime getLastSaved() {
        return lastSaved;
    }
    
    public void setLastSaved(LocalDateTime lastSaved) {
        this.lastSaved = lastSaved;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getCreatedByName() {
        return createdByName;
    }
    
    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }
    
    public Boolean getHasUnsavedChanges() {
        return hasUnsavedChanges;
    }
    
    public void setHasUnsavedChanges(Boolean hasUnsavedChanges) {
        this.hasUnsavedChanges = hasUnsavedChanges;
    }
    
    public String getChangeLog() {
        return changeLog;
    }
    
    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
}