package com.cms.permissions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 * 用于记录用户搜索行为和提供搜索建议
 */
@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_search_query", columnList = "search_query"),
    @Index(name = "idx_search_time", columnList = "search_time"),
    @Index(name = "idx_result_count", columnList = "result_count"),
    @Index(name = "idx_ip_address", columnList = "ip_address")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户ID（可为空，支持匿名搜索）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 搜索查询
     */
    @Column(name = "search_query", nullable = false, length = 500)
    private String searchQuery;
    
    /**
     * 搜索类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "search_type", nullable = false)
    private SearchType searchType;
    
    /**
     * 搜索结果数量
     */
    @Column(name = "result_count", nullable = false)
    private Long resultCount;
    
    /**
     * 搜索耗时（毫秒）
     */
    @Column(name = "search_duration")
    private Long searchDuration;
    
    /**
     * 是否找到结果
     */
    @Column(name = "has_results", nullable = false)
    private Boolean hasResults;
    
    /**
     * 用户IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 搜索来源页面
     */
    @Column(name = "referer", length = 500)
    private String referer;
    
    /**
     * 应用的筛选器（JSON格式）
     */
    @Column(name = "applied_filters", columnDefinition = "TEXT")
    private String appliedFilters;
    
    /**
     * 排序方式
     */
    @Column(name = "sort_by", length = 50)
    private String sortBy;
    
    /**
     * 排序方向
     */
    @Column(name = "sort_direction", length = 10)
    private String sortDirection;
    
    /**
     * 页码
     */
    @Column(name = "page_number")
    private Integer pageNumber;
    
    /**
     * 每页大小
     */
    @Column(name = "page_size")
    private Integer pageSize;
    
    /**
     * 搜索时间
     */
    @CreationTimestamp
    @Column(name = "search_time", nullable = false)
    private LocalDateTime searchTime;
    
    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    /**
     * 是否为建议搜索
     */
    @Column(name = "is_suggestion", nullable = false)
    private Boolean isSuggestion;
    
    /**
     * 搜索语言
     */
    @Column(name = "search_language", length = 10)
    private String searchLanguage;
    
    /**
     * 关联的用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    /**
     * 搜索类型枚举
     */
    public enum SearchType {
        /**
         * 全文搜索
         */
        FULL_TEXT,
        
        /**
         * 标题搜索
         */
        TITLE_ONLY,
        
        /**
         * 内容搜索
         */
        CONTENT_ONLY,
        
        /**
         * 标签搜索
         */
        TAG_SEARCH,
        
        /**
         * 分类搜索
         */
        CATEGORY_SEARCH,
        
        /**
         * 高级搜索
         */
        ADVANCED_SEARCH,
        
        /**
         * 模糊搜索
         */
        FUZZY_SEARCH,
        
        /**
         * 精确搜索
         */
        EXACT_SEARCH
    }
    
    /**
     * 构造函数 - 创建基本搜索历史
     */
    public SearchHistory(String searchQuery, SearchType searchType, Long resultCount) {
        this.searchQuery = searchQuery;
        this.searchType = searchType;
        this.resultCount = resultCount;
        this.hasResults = resultCount > 0;
        this.isSuggestion = false;
        this.searchLanguage = "zh-CN";
    }
    
    /**
     * 设置默认值
     */
    @PrePersist
    public void prePersist() {
        if (this.hasResults == null) {
            this.hasResults = this.resultCount != null && this.resultCount > 0;
        }
        if (this.isSuggestion == null) {
            this.isSuggestion = false;
        }
        if (this.searchLanguage == null) {
            this.searchLanguage = "zh-CN";
        }
        if (this.pageNumber == null) {
            this.pageNumber = 1;
        }
        if (this.pageSize == null) {
            this.pageSize = 10;
        }
    }
}