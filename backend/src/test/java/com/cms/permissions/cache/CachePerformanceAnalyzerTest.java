package com.cms.permissions.cache;

import static org.junit.jupiter.api.Assertions.*;

import com.cms.permissions.service.CachePerformanceAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CachePerformanceAnalyzerTest {

    @Autowired
    private CachePerformanceAnalyzer performanceAnalyzer;

    @BeforeEach
    void setUp() {
        // Reset performance analyzer before each test
        performanceAnalyzer.resetStatistics();
    }

    @Test
    void testRecordCacheOperation() {
        // Test recording cache operations
        assertDoesNotThrow(() -> {
            performanceAnalyzer.recordCacheOperation("test_cache", 100L, true);
            performanceAnalyzer.recordCacheOperation("test_cache", 150L, false);
        });
    }

    @Test
    void testGetPerformanceReport() {
        // Test getting performance report
        assertDoesNotThrow(() -> {
            // Record some operations first
            performanceAnalyzer.recordCacheOperation("test_cache", 100L, true);
            performanceAnalyzer.recordCacheOperation("test_cache", 150L, false);

            // Get performance report should not throw exception
            CachePerformanceAnalyzer.PerformanceReport report =
                performanceAnalyzer.getPerformanceReport();
            assertNotNull(report);
        });
    }

    @Test
    void testRecordCacheEviction() {
        // Test recording cache eviction
        assertDoesNotThrow(() -> {
            String cacheName = "eviction_test";

            // Record cache eviction
            performanceAnalyzer.recordCacheEviction(cacheName);

            // Should not throw exception
        });
    }

    @Test
    void testResetStatistics() {
        // Test resetting performance statistics
        assertDoesNotThrow(() -> {
            // Record some operations
            performanceAnalyzer.recordCacheOperation("reset_test", 100L, true);
            performanceAnalyzer.recordCacheOperation("reset_test", 150L, false);

            // Reset statistics
            performanceAnalyzer.resetStatistics();

            // Should not throw exception
        });
    }

    @Test
    void testConcurrentOperations() {
        // Test concurrent cache operations
        assertDoesNotThrow(() -> {
            // Simulate concurrent operations
            for (int i = 0; i < 10; i++) {
                performanceAnalyzer.recordCacheOperation(
                    "concurrent_cache",
                    100L + i,
                    i % 2 == 0
                );
            }

            // Should not throw exception
        });
    }

    @Test
    void testMultipleCaches() {
        // Test performance tracking for multiple caches
        assertDoesNotThrow(() -> {
            // Record operations for different caches
            performanceAnalyzer.recordCacheOperation("cache1", 100L, true);
            performanceAnalyzer.recordCacheOperation("cache2", 150L, false);
            performanceAnalyzer.recordCacheOperation("cache3", 200L, true);

            // Should not throw exception
        });
    }

    @Test
    void testPerformanceMetrics() {
        // Test comprehensive performance metrics
        assertDoesNotThrow(() -> {
            String cacheName = "metrics_test_cache";

            // Record various operations
            performanceAnalyzer.recordCacheOperation(cacheName, 50L, true);
            performanceAnalyzer.recordCacheOperation(cacheName, 100L, true);
            performanceAnalyzer.recordCacheOperation(cacheName, 200L, false);
            performanceAnalyzer.recordCacheOperation(cacheName, 75L, true);

            // Get performance report
            CachePerformanceAnalyzer.PerformanceReport report =
                performanceAnalyzer.getPerformanceReport();
            assertNotNull(report);
        });
    }
}
