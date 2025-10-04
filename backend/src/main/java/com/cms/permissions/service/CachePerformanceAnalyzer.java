package com.cms.permissions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CachePerformanceAnalyzer {

    @Autowired
    private PermissionCacheService permissionCacheService;

    // 性能指标收集
    private final Map<String, AtomicLong> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationTimes = new ConcurrentHashMap<>();
    private final List<PerformanceRecord> performanceHistory = Collections.synchronizedList(new ArrayList<>());

    public CachePerformanceAnalyzer() {
        // 初始化计数器
        operationCounts.put("cache_hits", new AtomicLong(0));
        operationCounts.put("cache_misses", new AtomicLong(0));
        operationCounts.put("cache_evictions", new AtomicLong(0));
        operationTimes.put("avg_response_time", new AtomicLong(0));
    }

    /**
     * 记录缓存操作
     */
    public void recordCacheOperation(String operation, long executionTime, boolean isHit) {
        operationCounts.get(isHit ? "cache_hits" : "cache_misses").incrementAndGet();
        operationTimes.get("avg_response_time").addAndGet(executionTime);

        // 记录性能历史
        PerformanceRecord record = new PerformanceRecord(
                LocalDateTime.now(),
                operation,
                executionTime,
                isHit
        );
        performanceHistory.add(record);

        // 保持历史记录在合理范围内（最近1000条）
        if (performanceHistory.size() > 1000) {
            performanceHistory.remove(0);
        }
    }

    /**
     * 记录缓存清除操作
     */
    public void recordCacheEviction(String cacheName) {
        operationCounts.get("cache_evictions").incrementAndGet();
    }

    /**
     * 获取性能分析报告
     */
    public PerformanceReport getPerformanceReport() {
        PermissionCacheService.CacheStats stats = permissionCacheService.getCacheStats();
        
        long totalHits = stats.getUserPermissionHits() + stats.getDocumentAssignmentHits() + 
                        stats.getDocumentPublicStatusHits();
        long totalMisses = stats.getUserPermissionMisses() + stats.getDocumentAssignmentMisses() + 
                          stats.getDocumentPublicStatusMisses();
        long totalRequests = totalHits + totalMisses;

        double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0;
        double missRate = totalRequests > 0 ? (double) totalMisses / totalRequests * 100 : 0;

        // 计算平均响应时间
        long avgResponseTime = totalRequests > 0 ? 
                operationTimes.get("avg_response_time").get() / totalRequests : 0;

        return new PerformanceReport(
                hitRate,
                missRate,
                totalRequests,
                avgResponseTime,
                operationCounts.get("cache_evictions").get(),
                getRecentPerformanceTrend(),
                getCacheEfficiencyByType(stats),
                getPerformanceRecommendations(hitRate, avgResponseTime)
        );
    }

    /**
     * 获取最近的性能趋势
     */
    private List<TrendData> getRecentPerformanceTrend() {
        List<TrendData> trend = new ArrayList<>();
        
        // 按小时分组统计
        Map<String, List<PerformanceRecord>> hourlyGroups = new HashMap<>();
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        
        for (PerformanceRecord record : performanceHistory) {
            String hourKey = record.getTimestamp().format(hourFormatter);
            hourlyGroups.computeIfAbsent(hourKey, k -> new ArrayList<>()).add(record);
        }

        for (Map.Entry<String, List<PerformanceRecord>> entry : hourlyGroups.entrySet()) {
            List<PerformanceRecord> records = entry.getValue();
            long hits = records.stream().mapToLong(r -> r.isHit() ? 1 : 0).sum();
            long total = records.size();
            double hitRate = total > 0 ? (double) hits / total * 100 : 0;
            double avgTime = records.stream().mapToLong(PerformanceRecord::getExecutionTime).average().orElse(0);

            trend.add(new TrendData(entry.getKey(), hitRate, avgTime, total));
        }

        // 按时间排序
        trend.sort(Comparator.comparing(TrendData::getTimeLabel));
        return trend;
    }

    /**
     * 按缓存类型获取效率统计
     */
    private Map<String, CacheTypeEfficiency> getCacheEfficiencyByType(PermissionCacheService.CacheStats stats) {
        Map<String, CacheTypeEfficiency> efficiency = new HashMap<>();

        // 用户权限缓存效率
        long userPermTotal = stats.getUserPermissionHits() + stats.getUserPermissionMisses();
        double userPermHitRate = userPermTotal > 0 ? 
                (double) stats.getUserPermissionHits() / userPermTotal * 100 : 0;
        efficiency.put("userPermissions", new CacheTypeEfficiency(
                userPermHitRate, userPermTotal, stats.getUserPermissionHits(), stats.getUserPermissionMisses()));

        // 文档分配缓存效率
        long docAssignTotal = stats.getDocumentAssignmentHits() + stats.getDocumentAssignmentMisses();
        double docAssignHitRate = docAssignTotal > 0 ? 
                (double) stats.getDocumentAssignmentHits() / docAssignTotal * 100 : 0;
        efficiency.put("documentAssignments", new CacheTypeEfficiency(
                docAssignHitRate, docAssignTotal, stats.getDocumentAssignmentHits(), stats.getDocumentAssignmentMisses()));

        // 文档公开状态缓存效率
        long docStatusTotal = stats.getDocumentPublicStatusHits() + stats.getDocumentPublicStatusMisses();
        double docStatusHitRate = docStatusTotal > 0 ? 
                (double) stats.getDocumentPublicStatusHits() / docStatusTotal * 100 : 0;
        efficiency.put("documentPublicStatus", new CacheTypeEfficiency(
                docStatusHitRate, docStatusTotal, stats.getDocumentPublicStatusHits(), stats.getDocumentPublicStatusMisses()));

        return efficiency;
    }

    /**
     * 获取性能优化建议
     */
    private List<String> getPerformanceRecommendations(double hitRate, long avgResponseTime) {
        List<String> recommendations = new ArrayList<>();

        if (hitRate < 70) {
            recommendations.add("缓存命中率较低(" + String.format("%.1f", hitRate) + "%)，建议增加缓存TTL时间");
        }
        if (hitRate > 95) {
            recommendations.add("缓存命中率很高(" + String.format("%.1f", hitRate) + "%)，可以考虑适当减少缓存TTL以节省内存");
        }
        if (avgResponseTime > 100) {
            recommendations.add("平均响应时间较长(" + avgResponseTime + "ms)，建议检查Redis连接配置");
        }
        if (operationCounts.get("cache_evictions").get() > 1000) {
            recommendations.add("缓存清除操作频繁，建议优化缓存失效策略");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("缓存性能良好，继续保持当前配置");
        }

        return recommendations;
    }

    /**
     * 重置性能统计
     */
    public void resetStatistics() {
        operationCounts.values().forEach(counter -> counter.set(0));
        operationTimes.values().forEach(counter -> counter.set(0));
        performanceHistory.clear();
    }

    // 内部类定义
    public static class PerformanceRecord {
        private final LocalDateTime timestamp;
        private final String operation;
        private final long executionTime;
        private final boolean hit;

        public PerformanceRecord(LocalDateTime timestamp, String operation, long executionTime, boolean hit) {
            this.timestamp = timestamp;
            this.operation = operation;
            this.executionTime = executionTime;
            this.hit = hit;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getOperation() { return operation; }
        public long getExecutionTime() { return executionTime; }
        public boolean isHit() { return hit; }
    }

    public static class PerformanceReport {
        private final double hitRate;
        private final double missRate;
        private final long totalRequests;
        private final long avgResponseTime;
        private final long totalEvictions;
        private final List<TrendData> performanceTrend;
        private final Map<String, CacheTypeEfficiency> cacheEfficiency;
        private final List<String> recommendations;

        public PerformanceReport(double hitRate, double missRate, long totalRequests, long avgResponseTime,
                               long totalEvictions, List<TrendData> performanceTrend,
                               Map<String, CacheTypeEfficiency> cacheEfficiency, List<String> recommendations) {
            this.hitRate = hitRate;
            this.missRate = missRate;
            this.totalRequests = totalRequests;
            this.avgResponseTime = avgResponseTime;
            this.totalEvictions = totalEvictions;
            this.performanceTrend = performanceTrend;
            this.cacheEfficiency = cacheEfficiency;
            this.recommendations = recommendations;
        }

        // Getters
        public double getHitRate() { return hitRate; }
        public double getMissRate() { return missRate; }
        public long getTotalRequests() { return totalRequests; }
        public long getAvgResponseTime() { return avgResponseTime; }
        public long getTotalEvictions() { return totalEvictions; }
        public List<TrendData> getPerformanceTrend() { return performanceTrend; }
        public Map<String, CacheTypeEfficiency> getCacheEfficiency() { return cacheEfficiency; }
        public List<String> getRecommendations() { return recommendations; }
    }

    public static class TrendData {
        private final String timeLabel;
        private final double hitRate;
        private final double avgResponseTime;
        private final long requestCount;

        public TrendData(String timeLabel, double hitRate, double avgResponseTime, long requestCount) {
            this.timeLabel = timeLabel;
            this.hitRate = hitRate;
            this.avgResponseTime = avgResponseTime;
            this.requestCount = requestCount;
        }

        // Getters
        public String getTimeLabel() { return timeLabel; }
        public double getHitRate() { return hitRate; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public long getRequestCount() { return requestCount; }
    }

    public static class CacheTypeEfficiency {
        private final double hitRate;
        private final long totalRequests;
        private final long hits;
        private final long misses;

        public CacheTypeEfficiency(double hitRate, long totalRequests, long hits, long misses) {
            this.hitRate = hitRate;
            this.totalRequests = totalRequests;
            this.hits = hits;
            this.misses = misses;
        }

        // Getters
        public double getHitRate() { return hitRate; }
        public long getTotalRequests() { return totalRequests; }
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
    }
}