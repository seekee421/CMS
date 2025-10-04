package com.cms.permissions.cache;

import com.cms.permissions.service.CachePerformanceAnalyzer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 缓存内存优化器
 * 负责监控和优化缓存内存使用，包括自动清理、内存压缩等功能
 */
@Service
public class CacheMemoryOptimizer {

    private static final Logger logger = LoggerFactory.getLogger(
        CacheMemoryOptimizer.class
    );

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Lazy
    private CachePerformanceAnalyzer performanceAnalyzer;

    @Value("${cache.optimization.memory.max-memory-usage:0.8}")
    private double maxMemoryUsageRatio;

    @Value("${cache.optimization.memory.cleanup-threshold:0.9}")
    private double cleanupThreshold;

    @Value("${cache.optimization.memory.batch-size:1000}")
    private int batchSize;

    @Value("${cache.optimization.memory.ttl-extension-factor:1.5}")
    private double ttlExtensionFactor;

    @Value("${cache.optimization.memory.enabled:true}")
    private boolean optimizationEnabled;

    /**
     * 内存使用信息
     */
    public static class MemoryUsageInfo {

        private final long usedMemory;
        private final long maxMemory;
        private final double usageRatio;
        private final long keyCount;
        private final Map<String, Long> cacheTypeCounts;
        private final LocalDateTime timestamp;

        public MemoryUsageInfo(
            long usedMemory,
            long maxMemory,
            double usageRatio,
            long keyCount,
            Map<String, Long> cacheTypeCounts
        ) {
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.usageRatio = usageRatio;
            this.keyCount = keyCount;
            this.cacheTypeCounts = new HashMap<>(cacheTypeCounts);
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public long getUsedMemory() {
            return usedMemory;
        }

        public long getMaxMemory() {
            return maxMemory;
        }

        public double getUsageRatio() {
            return usageRatio;
        }

        public long getKeyCount() {
            return keyCount;
        }

        public Map<String, Long> getCacheTypeCounts() {
            return new HashMap<>(cacheTypeCounts);
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public boolean isMemoryPressure() {
            return usageRatio > 0.8;
        }

        public boolean requiresCleanup() {
            return usageRatio > 0.9;
        }

        @Override
        public String toString() {
            return String.format(
                "MemoryUsageInfo{usedMemory=%d, maxMemory=%d, usageRatio=%.2f%%, keyCount=%d, timestamp=%s}",
                usedMemory,
                maxMemory,
                usageRatio * 100,
                keyCount,
                timestamp
            );
        }
    }

    /**
     * 清理结果
     */
    public static class CleanupResult {

        private final int keysRemoved;
        private final long memoryFreed;
        private final long durationMs;
        private final Map<String, Integer> cacheTypeCleanup;
        private final LocalDateTime timestamp;

        public CleanupResult(
            int keysRemoved,
            long memoryFreed,
            long durationMs,
            Map<String, Integer> cacheTypeCleanup
        ) {
            this.keysRemoved = keysRemoved;
            this.memoryFreed = memoryFreed;
            this.durationMs = durationMs;
            this.cacheTypeCleanup = new HashMap<>(cacheTypeCleanup);
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public int getKeysRemoved() {
            return keysRemoved;
        }

        public long getMemoryFreed() {
            return memoryFreed;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public Map<String, Integer> getCacheTypeCleanup() {
            return new HashMap<>(cacheTypeCleanup);
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                "CleanupResult{keysRemoved=%d, memoryFreed=%d, durationMs=%d, timestamp=%s}",
                keysRemoved,
                memoryFreed,
                durationMs,
                timestamp
            );
        }
    }

    /**
     * 优化建议
     */
    public static class OptimizationRecommendation {

        private final String type;
        private final String description;
        private final String action;
        private final int priority; // 1-5, 5 being highest priority

        public OptimizationRecommendation(
            String type,
            String description,
            String action,
            int priority
        ) {
            this.type = type;
            this.description = description;
            this.action = action;
            this.priority = priority;
        }

        // Getters
        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public String getAction() {
            return action;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return String.format(
                "OptimizationRecommendation{type='%s', priority=%d, description='%s', action='%s'}",
                type,
                priority,
                description,
                action
            );
        }
    }

    /**
     * 获取当前内存使用信息
     */
    public MemoryUsageInfo getMemoryUsageInfo() {
        try {
            // 获取Redis内存信息
            Properties info = redisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Properties>) connection -> 
                    connection.serverCommands().info("memory")
            );

            long usedMemory = Long.parseLong(
                info.getProperty("used_memory", "0")
            );
            long maxMemory = Long.parseLong(info.getProperty("maxmemory", "0"));

            // 如果没有设置maxmemory，使用系统内存
            if (maxMemory == 0) {
                maxMemory = Runtime.getRuntime().maxMemory();
            }

            double usageRatio = maxMemory > 0
                ? (double) usedMemory / maxMemory
                : 0.0;

            // 获取键数量
            long keyCount = getKeyCount();

            // 获取各类型缓存数量
            Map<String, Long> cacheTypeCounts = getCacheTypeCounts();

            return new MemoryUsageInfo(
                usedMemory,
                maxMemory,
                usageRatio,
                keyCount,
                cacheTypeCounts
            );
        } catch (Exception e) {
            logger.error("Failed to get memory usage info", e);
            return new MemoryUsageInfo(0, 0, 0.0, 0, new HashMap<>());
        }
    }

    /**
     * 获取键总数
     */
    private long getKeyCount() {
        try {
            Properties info = redisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<Properties>) connection -> 
                    connection.serverCommands().info("keyspace")
            );
            return info
                .stringPropertyNames()
                .stream()
                .filter(key -> key.startsWith("db"))
                .mapToLong(key -> {
                    String value = info.getProperty(key);
                    String[] parts = value.split(",");
                    if (parts.length > 0) {
                        String keysPart = parts[0];
                        if (keysPart.startsWith("keys=")) {
                            return Long.parseLong(keysPart.substring(5));
                        }
                    }
                    return 0;
                })
                .sum();
        } catch (Exception e) {
            logger.warn("Failed to get key count", e);
            return 0;
        }
    }

    /**
     * 获取各类型缓存数量
     */
    private Map<String, Long> getCacheTypeCounts() {
        Map<String, Long> counts = new HashMap<>();

        try {
            // 扫描不同类型的缓存键
            counts.put(
                "userPermissions",
                countKeysWithPattern("cms:permissions:user:*")
            );
            counts.put(
                "documentPermissions",
                countKeysWithPattern("cms:permissions:document:*")
            );
            counts.put(
                "documentPublic",
                countKeysWithPattern("cms:public:document:*")
            );
            counts.put(
                "documentAssignments",
                countKeysWithPattern("cms:assignments:user:*")
            );
            counts.put("statistics", countKeysWithPattern("cms:stats:*"));
            counts.put(
                "performance",
                countKeysWithPattern("cms:performance:*")
            );
        } catch (Exception e) {
            logger.warn("Failed to get cache type counts", e);
        }

        return counts;
    }

    /**
     * 计算匹配模式的键数量
     */
    private long countKeysWithPattern(String pattern) {
        try {
            ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(1000)
                .build();

            return redisTemplate.scan(options).stream().count();
        } catch (Exception e) {
            logger.warn("Failed to count keys with pattern: " + pattern, e);
            return 0;
        }
    }

    /**
     * 执行内存清理
     */
    @Async
    public CompletableFuture<CleanupResult> performCleanup() {
        if (!optimizationEnabled) {
            logger.info("Cache optimization is disabled");
            return CompletableFuture.completedFuture(
                new CleanupResult(0, 0, 0, new HashMap<>())
            );
        }

        long startTime = System.currentTimeMillis();
        long memoryBefore = getMemoryUsageInfo().getUsedMemory();

        Map<String, Integer> cacheTypeCleanup = new HashMap<>();
        int totalKeysRemoved = 0;

        try {
            logger.info("Starting cache memory cleanup");

            // 清理过期的性能统计数据
            int perfKeysRemoved = cleanupPerformanceData();
            cacheTypeCleanup.put("performance", perfKeysRemoved);
            totalKeysRemoved += perfKeysRemoved;

            // 清理低命中率的缓存项
            int lowHitKeysRemoved = cleanupLowHitRateItems();
            cacheTypeCleanup.put("lowHitRate", lowHitKeysRemoved);
            totalKeysRemoved += lowHitKeysRemoved;

            // 清理长时间未访问的缓存项
            int idleKeysRemoved = cleanupIdleItems();
            cacheTypeCleanup.put("idle", idleKeysRemoved);
            totalKeysRemoved += idleKeysRemoved;

            // 压缩内存
            compressMemory();

            long memoryAfter = getMemoryUsageInfo().getUsedMemory();
            long memoryFreed = memoryBefore - memoryAfter;
            long duration = System.currentTimeMillis() - startTime;

            CleanupResult result = new CleanupResult(
                totalKeysRemoved,
                memoryFreed,
                duration,
                cacheTypeCleanup
            );

            logger.info("Cache cleanup completed: {}", result);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("Failed to perform cache cleanup", e);
            long duration = System.currentTimeMillis() - startTime;
            return CompletableFuture.completedFuture(
                new CleanupResult(
                    totalKeysRemoved,
                    0,
                    duration,
                    cacheTypeCleanup
                )
            );
        }
    }

    /**
     * 清理过期的性能统计数据
     */
    private int cleanupPerformanceData() {
        try {
            ScanOptions options = ScanOptions.scanOptions()
                .match("cms:performance:*")
                .count(batchSize)
                .build();

            List<String> keysToDelete = new ArrayList<>();
            try (var cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    try {
                        Long ttl = redisTemplate.getExpire(
                            key,
                            TimeUnit.SECONDS
                        );
                        if (ttl != null && ttl < 0) {
                            // 没有TTL的键
                            keysToDelete.add(key);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to check TTL for key: " + key, e);
                    }
                }
            }

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                logger.info(
                    "Cleaned up {} performance data keys",
                    keysToDelete.size()
                );
            }

            return keysToDelete.size();
        } catch (Exception e) {
            logger.error("Failed to cleanup performance data", e);
            return 0;
        }
    }

    /**
     * 清理低命中率的缓存项
     */
    private int cleanupLowHitRateItems() {
        // 这里可以根据性能分析器的数据来识别低命中率的缓存项
        // 暂时返回0，实际实现需要与性能分析器集成
        return 0;
    }

    /**
     * 清理长时间未访问的缓存项
     */
    private int cleanupIdleItems() {
        try {
            int removedCount = 0;

            // 扫描所有缓存键，检查最后访问时间
            ScanOptions options = ScanOptions.scanOptions()
                .match("cms:cache:*")
                .count(batchSize)
                .build();

            List<String> keysToDelete = new ArrayList<>();
            try (var cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    try {
                        // 检查键的空闲时间
                        Long idleTime = redisTemplate.execute(
                            (org.springframework.data.redis.core.RedisCallback<
                                Long
                            >) connection -> {
                                try {
                                    // 使用Redis的OBJECT IDLETIME命令
                                    java.time.Duration duration = connection.keyCommands().idletime(key.getBytes());
                                    return duration != null ? duration.getSeconds() : null;
                                } catch (Exception e) {
                                    return null;
                                }
                            }
                        );

                        if (idleTime != null && idleTime > 3600) {
                            // 1小时未访问
                            keysToDelete.add(key);
                        }
                    } catch (Exception e) {
                        logger.warn(
                            "Failed to check idle time for key: " + key,
                            e
                        );
                    }
                }
            }

            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
                removedCount = keysToDelete.size();
                logger.info("Cleaned up {} idle cache keys", removedCount);
            }

            return removedCount;
        } catch (Exception e) {
            logger.error("Failed to cleanup idle items", e);
            return 0;
        }
    }

    /**
     * 压缩内存
     */
    private void compressMemory() {
        try {
            // 执行Redis内存压缩 - 使用BGREWRITEAOF命令
            redisTemplate.execute(
                (org.springframework.data.redis.core.RedisCallback<
                    Object
                >) connection -> {
                    connection.serverCommands().bgReWriteAof();
                    return null;
                }
            );
            logger.info(
                "Initiated background AOF rewrite for memory compression"
            );
        } catch (Exception e) {
            logger.warn("Failed to initiate memory compression", e);
        }
    }

    /**
     * 获取优化建议
     */
    public List<OptimizationRecommendation> getOptimizationRecommendations() {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();

        MemoryUsageInfo memoryInfo = getMemoryUsageInfo();

        // 内存使用率建议
        if (memoryInfo.getUsageRatio() > 0.9) {
            recommendations.add(
                new OptimizationRecommendation(
                    "MEMORY_CRITICAL",
                    "Memory usage is critically high (" +
                        String.format(
                            "%.1f%%",
                            memoryInfo.getUsageRatio() * 100
                        ) +
                        ")",
                    "Immediate cleanup required. Consider reducing cache TTL or increasing memory limit.",
                    5
                )
            );
        } else if (memoryInfo.getUsageRatio() > 0.8) {
            recommendations.add(
                new OptimizationRecommendation(
                    "MEMORY_WARNING",
                    "Memory usage is high (" +
                        String.format(
                            "%.1f%%",
                            memoryInfo.getUsageRatio() * 100
                        ) +
                        ")",
                    "Schedule cleanup or monitor closely. Consider optimizing cache policies.",
                    3
                )
            );
        }

        // 键数量建议
        if (memoryInfo.getKeyCount() > 100000) {
            recommendations.add(
                new OptimizationRecommendation(
                    "KEY_COUNT_HIGH",
                    "High number of cache keys (" +
                        memoryInfo.getKeyCount() +
                        ")",
                    "Consider implementing key expiration policies or data partitioning.",
                    2
                )
            );
        }

        // 缓存类型分布建议
        Map<String, Long> typeCounts = memoryInfo.getCacheTypeCounts();
        long totalKeys = typeCounts
            .values()
            .stream()
            .mapToLong(Long::longValue)
            .sum();

        if (totalKeys > 0) {
            typeCounts.forEach((type, count) -> {
                double percentage = (double) count / totalKeys;
                if (percentage > 0.5) {
                    recommendations.add(
                        new OptimizationRecommendation(
                            "CACHE_IMBALANCE",
                            type +
                                " cache dominates (" +
                                String.format("%.1f%%", percentage * 100) +
                                " of total keys)",
                            "Consider optimizing " +
                                type +
                                " cache policies or implementing data archiving.",
                            2
                        )
                    );
                }
            });
        }

        // 性能建议
        try {
            CachePerformanceAnalyzer.PerformanceReport report =
                performanceAnalyzer.getPerformanceReport();

            if (report.getHitRate() < 70.0) {
                recommendations.add(
                    new OptimizationRecommendation(
                        "LOW_HIT_RATE",
                        "Overall cache hit rate is low (" +
                            String.format("%.1f%%", report.getHitRate()) +
                            ")",
                        "Review cache warming strategies and access patterns.",
                        4
                    )
                );
            }

            if (report.getAvgResponseTime() > 50) {
                recommendations.add(
                    new OptimizationRecommendation(
                        "HIGH_RESPONSE_TIME",
                        "Average response time is high (" +
                            String.format("%.1f", report.getAvgResponseTime()) +
                            "ms)",
                        "Check network latency and Redis configuration. Consider connection pooling optimization.",
                        3
                    )
                );
            }
        } catch (Exception e) {
            logger.warn(
                "Failed to get performance summary for recommendations",
                e
            );
        }

        // 按优先级排序
        recommendations.sort((a, b) ->
            Integer.compare(b.getPriority(), a.getPriority())
        );

        return recommendations;
    }

    /**
     * 定期内存监控和清理
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void scheduledMemoryCheck() {
        if (!optimizationEnabled) {
            return;
        }

        try {
            MemoryUsageInfo memoryInfo = getMemoryUsageInfo();
            logger.debug("Memory usage check: {}", memoryInfo);

            if (memoryInfo.getUsageRatio() > cleanupThreshold) {
                logger.warn(
                    "Memory usage exceeds cleanup threshold ({}%), initiating cleanup",
                    cleanupThreshold * 100
                );
                performCleanup();
            }
        } catch (Exception e) {
            logger.error("Failed to perform scheduled memory check", e);
        }
    }

    /**
     * 优化缓存TTL
     */
    public void optimizeCacheTtl(String cacheType, double hitRate) {
        if (!optimizationEnabled) {
            return;
        }

        try {
            // 根据命中率调整TTL
            if (hitRate > 0.9) {
                // 高命中率，可以延长TTL
                logger.info(
                    "High hit rate for {}, considering TTL extension",
                    cacheType
                );
            } else if (hitRate < 0.5) {
                // 低命中率，缩短TTL
                logger.info(
                    "Low hit rate for {}, considering TTL reduction",
                    cacheType
                );
            }
        } catch (Exception e) {
            logger.error("Failed to optimize cache TTL for " + cacheType, e);
        }
    }

    /**
     * 获取内存优化统计
     */
    public Map<String, Object> getOptimizationStats() {
        Map<String, Object> stats = new HashMap<>();

        MemoryUsageInfo memoryInfo = getMemoryUsageInfo();
        stats.put("memoryUsage", memoryInfo);
        stats.put("recommendations", getOptimizationRecommendations());
        stats.put("optimizationEnabled", optimizationEnabled);
        stats.put("lastCheckTime", LocalDateTime.now());

        return stats;
    }
}
