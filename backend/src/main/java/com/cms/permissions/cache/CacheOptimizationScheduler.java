package com.cms.permissions.cache;

import com.cms.permissions.service.CacheHealthService;
import com.cms.permissions.service.CachePerformanceAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

/**
 * 缓存优化调度器
 * 定期执行缓存优化任务
 */
@Component
@ConditionalOnProperty(prefix = "cache.optimization", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CacheOptimizationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CacheOptimizationScheduler.class);

    private final CacheMemoryOptimizer memoryOptimizer;
    private final CacheWarmupStrategy warmupStrategy;
    private final CacheHealthService healthService;
    private final CachePerformanceAnalyzer performanceAnalyzer;

    public CacheOptimizationScheduler(CacheMemoryOptimizer memoryOptimizer,
                                    CacheWarmupStrategy warmupStrategy,
                                    CacheHealthService healthService,
                                    CachePerformanceAnalyzer performanceAnalyzer) {
        this.memoryOptimizer = memoryOptimizer;
        this.warmupStrategy = warmupStrategy;
        this.healthService = healthService;
        this.performanceAnalyzer = performanceAnalyzer;
    }

    /**
     * 每5分钟检查内存使用情况
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkMemoryUsage() {
        try {
            logger.debug("开始检查缓存内存使用情况");
            
            CacheMemoryOptimizer.MemoryUsageInfo memoryInfo = memoryOptimizer.getMemoryUsageInfo();
            
            if (memoryInfo.getUsageRatio() > 0.9) {
                logger.warn("缓存内存使用率过高: {}%", memoryInfo.getUsageRatio() * 100);
                
                // 异步执行内存清理
                CompletableFuture.runAsync(() -> {
                    try {
                        memoryOptimizer.performCleanup().thenAccept(result -> {
                            logger.info("内存清理完成: 清理了{}个条目，释放了{}字节内存", 
                                      result.getKeysRemoved(), result.getMemoryFreed());
                        });
                    } catch (Exception e) {
                        logger.error("内存清理失败", e);
                    }
                });
            }
            
        } catch (Exception e) {
            logger.error("检查内存使用情况时发生错误", e);
        }
    }

    /**
     * 每小时执行智能预热
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void performSmartWarmup() {
        try {
            logger.debug("开始执行智能缓存预热");
            
            // 检查当前时间是否为高峰时段
            LocalTime now = LocalTime.now();
            int hour = now.getHour();
            
            if (isBusinessHour(hour)) {
                CompletableFuture.runAsync(() -> {
                    try {
                        warmupStrategy.executeSmartWarmup().thenAccept(result -> {
                            logger.info("智能预热完成: 策略={}, 预热了{}个条目", 
                                      result.getStrategy(), result.getItemsWarmed().values().stream().mapToInt(Integer::intValue).sum());
                        });
                    } catch (Exception e) {
                        logger.error("智能预热失败", e);
                    }
                });
            }
            
        } catch (Exception e) {
            logger.error("执行智能预热时发生错误", e);
        }
    }

    /**
     * 每30分钟检查缓存健康状况
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void checkCacheHealth() {
        try {
            logger.debug("开始检查缓存健康状况");
            
            var healthStatus = healthService.checkCacheHealth();
            
            if (!healthStatus.isHealthy()) {
                StringBuilder issues = new StringBuilder();
                
                if (!healthStatus.isRedisConnected()) {
                    issues.append("Redis连接异常; ");
                }
                
                if (healthStatus.getCachePerformance() != null && !healthStatus.getCachePerformance().isHealthy()) {
                    issues.append("缓存性能问题(命中率: ").append(healthStatus.getCachePerformance().getHitRate())
                           .append("%, 响应时间: ").append(healthStatus.getCachePerformance().getAverageResponseTime())
                           .append("ms); ");
                }
                
                if (healthStatus.getCacheSize() != null && !healthStatus.getCacheSize().isHealthy()) {
                    issues.append("缓存大小问题; ");
                }
                
                if (healthStatus.getCacheConfiguration() != null && !healthStatus.getCacheConfiguration().isHealthy()) {
                    issues.append("缓存配置问题; ");
                }
                
                if (healthStatus.getErrorMessage() != null) {
                    issues.append("错误信息: ").append(healthStatus.getErrorMessage());
                }
                
                logger.warn("缓存健康检查发现问题: {}", issues.toString());
                
                // 如果是性能问题，触发优化
                if (healthStatus.getCachePerformance() != null && !healthStatus.getCachePerformance().isHealthy()) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            warmupStrategy.executeSmartWarmup();
                            logger.info("由于性能问题触发了智能预热");
                        } catch (Exception e) {
                            logger.error("健康检查触发的预热失败", e);
                        }
                    });
                }
            }
            
        } catch (Exception e) {
            logger.error("检查缓存健康状况时发生错误", e);
        }
    }

    /**
     * 每天凌晨2点执行深度优化
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void performDeepOptimization() {
        try {
            logger.info("开始执行深度缓存优化");
            
            // 1. 内存清理和优化
            CompletableFuture<Void> memoryTask = CompletableFuture.runAsync(() -> {
                try {
                    memoryOptimizer.performCleanup();
                    logger.info("内存清理和优化完成");
                } catch (Exception e) {
                    logger.error("内存清理和优化失败", e);
                }
            });

            // 2. 性能数据清理
            CompletableFuture<Void> performanceTask = CompletableFuture.runAsync(() -> {
                try {
                    performanceAnalyzer.resetStatistics();
                    logger.info("性能数据重置完成");
                } catch (Exception e) {
                    logger.error("性能数据重置失败", e);
                }
            });

            // 3. 访问模式重置
            CompletableFuture<Void> patternTask = CompletableFuture.runAsync(() -> {
                try {
                    warmupStrategy.resetAccessPatterns();
                    logger.info("访问模式重置完成");
                } catch (Exception e) {
                    logger.error("访问模式重置失败", e);
                }
            });

            // 等待所有任务完成
            CompletableFuture.allOf(memoryTask, performanceTask, patternTask)
                    .thenRun(() -> logger.info("深度缓存优化完成"))
                    .exceptionally(throwable -> {
                        logger.error("深度缓存优化过程中发生错误", throwable);
                        return null;
                    });
            
        } catch (Exception e) {
            logger.error("执行深度缓存优化时发生错误", e);
        }
    }

    /**
     * 每周日凌晨3点生成优化报告
     */
    @Scheduled(cron = "0 0 3 ? * SUN") // 每周日凌晨3点
    public void generateOptimizationReport() {
        try {
            logger.info("开始生成缓存优化报告");
            
            // 获取性能报告
            var performanceReport = performanceAnalyzer.getPerformanceReport();
            
            // 获取内存优化建议
            var recommendations = memoryOptimizer.getOptimizationRecommendations();
            
            // 获取预热策略统计
            var strategyStats = warmupStrategy.getStrategyStats();
            
            logger.info("=== 缓存优化周报 ===");
            logger.info("性能报告: {}", performanceReport);
            logger.info("优化建议: {}", recommendations);
            logger.info("预热策略统计: {}", strategyStats);
            logger.info("==================");
            
        } catch (Exception e) {
            logger.error("生成缓存优化报告时发生错误", e);
        }
    }

    /**
     * 判断是否为工作时间
     */
    private boolean isBusinessHour(int hour) {
        return hour >= 9 && hour <= 18;
    }
}