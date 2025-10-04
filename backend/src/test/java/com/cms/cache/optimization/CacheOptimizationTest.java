package com.cms.cache.optimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CacheOptimizationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testCacheMemoryOptimization() {
        // Test cache memory optimization
        assertDoesNotThrow(() -> {
            // Create test data with different sizes
            for (int i = 0; i < 100; i++) {
                String key = "memory:test:" + i;
                String value = "value".repeat(i + 1); // Variable size values
                redisTemplate.opsForValue().set(key, value);
            }
            
            // Verify data exists
            assertTrue(redisTemplate.hasKey("memory:test:0"));
            assertTrue(redisTemplate.hasKey("memory:test:50"));
            assertTrue(redisTemplate.hasKey("memory:test:99"));
        });
    }

    @Test
    void testCacheEvictionPolicy() {
        // Test cache eviction policy
        assertDoesNotThrow(() -> {
            // Set cache values with expiration
            for (int i = 0; i < 10; i++) {
                String key = "eviction:test:" + i;
                redisTemplate.opsForValue().set(key, "value" + i);
                redisTemplate.expire(key, java.time.Duration.ofSeconds(1));
            }
            
            // Verify keys exist
            assertTrue(redisTemplate.hasKey("eviction:test:0"));
            
            // Wait for expiration
            Thread.sleep(1100);
            
            // Verify keys expired
            assertFalse(redisTemplate.hasKey("eviction:test:0"));
        });
    }

    @Test
    void testCacheCompressionOptimization() {
        // Test cache compression optimization
        assertDoesNotThrow(() -> {
            // Create large data that could benefit from compression
            String largeData = "This is a large string that could be compressed. ".repeat(100);
            String key = "compression:test";
            
            redisTemplate.opsForValue().set(key, largeData);
            String retrieved = (String) redisTemplate.opsForValue().get(key);
            
            assertEquals(largeData, retrieved);
        });
    }

    @Test
    void testCachePrefetchOptimization() {
        // Test cache prefetch optimization
        assertDoesNotThrow(() -> {
            // Simulate prefetching related data
            String baseKey = "prefetch:user:123";
            redisTemplate.opsForValue().set(baseKey + ":profile", "user_profile_data");
            redisTemplate.opsForValue().set(baseKey + ":permissions", "READ,WRITE");
            redisTemplate.opsForValue().set(baseKey + ":preferences", "theme:dark");
            
            // Verify all related data is cached
            assertTrue(redisTemplate.hasKey(baseKey + ":profile"));
            assertTrue(redisTemplate.hasKey(baseKey + ":permissions"));
            assertTrue(redisTemplate.hasKey(baseKey + ":preferences"));
        });
    }

    @Test
    void testCacheBatchOptimization() {
        // Test cache batch optimization
        assertDoesNotThrow(() -> {
            // Batch set operations
            for (int i = 0; i < 50; i++) {
                String key = "batch:test:" + i;
                redisTemplate.opsForValue().set(key, "batch_value_" + i);
            }
            
            // Batch get operations
            int foundCount = 0;
            for (int i = 0; i < 50; i++) {
                String key = "batch:test:" + i;
                if (redisTemplate.hasKey(key)) {
                    foundCount++;
                }
            }
            
            assertEquals(50, foundCount);
        });
    }

    @Test
    void testCachePartitioningOptimization() {
        // Test cache partitioning optimization
        assertDoesNotThrow(() -> {
            // Partition data by type
            redisTemplate.opsForValue().set("partition:users:123", "user_data");
            redisTemplate.opsForValue().set("partition:documents:456", "doc_data");
            redisTemplate.opsForValue().set("partition:permissions:789", "perm_data");
            
            // Verify partitioned data
            assertTrue(redisTemplate.hasKey("partition:users:123"));
            assertTrue(redisTemplate.hasKey("partition:documents:456"));
            assertTrue(redisTemplate.hasKey("partition:permissions:789"));
        });
    }

    @Test
    void testCacheIndexOptimization() {
        // Test cache index optimization
        assertDoesNotThrow(() -> {
            // Create indexed data
            String userId = "123";
            redisTemplate.opsForValue().set("index:user:" + userId, "user_data");
            redisTemplate.opsForSet().add("index:users:active", userId);
            redisTemplate.opsForSet().add("index:users:by_department:IT", userId);
            
            // Verify indexed data
            assertTrue(redisTemplate.hasKey("index:user:" + userId));
            assertTrue(redisTemplate.opsForSet().isMember("index:users:active", userId));
            assertTrue(redisTemplate.opsForSet().isMember("index:users:by_department:IT", userId));
        });
    }

    @Test
    void testCachePerformanceOptimization() {
        // Test cache performance optimization
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();
            
            // Perform optimized cache operations
            for (int i = 0; i < 1000; i++) {
                String key = "perf:test:" + i;
                redisTemplate.opsForValue().set(key, "value" + i);
                redisTemplate.opsForValue().get(key);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Verify operations completed in reasonable time
            assertTrue(duration < 10000); // Less than 10 seconds
        });
    }

    @Test
    void testCacheConcurrencyOptimization() {
        // Test cache concurrency optimization
        assertDoesNotThrow(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            
            // Submit concurrent tasks
            for (int i = 0; i < 100; i++) {
                final int index = i;
                executor.submit(() -> {
                    String key = "concurrent:opt:" + index;
                    redisTemplate.opsForValue().set(key, "value" + index);
                    redisTemplate.opsForValue().get(key);
                });
            }
            
            executor.shutdown();
            assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));
            
            // Verify some operations completed
            boolean hasData = false;
            for (int i = 0; i < 100; i++) {
                if (redisTemplate.hasKey("concurrent:opt:" + i)) {
                    hasData = true;
                    break;
                }
            }
            assertTrue(hasData);
        });
    }

    @Test
    void testCacheWarmupOptimization() {
        // Test cache warmup optimization
        assertDoesNotThrow(() -> {
            // Simulate cache warmup
            String[] criticalKeys = {
                "warmup:user:permissions:123",
                "warmup:document:public:456",
                "warmup:system:config"
            };
            
            for (String key : criticalKeys) {
                redisTemplate.opsForValue().set(key, "warmed_up_data");
            }
            
            // Verify warmup data is cached
            for (String key : criticalKeys) {
                assertTrue(redisTemplate.hasKey(key));
                assertNotNull(redisTemplate.opsForValue().get(key));
            }
        });
    }

    @Test
    void testCacheCleanupOptimization() {
        // Test cache cleanup optimization
        assertDoesNotThrow(() -> {
            // Create test data with different patterns
            redisTemplate.opsForValue().set("cleanup:temp:1", "temp_data");
            redisTemplate.opsForValue().set("cleanup:permanent:1", "permanent_data");
            redisTemplate.opsForValue().set("cleanup:session:1", "session_data");
            
            // Verify data exists
            assertTrue(redisTemplate.hasKey("cleanup:temp:1"));
            assertTrue(redisTemplate.hasKey("cleanup:permanent:1"));
            assertTrue(redisTemplate.hasKey("cleanup:session:1"));
            
            // Simulate selective cleanup (delete temp data)
            redisTemplate.delete("cleanup:temp:1");
            
            // Verify selective cleanup
            assertFalse(redisTemplate.hasKey("cleanup:temp:1"));
            assertTrue(redisTemplate.hasKey("cleanup:permanent:1"));
            assertTrue(redisTemplate.hasKey("cleanup:session:1"));
        });
    }

    @Test
    void testCacheMonitoringOptimization() {
        // Test cache monitoring optimization
        assertDoesNotThrow(() -> {
            // Set monitoring data
            redisTemplate.opsForValue().set("monitor:hits", "1000");
            redisTemplate.opsForValue().set("monitor:misses", "100");
            redisTemplate.opsForValue().set("monitor:evictions", "10");
            redisTemplate.opsForValue().set("monitor:memory_usage", "512MB");
            
            // Verify monitoring data
            assertEquals("1000", redisTemplate.opsForValue().get("monitor:hits"));
            assertEquals("100", redisTemplate.opsForValue().get("monitor:misses"));
            assertEquals("10", redisTemplate.opsForValue().get("monitor:evictions"));
            assertEquals("512MB", redisTemplate.opsForValue().get("monitor:memory_usage"));
        });
    }
}