package com.cms.permissions.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 媒体资源实体类
 * 存储文件的元数据信息
 */
@Entity
@Table(name = "media_resources")
public class MediaResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 原始文件名
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * 在MinIO中的对象名称（路径）
     */
    @Column(name = "object_name", nullable = false, unique = true)
    private String objectName;

    /**
     * 文件类型（MIME类型）
     */
    @Column(name = "content_type")
    private String contentType;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件访问URL
     */
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    /**
     * 文件分类（如：document, image, video等）
     */
    @Column(name = "file_category")
    private String fileCategory;

    /**
     * 关联的文档ID（可选）
     */
    @Column(name = "document_id")
    private Long documentId;

    /**
     * 上传用户ID
     */
    @Column(name = "uploaded_by")
    private Long uploadedBy;

    /**
     * 上传时间
     */
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    /**
     * 最后访问时间
     */
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * 是否已删除（软删除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 文件描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 文件标签（JSON格式）
     */
    @Column(name = "tags", length = 1000)
    private String tags;

    // 构造函数
    public MediaResource() {
        this.uploadTime = LocalDateTime.now();
        this.isDeleted = false;
    }

    public MediaResource(String originalFilename, String objectName, String contentType, Long fileSize) {
        this();
        this.originalFilename = originalFilename;
        this.objectName = objectName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Long uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "MediaResource{" +
                "id=" + id +
                ", originalFilename='" + originalFilename + '\'' +
                ", objectName='" + objectName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", fileCategory='" + fileCategory + '\'' +
                ", uploadTime=" + uploadTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
}