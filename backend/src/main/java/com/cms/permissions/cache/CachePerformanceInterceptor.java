package com.cms.permissions.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheInterceptor;

/**
 * 缓存性能拦截器
 * 自动记录缓存操作的性能指标
 */
//@Component  // 暂时注释掉，避免自动注册导致的问题
@ConditionalOnBean(CacheOptimizationConfig.CachePerformanceMonitor.class)
public class CachePerformanceInterceptor extends CacheInterceptor {

    private final CacheOptimizationConfig.CachePerformanceMonitor performanceMonitor;

    public CachePerformanceInterceptor(
        CacheOptimizationConfig.CachePerformanceMonitor performanceMonitor
    ) {
        this.performanceMonitor = performanceMonitor;
    }

    @Override
    protected Cache.ValueWrapper doGet(Cache cache, Object key) {
        long startTime = System.currentTimeMillis();

        try {
            Cache.ValueWrapper result = super.doGet(cache, key);
            long duration = System.currentTimeMillis() - startTime;

            if (performanceMonitor != null) {
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    result != null,
                    duration
                );
            }
            return result;
        } catch (Exception e) {
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Assume miss/error
                    duration
                );
            }
            throw e;
        }
    }

    @Override
    protected void doPut(Cache cache, Object key, Object result) {
        long startTime = System.currentTimeMillis();

        try {
            super.doPut(cache, key, result);
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    true, // Assume hit for put operations
                    duration
                );
            }
        } catch (Exception e) {
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Assume miss/error for failed put operations
                    duration
                );
            }
            throw e;
        }
    }

    @Override
    protected void doEvict(Cache cache, Object key, boolean immediate) {
        long startTime = System.currentTimeMillis();

        try {
            super.doEvict(cache, key, immediate);
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                // Record as a cache operation with eviction flag
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Not a hit/miss operation
                    duration
                );
            }
        } catch (Exception e) {
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Not a hit/miss operation
                    duration
                );
            }
            throw e;
        }
    }

    @Override
    protected void doClear(Cache cache, boolean immediate) {
        long startTime = System.currentTimeMillis();

        try {
            super.doClear(cache, immediate);
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Not a hit/miss operation
                    duration
                );
            }
        } catch (Exception e) {
            if (performanceMonitor != null) {
                long duration = System.currentTimeMillis() - startTime;
                performanceMonitor.recordCacheOperation(
                    cache.getName(),
                    false, // Not a hit/miss operation
                    duration
                );
            }
            throw e;
        }
    }
}
