package com.cms.permissions.cache;

import com.cms.permissions.service.CacheWarmupService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 缓存预热策略优化器
 * 基于访问模式和性能数据智能优化缓存预热策略
 */
@Service
public class CacheWarmupStrategy {

    private static final Logger logger = LoggerFactory.getLogger(
        CacheWarmupStrategy.class
    );

    @Autowired
    private CacheWarmupService cacheWarmupService;

    private CacheMemoryOptimizer memoryOptimizer;

    @Value("${cache.warmup.strategy.enabled:true}")
    private boolean strategyEnabled;

    @Value("${cache.warmup.strategy.peak-hours:9,10,11,14,15,16}")
    private List<Integer> peakHours;

    @Value("${cache.warmup.strategy.min-hit-rate:0.7}")
    private double minHitRate;

    @Value("${cache.warmup.strategy.max-memory-usage:0.8}")
    private double maxMemoryUsage;

    @Value("${cache.warmup.strategy.batch-size:500}")
    private int batchSize;

    // 访问模式统计
    private final Map<String, AccessPattern> accessPatterns =
        new ConcurrentHashMap<>();

    // 预热历史记录
    private final List<WarmupExecution> warmupHistory = new ArrayList<>();

    /**
     * 访问模式
     */
    public static class AccessPattern {

        private final String cacheType;
        private final Map<Integer, Integer> hourlyAccess; // 小时 -> 访问次数
        private final Map<String, Integer> keyAccess; // 键 -> 访问次数
        private double averageHitRate;
        private LocalDateTime lastUpdated;

        public AccessPattern(String cacheType) {
            this.cacheType = cacheType;
            this.hourlyAccess = new ConcurrentHashMap<>();
            this.keyAccess = new ConcurrentHashMap<>();
            this.averageHitRate = 0.0;
            this.lastUpdated = LocalDateTime.now();
        }

        public void recordAccess(String key, int hour) {
            hourlyAccess.merge(hour, 1, Integer::sum);
            keyAccess.merge(key, 1, Integer::sum);
            lastUpdated = LocalDateTime.now();
        }

        public void updateHitRate(double hitRate) {
            this.averageHitRate = hitRate;
            this.lastUpdated = LocalDateTime.now();
        }

        // Getters
        public String getCacheType() {
            return cacheType;
        }

        public Map<Integer, Integer> getHourlyAccess() {
            return new HashMap<>(hourlyAccess);
        }

        public Map<String, Integer> getKeyAccess() {
            return new HashMap<>(keyAccess);
        }

        public double getAverageHitRate() {
            return averageHitRate;
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        public List<Integer> getPeakHours() {
            return hourlyAccess
                .entrySet()
                .stream()
                .sorted(
                    Map.Entry.<Integer, Integer>comparingByValue().reversed()
                )
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }

        public List<String> getPopularKeys(int limit) {
            return keyAccess
                .entrySet()
                .stream()
                .sorted(
                    Map.Entry.<String, Integer>comparingByValue().reversed()
                )
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
    }

    /**
     * 预热执行记录
     */
    public static class WarmupExecution {

        private final String strategy;
        private final LocalDateTime executionTime;
        private final Map<String, Integer> itemsWarmed;
        private final long durationMs;
        private final boolean successful;
        private final String reason;

        public WarmupExecution(
            String strategy,
            Map<String, Integer> itemsWarmed,
            long durationMs,
            boolean successful,
            String reason
        ) {
            this.strategy = strategy;
            this.executionTime = LocalDateTime.now();
            this.itemsWarmed = new HashMap<>(itemsWarmed);
            this.durationMs = durationMs;
            this.successful = successful;
            this.reason = reason;
        }

        // Getters
        public String getStrategy() {
            return strategy;
        }

        public LocalDateTime getExecutionTime() {
            return executionTime;
        }

        public Map<String, Integer> getItemsWarmed() {
            return new HashMap<>(itemsWarmed);
        }

        public long getDurationMs() {
            return durationMs;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return String.format(
                "WarmupExecution{strategy='%s', successful=%s, durationMs=%d, itemsWarmed=%d, time=%s}",
                strategy,
                successful,
                durationMs,
                itemsWarmed.values().stream().mapToInt(Integer::intValue).sum(),
                executionTime
            );
        }
    }

    /**
     * 预热策略
     */
    public enum WarmupStrategyType {
        PEAK_HOUR_PREPARATION, // 高峰期准备
        LOW_HIT_RATE_RECOVERY, // 低命中率恢复
        MEMORY_PRESSURE_RELIEF, // 内存压力缓解
        SCHEDULED_MAINTENANCE, // 定期维护
        ADAPTIVE_LEARNING, // 自适应学习
    }

    /**
     * 记录缓存访问
     */
    public void recordCacheAccess(String cacheType, String key) {
        if (!strategyEnabled) {
            return;
        }

        try {
            int currentHour = LocalTime.now().getHour();
            AccessPattern pattern = accessPatterns.computeIfAbsent(
                cacheType,
                AccessPattern::new
            );
            pattern.recordAccess(key, currentHour);
        } catch (Exception e) {
            logger.warn(
                "Failed to record cache access for {}: {}",
                cacheType,
                key,
                e
            );
        }
    }

    /**
     * 更新缓存命中率
     */
    public void updateCacheHitRate(String cacheType, double hitRate) {
        if (!strategyEnabled) {
            return;
        }

        try {
            AccessPattern pattern = accessPatterns.get(cacheType);
            if (pattern != null) {
                pattern.updateHitRate(hitRate);
            }
        } catch (Exception e) {
            logger.warn("Failed to update hit rate for {}", cacheType, e);
        }
    }

    /**
     * 执行智能预热
     */
    @Async
    public CompletableFuture<WarmupExecution> executeSmartWarmup() {
        if (!strategyEnabled) {
            return CompletableFuture.completedFuture(
                new WarmupExecution(
                    "DISABLED",
                    new HashMap<>(),
                    0,
                    false,
                    "Strategy disabled"
                )
            );
        }

        long startTime = System.currentTimeMillis();

        try {
            WarmupStrategyType strategy = determineOptimalStrategy();
            Map<String, Integer> itemsWarmed = new HashMap<>();

            logger.info("Executing smart warmup with strategy: {}", strategy);

            switch (strategy) {
                case PEAK_HOUR_PREPARATION:
                    itemsWarmed = executePeakHourPreparation();
                    break;
                case LOW_HIT_RATE_RECOVERY:
                    itemsWarmed = executeLowHitRateRecovery();
                    break;
                case MEMORY_PRESSURE_RELIEF:
                    itemsWarmed = executeMemoryPressureRelief();
                    break;
                case SCHEDULED_MAINTENANCE:
                    itemsWarmed = executeScheduledMaintenance();
                    break;
                case ADAPTIVE_LEARNING:
                    itemsWarmed = executeAdaptiveLearning();
                    break;
            }

            long duration = System.currentTimeMillis() - startTime;
            WarmupExecution execution = new WarmupExecution(
                strategy.name(),
                itemsWarmed,
                duration,
                true,
                "Completed successfully"
            );

            recordWarmupExecution(execution);
            logger.info("Smart warmup completed: {}", execution);

            return CompletableFuture.completedFuture(execution);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            WarmupExecution execution = new WarmupExecution(
                "ERROR",
                new HashMap<>(),
                duration,
                false,
                e.getMessage()
            );

            recordWarmupExecution(execution);
            logger.error("Smart warmup failed", e);

            return CompletableFuture.completedFuture(execution);
        }
    }

    /**
     * 确定最优预热策略
     */
    private WarmupStrategyType determineOptimalStrategy() {
        int currentHour = LocalTime.now().getHour();

        // 检查内存压力
        CacheMemoryOptimizer.MemoryUsageInfo memoryInfo =
            memoryOptimizer.getMemoryUsageInfo();
        if (memoryInfo.getUsageRatio() > maxMemoryUsage) {
            return WarmupStrategyType.MEMORY_PRESSURE_RELIEF;
        }

        // 检查是否接近高峰期
        boolean nearPeakHour =
            peakHours.contains(currentHour) ||
            peakHours.contains((currentHour + 1) % 24);
        if (nearPeakHour) {
            return WarmupStrategyType.PEAK_HOUR_PREPARATION;
        }

        // 检查命中率
        boolean hasLowHitRate = accessPatterns
            .values()
            .stream()
            .anyMatch(pattern -> pattern.getAverageHitRate() < minHitRate);
        if (hasLowHitRate) {
            return WarmupStrategyType.LOW_HIT_RATE_RECOVERY;
        }

        // 检查是否需要自适应学习
        if (shouldPerformAdaptiveLearning()) {
            return WarmupStrategyType.ADAPTIVE_LEARNING;
        }

        return WarmupStrategyType.SCHEDULED_MAINTENANCE;
    }

    /**
     * 执行高峰期准备预热
     */
    private Map<String, Integer> executePeakHourPreparation() {
        Map<String, Integer> itemsWarmed = new HashMap<>();

        try {
            // 预热用户权限缓存
            CacheWarmupService.WarmupResult userResult = cacheWarmupService
                .warmupUserPermissions()
                .get();
            itemsWarmed.put("userPermissions", userResult.getSuccessCount());

            // 预热热门文档
            CacheWarmupService.WarmupResult docResult = cacheWarmupService
                .warmupDocumentPublicStatus()
                .get();
            itemsWarmed.put("documentPublic", docResult.getSuccessCount());

            logger.info("Peak hour preparation completed: {}", itemsWarmed);
        } catch (Exception e) {
            logger.error("Failed to execute peak hour preparation", e);
        }

        return itemsWarmed;
    }

    /**
     * 执行低命中率恢复预热
     */
    private Map<String, Integer> executeLowHitRateRecovery() {
        Map<String, Integer> itemsWarmed = new HashMap<>();

        try {
            // 找出命中率最低的缓存类型
            String lowestHitRateCache = accessPatterns
                .entrySet()
                .stream()
                .min(
                    Map.Entry.comparingByValue((p1, p2) ->
                        Double.compare(
                            p1.getAverageHitRate(),
                            p2.getAverageHitRate()
                        )
                    )
                )
                .map(Map.Entry::getKey)
                .orElse("userPermissions");

            // 针对性预热
            if ("userPermissions".equals(lowestHitRateCache)) {
                CacheWarmupService.WarmupResult result = cacheWarmupService
                    .warmupUserPermissions()
                    .get();
                itemsWarmed.put("userPermissions", result.getSuccessCount());
            } else if ("documentPublic".equals(lowestHitRateCache)) {
                CacheWarmupService.WarmupResult result = cacheWarmupService
                    .warmupDocumentPublicStatus()
                    .get();
                itemsWarmed.put("documentPublic", result.getSuccessCount());
            } else if ("documentAssignments".equals(lowestHitRateCache)) {
                CacheWarmupService.WarmupResult result = cacheWarmupService
                    .warmupPopularDocumentAssignments()
                    .get();
                itemsWarmed.put(
                    "documentAssignments",
                    result.getSuccessCount()
                );
            }

            logger.info(
                "Low hit rate recovery completed for {}: {}",
                lowestHitRateCache,
                itemsWarmed
            );
        } catch (Exception e) {
            logger.error("Failed to execute low hit rate recovery", e);
        }

        return itemsWarmed;
    }

    /**
     * 执行内存压力缓解预热
     */
    private Map<String, Integer> executeMemoryPressureRelief() {
        Map<String, Integer> itemsWarmed = new HashMap<>();

        try {
            // 在内存压力下，只预热最关键的缓存
            CacheWarmupService.WarmupResult userResult = cacheWarmupService
                .warmupUserPermissions()
                .get();
            itemsWarmed.put(
                "userPermissions",
                Math.min(userResult.getSuccessCount(), batchSize / 2)
            );

            logger.info(
                "Memory pressure relief warmup completed: {}",
                itemsWarmed
            );
        } catch (Exception e) {
            logger.error("Failed to execute memory pressure relief", e);
        }

        return itemsWarmed;
    }

    /**
     * 执行定期维护预热
     */
    private Map<String, Integer> executeScheduledMaintenance() {
        Map<String, Integer> itemsWarmed = new HashMap<>();

        try {
            // 全面但温和的预热
            CacheWarmupService.CompleteWarmupResult result = cacheWarmupService
                .performCompleteWarmup()
                .get();
            itemsWarmed.put(
                "userPermissions",
                result.getUserPermissionsResult().getSuccessCount()
            );
            itemsWarmed.put(
                "documentPublic",
                result.getDocumentPublicResult().getSuccessCount()
            );
            itemsWarmed.put(
                "documentAssignments",
                result.getDocumentAssignmentsResult().getSuccessCount()
            );

            logger.info(
                "Scheduled maintenance warmup completed: {}",
                itemsWarmed
            );
        } catch (Exception e) {
            logger.error("Failed to execute scheduled maintenance", e);
        }

        return itemsWarmed;
    }

    /**
     * 执行自适应学习预热
     */
    private Map<String, Integer> executeAdaptiveLearning() {
        Map<String, Integer> itemsWarmed = new HashMap<>();

        try {
            // 基于访问模式进行智能预热
            for (AccessPattern pattern : accessPatterns.values()) {
                List<String> popularKeys = pattern.getPopularKeys(batchSize);

                if (
                    "userPermissions".equals(pattern.getCacheType()) &&
                    !popularKeys.isEmpty()
                ) {
                    // 这里可以实现基于热门键的选择性预热
                    CacheWarmupService.WarmupResult result = cacheWarmupService
                        .warmupUserPermissions()
                        .get();
                    itemsWarmed.put(
                        "userPermissions",
                        Math.min(result.getSuccessCount(), popularKeys.size())
                    );
                }
            }

            logger.info("Adaptive learning warmup completed: {}", itemsWarmed);
        } catch (Exception e) {
            logger.error("Failed to execute adaptive learning", e);
        }

        return itemsWarmed;
    }

    /**
     * 判断是否需要自适应学习
     */
    private boolean shouldPerformAdaptiveLearning() {
        // 如果有足够的访问模式数据，且最近没有执行过自适应学习
        long recentAdaptiveExecutions = warmupHistory
            .stream()
            .filter(exec -> exec.getStrategy().equals("ADAPTIVE_LEARNING"))
            .filter(exec ->
                exec
                    .getExecutionTime()
                    .isAfter(LocalDateTime.now().minusHours(6))
            )
            .count();

        return accessPatterns.size() >= 2 && recentAdaptiveExecutions == 0;
    }

    /**
     * 记录预热执行
     */
    private void recordWarmupExecution(WarmupExecution execution) {
        synchronized (warmupHistory) {
            warmupHistory.add(execution);

            // 保持历史记录在合理范围内
            if (warmupHistory.size() > 100) {
                warmupHistory.remove(0);
            }
        }
    }

    /**
     * 定期智能预热
     */
    @Scheduled(fixedRate = 1800000) // 每30分钟执行一次
    public void scheduledSmartWarmup() {
        if (!strategyEnabled) {
            return;
        }

        try {
            logger.debug("Starting scheduled smart warmup");
            executeSmartWarmup();
        } catch (Exception e) {
            logger.error("Failed to execute scheduled smart warmup", e);
        }
    }

    /**
     * 获取访问模式统计
     */
    public Map<String, AccessPattern> getAccessPatterns() {
        return new HashMap<>(accessPatterns);
    }

    /**
     * 获取预热历史
     */
    public List<WarmupExecution> getWarmupHistory() {
        synchronized (warmupHistory) {
            return new ArrayList<>(warmupHistory);
        }
    }

    /**
     * 获取策略统计
     */
    public Map<String, Object> getStrategyStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("strategyEnabled", strategyEnabled);
        stats.put("accessPatterns", getAccessPatterns());
        stats.put("warmupHistory", getWarmupHistory());
        stats.put("peakHours", peakHours);
        stats.put("minHitRate", minHitRate);
        stats.put("maxMemoryUsage", maxMemoryUsage);

        // 统计各策略执行次数
        Map<String, Long> strategyExecutions = warmupHistory
            .stream()
            .collect(
                Collectors.groupingBy(
                    WarmupExecution::getStrategy,
                    Collectors.counting()
                )
            );
        stats.put("strategyExecutions", strategyExecutions);

        // 成功率统计
        long totalExecutions = warmupHistory.size();
        long successfulExecutions = warmupHistory
            .stream()
            .mapToLong(exec -> exec.isSuccessful() ? 1 : 0)
            .sum();
        double successRate = totalExecutions > 0
            ? (double) successfulExecutions / totalExecutions
            : 0.0;
        stats.put("successRate", successRate);

        return stats;
    }

    /**
     * 重置访问模式
     */
    public void resetAccessPatterns() {
        accessPatterns.clear();
        logger.info("Access patterns reset");
    }

    /**
     * 清理预热历史
     */
    public void clearWarmupHistory() {
        synchronized (warmupHistory) {
            warmupHistory.clear();
        }
        logger.info("Warmup history cleared");
    }
}
