package com.cms.permissions.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 编辑器内容请求DTO
 */
public class EditorContentRequest {
    
    @NotNull(message = "Document ID is required")
    private Long documentId;
    
    @Size(max = 50000, message = "Content must not exceed 50000 characters")
    private String content;
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    private Boolean isDraft = false;
    
    private String changeLog;
    
    // 编辑器相关配置
    private String editorMode = "markdown"; // markdown, wysiwyg
    private Boolean autoSave = true;
    private Integer autoSaveInterval = 30; // 秒
    
    public EditorContentRequest() {}
    
    public EditorContentRequest(Long documentId, String content, String title) {
        this.documentId = documentId;
        this.content = content;
        this.title = title;
    }
    
    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Boolean getIsDraft() {
        return isDraft;
    }
    
    public void setIsDraft(Boolean isDraft) {
        this.isDraft = isDraft;
    }
    
    public String getChangeLog() {
        return changeLog;
    }
    
    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
    
    public String getEditorMode() {
        return editorMode;
    }
    
    public void setEditorMode(String editorMode) {
        this.editorMode = editorMode;
    }
    
    public Boolean getAutoSave() {
        return autoSave;
    }
    
    public void setAutoSave(Boolean autoSave) {
        this.autoSave = autoSave;
    }
    
    public Integer getAutoSaveInterval() {
        return autoSaveInterval;
    }
    
    public void setAutoSaveInterval(Integer autoSaveInterval) {
        this.autoSaveInterval = autoSaveInterval;
    }
}