package com.cms.permissions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 文档索引实体
 * 用于存储文档的搜索索引信息和内容块
 */
@Entity
@Table(name = "document_index", indexes = {
    @Index(name = "idx_document_id", columnList = "document_id"),
    @Index(name = "idx_content_type", columnList = "content_type"),
    @Index(name = "idx_language", columnList = "language"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIndex {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联的文档ID
     */
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    /**
     * 内容块标题
     */
    @Column(name = "title", length = 500)
    private String title;
    
    /**
     * 索引内容（用于搜索）
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    /**
     * 纯文本内容（去除HTML标签）
     */
    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText;
    
    /**
     * 内容摘要
     */
    @Column(name = "summary", length = 1000)
    private String summary;
    
    /**
     * 内容类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;
    
    /**
     * 内容块在文档中的位置
     */
    @Column(name = "block_position")
    private Integer blockPosition;
    
    /**
     * 内容块的锚点ID
     */
    @Column(name = "anchor_id", length = 100)
    private String anchorId;
    
    /**
     * 标签（以逗号分隔）
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 分类路径
     */
    @Column(name = "category_path", length = 500)
    private String categoryPath;
    
    /**
     * 语言代码
     */
    @Column(name = "language", length = 10, nullable = false)
    private String language;
    
    /**
     * 字数统计
     */
    @Column(name = "word_count")
    private Integer wordCount;
    
    /**
     * 字符数统计
     */
    @Column(name = "character_count")
    private Integer characterCount;
    
    /**
     * 搜索权重
     */
    @Column(name = "search_weight")
    private Double searchWeight;
    
    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    /**
     * 是否为公开内容
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 索引版本
     */
    @Column(name = "index_version", length = 50)
    private String indexVersion;
    
    /**
     * 关联的文档实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;
    
    /**
     * 内容类型枚举
     */
    public enum ContentType {
        /**
         * 标题
         */
        TITLE,
        
        /**
         * 段落
         */
        PARAGRAPH,
        
        /**
         * 代码块
         */
        CODE_BLOCK,
        
        /**
         * 表格
         */
        TABLE,
        
        /**
         * 列表
         */
        LIST,
        
        /**
         * 引用
         */
        QUOTE,
        
        /**
         * 图片描述
         */
        IMAGE_CAPTION,
        
        /**
         * 链接文本
         */
        LINK_TEXT,
        
        /**
         * 元数据
         */
        METADATA,
        
        /**
         * 全文内容
         */
        FULL_CONTENT
    }
    
    /**
     * 构造函数 - 创建基本索引
     */
    public DocumentIndex(Long documentId, String title, String content, ContentType contentType) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.contentType = contentType;
        this.language = "zh-CN";
        this.isActive = true;
        this.isPublic = true;
        this.searchWeight = 1.0;
    }
    
    /**
     * 设置默认值
     */
    @PrePersist
    public void prePersist() {
        if (this.language == null) {
            this.language = "zh-CN";
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isPublic == null) {
            this.isPublic = true;
        }
        if (this.searchWeight == null) {
            this.searchWeight = 1.0;
        }
        if (this.wordCount == null && this.plainText != null) {
            this.wordCount = this.plainText.length();
        }
        if (this.characterCount == null && this.plainText != null) {
            this.characterCount = this.plainText.length();
        }
    }
}