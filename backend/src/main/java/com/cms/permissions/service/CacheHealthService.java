package com.cms.permissions.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 缓存健康检查服务
 */
@Service
public class CacheHealthService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private CachePerformanceAnalyzer performanceAnalyzer;

    /**
     * 检查缓存健康状态
     */
    public CacheHealthStatus checkCacheHealth() {
        CacheHealthStatus status = new CacheHealthStatus();

        try {
            // 检查Redis连接
            status.setRedisConnected(checkRedisConnection());

            // 检查缓存性能
            status.setCachePerformance(checkCachePerformance());

            // 检查缓存大小
            status.setCacheSize(checkCacheSize());

            // 检查缓存配置
            status.setCacheConfiguration(checkCacheConfiguration());

            // 设置整体健康状态
            status.setHealthy(
                status.isRedisConnected() &&
                    status.getCachePerformance().isHealthy() &&
                    status.getCacheSize().isHealthy()
            );

            status.setLastCheckTime(LocalDateTime.now());
        } catch (Exception e) {
            status.setHealthy(false);
            status.setErrorMessage(e.getMessage());
        }

        return status;
    }

    /**
     * 检查Redis连接
     */
    private boolean checkRedisConnection() {
        try {
            redisTemplate.opsForValue().set("health:check", "test", 1);
            String result = (String) redisTemplate
                .opsForValue()
                .get("health:check");
            redisTemplate.delete("health:check");
            return "test".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查缓存性能
     */
    private CachePerformanceHealth checkCachePerformance() {
        CachePerformanceHealth performance = new CachePerformanceHealth();

        try {
            var report = performanceAnalyzer.getPerformanceReport();

            // 检查命中率
            double hitRate = report.getHitRate();

            performance.setHitRate(hitRate);
            performance.setHealthy(hitRate >= 70.0); // 命中率低于70%认为不健康

            // 检查平均响应时间
            double avgResponseTime = report.getAvgResponseTime();

            performance.setAverageResponseTime(avgResponseTime);
            performance.setHealthy(
                performance.isHealthy() && avgResponseTime <= 100
            ); // 响应时间超过100ms认为不健康
        } catch (Exception e) {
            performance.setHealthy(false);
            performance.setErrorMessage(e.getMessage());
        }

        return performance;
    }

    /**
     * 检查缓存大小
     */
    private CacheSizeHealth checkCacheSize() {
        CacheSizeHealth sizeHealth = new CacheSizeHealth();

        try {
            var stats = permissionCacheService.getCacheStats();

            int totalSize =
                stats.getUserPermissionsCacheSize() +
                stats.getDocumentPermissionsCacheSize() +
                stats.getDocumentPublicCacheSize();

            sizeHealth.setTotalCacheSize(totalSize);
            sizeHealth.setHealthy(totalSize < 10000); // 缓存条目超过10000认为可能有问题

            if (totalSize >= 10000) {
                sizeHealth.setWarningMessage(
                    "缓存大小可能过大，建议检查缓存策略"
                );
            }
        } catch (Exception e) {
            sizeHealth.setHealthy(false);
            sizeHealth.setErrorMessage(e.getMessage());
        }

        return sizeHealth;
    }

    /**
     * 检查缓存配置
     */
    private CacheConfigurationHealth checkCacheConfiguration() {
        CacheConfigurationHealth configHealth = new CacheConfigurationHealth();

        try {
            // 检查TTL配置
            configHealth.setTtlConfigured(true); // 假设TTL已配置
            configHealth.setHealthy(true);
        } catch (Exception e) {
            configHealth.setHealthy(false);
            configHealth.setErrorMessage(e.getMessage());
        }

        return configHealth;
    }

    /**
     * 缓存健康状态
     */
    public static class CacheHealthStatus {

        private boolean healthy;
        private boolean redisConnected;
        private CachePerformanceHealth cachePerformance;
        private CacheSizeHealth cacheSize;
        private CacheConfigurationHealth cacheConfiguration;
        private LocalDateTime lastCheckTime;
        private String errorMessage;

        // Getters and Setters
        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public boolean isRedisConnected() {
            return redisConnected;
        }

        public void setRedisConnected(boolean redisConnected) {
            this.redisConnected = redisConnected;
        }

        public CachePerformanceHealth getCachePerformance() {
            return cachePerformance;
        }

        public void setCachePerformance(
            CachePerformanceHealth cachePerformance
        ) {
            this.cachePerformance = cachePerformance;
        }

        public CacheSizeHealth getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(CacheSizeHealth cacheSize) {
            this.cacheSize = cacheSize;
        }

        public CacheConfigurationHealth getCacheConfiguration() {
            return cacheConfiguration;
        }

        public void setCacheConfiguration(
            CacheConfigurationHealth cacheConfiguration
        ) {
            this.cacheConfiguration = cacheConfiguration;
        }

        public LocalDateTime getLastCheckTime() {
            return lastCheckTime;
        }

        public void setLastCheckTime(LocalDateTime lastCheckTime) {
            this.lastCheckTime = lastCheckTime;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 缓存性能健康状态
     */
    public static class CachePerformanceHealth {

        private boolean healthy;
        private double hitRate;
        private double averageResponseTime;
        private String errorMessage;

        // Getters and Setters
        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public double getHitRate() {
            return hitRate;
        }

        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }

        public double getAverageResponseTime() {
            return averageResponseTime;
        }

        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 缓存大小健康状态
     */
    public static class CacheSizeHealth {

        private boolean healthy;
        private int totalCacheSize;
        private String warningMessage;
        private String errorMessage;

        // Getters and Setters
        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public int getTotalCacheSize() {
            return totalCacheSize;
        }

        public void setTotalCacheSize(int totalCacheSize) {
            this.totalCacheSize = totalCacheSize;
        }

        public String getWarningMessage() {
            return warningMessage;
        }

        public void setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 缓存配置健康状态
     */
    public static class CacheConfigurationHealth {

        private boolean healthy;
        private boolean ttlConfigured;
        private String errorMessage;

        // Getters and Setters
        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public boolean isTtlConfigured() {
            return ttlConfigured;
        }

        public void setTtlConfigured(boolean ttlConfigured) {
            this.ttlConfigured = ttlConfigured;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
