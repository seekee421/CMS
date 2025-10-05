package com.cms.permissions.repository;

import com.cms.permissions.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索历史Repository
 * 提供搜索历史记录和统计分析的数据访问方法
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    /**
     * 根据用户ID查找搜索历史
     */
    Page<SearchHistory> findByUserIdOrderBySearchTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID和搜索查询查找历史
     */
    List<SearchHistory> findByUserIdAndSearchQueryContainingIgnoreCaseOrderBySearchTimeDesc(
        Long userId, String searchQuery
    );
    
    /**
     * 根据IP地址查找搜索历史（匿名用户）
     */
    Page<SearchHistory> findByUserIdIsNullAndIpAddressOrderBySearchTimeDesc(
        String ipAddress, Pageable pageable
    );
    
    /**
     * 获取热门搜索查询
     */
    @Query("SELECT sh.searchQuery, COUNT(sh) as searchCount FROM SearchHistory sh " +
           "WHERE sh.searchTime >= :startDate " +
           "AND (:hasResults IS NULL OR sh.hasResults = :hasResults) " +
           "GROUP BY sh.searchQuery " +
           "ORDER BY searchCount DESC")
    List<Object[]> findPopularSearchQueries(
        @Param("startDate") LocalDateTime startDate,
        @Param("hasResults") Boolean hasResults,
        Pageable pageable
    );
    
    /**
     * 获取搜索建议（基于历史搜索）
     */
    @Query("SELECT DISTINCT sh.searchQuery FROM SearchHistory sh " +
           "WHERE LOWER(sh.searchQuery) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND sh.hasResults = true " +
           "AND sh.searchTime >= :recentDate " +
           "ORDER BY sh.searchTime DESC")
    List<String> findSearchSuggestions(
        @Param("query") String query,
        @Param("recentDate") LocalDateTime recentDate,
        Pageable pageable
    );
    
    /**
     * 统计搜索次数
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchTime >= :startDate " +
           "AND sh.searchTime <= :endDate")
    Long countSearchesBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 统计成功搜索次数
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.searchTime >= :startDate " +
           "AND sh.searchTime <= :endDate AND sh.hasResults = true")
    Long countSuccessfulSearchesBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 统计平均搜索耗时
     */
    @Query("SELECT AVG(sh.searchDuration) FROM SearchHistory sh WHERE sh.searchTime >= :startDate " +
           "AND sh.searchTime <= :endDate AND sh.searchDuration IS NOT NULL")
    Double averageSearchDurationBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 按搜索类型统计
     */
    @Query("SELECT sh.searchType, COUNT(sh) FROM SearchHistory sh " +
           "WHERE sh.searchTime >= :startDate AND sh.searchTime <= :endDate " +
           "GROUP BY sh.searchType " +
           "ORDER BY COUNT(sh) DESC")
    List<Object[]> countBySearchTypeBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 按小时统计搜索量
     */
    @Query(value = """
        SELECT HOUR(sh.search_time) as hour, COUNT(*) as count 
        FROM search_history sh 
        WHERE sh.search_time >= :startDate AND sh.search_time <= :endDate 
        GROUP BY HOUR(sh.search_time) 
        ORDER BY hour
        """, nativeQuery = true)
    List<Object[]> countByHourBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 按日期统计搜索量
     */
    @Query(value = """
        SELECT DATE(sh.search_time) as date, COUNT(*) as count 
        FROM search_history sh 
        WHERE sh.search_time >= :startDate AND sh.search_time <= :endDate 
        GROUP BY DATE(sh.search_time) 
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> countByDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 获取用户搜索行为分析
     */
    @Query("SELECT sh.userId, COUNT(sh) as searchCount, " +
           "AVG(sh.searchDuration) as avgDuration, " +
           "SUM(CASE WHEN sh.hasResults = true THEN 1 ELSE 0 END) as successCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.userId IS NOT NULL " +
           "AND sh.searchTime >= :startDate AND sh.searchTime <= :endDate " +
           "GROUP BY sh.userId " +
           "ORDER BY searchCount DESC")
    List<Object[]> getUserSearchAnalytics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * 获取无结果搜索查询
     */
    @Query("SELECT sh.searchQuery, COUNT(sh) as count FROM SearchHistory sh " +
           "WHERE sh.hasResults = false " +
           "AND sh.searchTime >= :startDate " +
           "GROUP BY sh.searchQuery " +
           "ORDER BY count DESC")
    List<Object[]> findNoResultQueries(
        @Param("startDate") LocalDateTime startDate,
        Pageable pageable
    );
    
    /**
     * 获取搜索趋势数据
     */
    @Query(value = """
        SELECT 
            DATE(sh.search_time) as date,
            COUNT(*) as total_searches,
            COUNT(CASE WHEN sh.has_results = true THEN 1 END) as successful_searches,
            AVG(sh.search_duration) as avg_duration,
            COUNT(DISTINCT sh.search_query) as unique_queries
        FROM search_history sh 
        WHERE sh.search_time >= :startDate AND sh.search_time <= :endDate 
        GROUP BY DATE(sh.search_time) 
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getSearchTrends(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * 删除过期的搜索历史
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.searchTime < :cutoffDate")
    int deleteOldSearchHistory(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 删除用户的搜索历史
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 检查是否存在重复搜索
     */
    @Query("SELECT COUNT(sh) > 0 FROM SearchHistory sh " +
           "WHERE sh.userId = :userId " +
           "AND sh.searchQuery = :searchQuery " +
           "AND sh.searchTime >= :recentTime")
    boolean existsRecentSearch(
        @Param("userId") Long userId,
        @Param("searchQuery") String searchQuery,
        @Param("recentTime") LocalDateTime recentTime
    );
    
    /**
     * 获取用户最近的搜索查询
     */
    @Query("SELECT DISTINCT sh.searchQuery FROM SearchHistory sh " +
           "WHERE sh.userId = :userId " +
           "ORDER BY sh.searchTime DESC")
    List<String> findRecentSearchQueriesByUser(
        @Param("userId") Long userId,
        Pageable pageable
    );
    
    /**
     * 获取IP地址的最近搜索查询（匿名用户）
     */
    @Query("SELECT DISTINCT sh.searchQuery FROM SearchHistory sh " +
           "WHERE sh.userId IS NULL AND sh.ipAddress = :ipAddress " +
           "ORDER BY sh.searchTime DESC")
    List<String> findRecentSearchQueriesByIp(
        @Param("ipAddress") String ipAddress,
        Pageable pageable
    );
}