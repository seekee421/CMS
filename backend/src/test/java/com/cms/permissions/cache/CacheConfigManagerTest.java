package com.cms.permissions.cache;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CacheConfigManagerTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testRedisConnection() {
        // Test Redis connection
        assertDoesNotThrow(() -> {
            redisTemplate.opsForValue().set("test:key", "test:value");
            String value = (String) redisTemplate.opsForValue().get("test:key");
            assertEquals("test:value", value);
        });
    }

    @Test
    void testCacheConfiguration() {
        // Test basic cache configuration
        assertDoesNotThrow(() -> {
            // Test setting cache values
            redisTemplate.opsForValue().set("config:cache:ttl", "300");
            redisTemplate.opsForValue().set("config:cache:maxsize", "1000");

            // Test getting cache values
            String ttl = (String) redisTemplate
                .opsForValue()
                .get("config:cache:ttl");
            String maxSize = (String) redisTemplate
                .opsForValue()
                .get("config:cache:maxsize");

            assertEquals("300", ttl);
            assertEquals("1000", maxSize);
        });
    }

    @Test
    void testCacheKeyPatterns() {
        // Test cache key patterns
        assertDoesNotThrow(() -> {
            // Test user permissions key pattern
            String userKey = "user:permissions:123";
            redisTemplate.opsForValue().set(userKey, "READ,WRITE");

            // Test document permissions key pattern
            String docKey = "user:doc:permissions:123:456";
            redisTemplate.opsForValue().set(docKey, "VIEWER");

            // Test document public key pattern
            String publicKey = "document:public:456";
            redisTemplate.opsForValue().set(publicKey, "true");

            // Verify keys exist
            assertTrue(redisTemplate.hasKey(userKey));
            assertTrue(redisTemplate.hasKey(docKey));
            assertTrue(redisTemplate.hasKey(publicKey));
        });
    }

    @Test
    void testCacheExpiration() {
        // Test cache expiration settings
        assertDoesNotThrow(() -> {
            String key = "test:expiration";
            redisTemplate.opsForValue().set(key, "value");

            // Set expiration
            redisTemplate.expire(key, java.time.Duration.ofSeconds(1));

            // Verify key exists
            assertTrue(redisTemplate.hasKey(key));

            // Wait for expiration
            Thread.sleep(1100);

            // Verify key expired
            assertFalse(redisTemplate.hasKey(key));
        });
    }

    @Test
    void testCacheStats() {
        // Test cache statistics collection
        assertDoesNotThrow(() -> {
            // Set some test data
            redisTemplate.opsForValue().set("stats:hits", "100");
            redisTemplate.opsForValue().set("stats:misses", "10");
            redisTemplate.opsForValue().set("stats:evictions", "5");

            // Get stats
            String hits = (String) redisTemplate
                .opsForValue()
                .get("stats:hits");
            String misses = (String) redisTemplate
                .opsForValue()
                .get("stats:misses");
            String evictions = (String) redisTemplate
                .opsForValue()
                .get("stats:evictions");

            assertEquals("100", hits);
            assertEquals("10", misses);
            assertEquals("5", evictions);
        });
    }

    @Test
    void testCacheOperations() {
        // Test basic cache operations
        assertDoesNotThrow(() -> {
            String key = "test:operations";

            // Test set operation
            redisTemplate.opsForValue().set(key, "initial_value");
            assertEquals("initial_value", redisTemplate.opsForValue().get(key));

            // Test update operation
            redisTemplate.opsForValue().set(key, "updated_value");
            assertEquals("updated_value", redisTemplate.opsForValue().get(key));

            // Test delete operation
            redisTemplate.delete(key);
            assertFalse(redisTemplate.hasKey(key));
        });
    }

    @Test
    void testCachePerformance() {
        // Test cache performance metrics
        assertDoesNotThrow(() -> {
            long startTime = System.currentTimeMillis();

            // Perform multiple cache operations
            for (int i = 0; i < 100; i++) {
                String key = "perf:test:" + i;
                redisTemplate.opsForValue().set(key, "value" + i);
                redisTemplate.opsForValue().get(key);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Verify operations completed in reasonable time
            assertTrue(duration < 5000); // Less than 5 seconds
        });
    }

    @Test
    void testConcurrentAccess() {
        // Test concurrent cache access
        assertDoesNotThrow(() -> {
            String key = "concurrent:test";

            // Simulate concurrent access
            for (int i = 0; i < 10; i++) {
                final int index = i;
                new Thread(() -> {
                    redisTemplate
                        .opsForValue()
                        .set(key + ":" + index, "value" + index);
                })
                    .start();
            }

            // Wait for threads to complete
            Thread.sleep(1000);

            // Verify some keys were created
            boolean hasKeys = false;
            for (int i = 0; i < 10; i++) {
                if (redisTemplate.hasKey(key + ":" + i)) {
                    hasKeys = true;
                    break;
                }
            }
            assertTrue(hasKeys);
        });
    }

    @Test
    void testCacheCleanup() {
        // Test cache cleanup operations
        assertDoesNotThrow(() -> {
            // Create test data
            for (int i = 0; i < 10; i++) {
                redisTemplate
                    .opsForValue()
                    .set("cleanup:test:" + i, "value" + i);
            }

            // Verify data exists
            assertTrue(redisTemplate.hasKey("cleanup:test:0"));

            // Cleanup all test data
            redisTemplate.getConnectionFactory().getConnection().flushAll();

            // Verify data is cleaned up
            assertFalse(redisTemplate.hasKey("cleanup:test:0"));
        });
    }

    @Test
    void testCacheHealthCheck() {
        // Test cache health check
        assertDoesNotThrow(() -> {
            // Test Redis connectivity
            redisTemplate.opsForValue().set("health:check", "ok");
            String result = (String) redisTemplate
                .opsForValue()
                .get("health:check");
            assertEquals("ok", result);

            // Test Redis info
            assertNotNull(redisTemplate.getConnectionFactory());
        });
    }
}
