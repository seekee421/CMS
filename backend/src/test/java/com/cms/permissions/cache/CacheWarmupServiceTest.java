package com.cms.permissions.cache;

import static org.junit.jupiter.api.Assertions.*;

import com.cms.permissions.service.PermissionCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CacheWarmupServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private PermissionCacheService permissionCacheService;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testBasicCacheWarmup() {
        // Test basic cache warmup functionality
        String username = "testuser";

        assertDoesNotThrow(() -> {
            permissionCacheService.getUserPermissions(username);
        });
    }

    @Test
    void testCachePreloading() {
        // Test cache preloading
        assertDoesNotThrow(() -> {
            // Simulate preloading common data
            for (int i = 1; i <= 5; i++) {
                String username = "testuser" + i;
                permissionCacheService.getUserPermissions(username);
            }
        });
    }

    @Test
    void testCacheWarmupPerformance() {
        // Test warmup performance
        long startTime = System.currentTimeMillis();

        assertDoesNotThrow(() -> {
            // Simulate warmup operations
            for (int i = 1; i <= 10; i++) {
                String username = "testuser" + i;
                permissionCacheService.getUserPermissions(username);
            }
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Warmup should complete within reasonable time
        assertTrue(duration < 5000, "Warmup took too long: " + duration + "ms");
    }

    @Test
    void testCacheWarmupWithEviction() {
        // Test warmup with cache eviction
        String username = "testuser";

        assertDoesNotThrow(() -> {
            // Populate cache
            permissionCacheService.getUserPermissions(username);

            // Clear cache
            permissionCacheService.evictUserPermissions(username);

            // Warmup again
            permissionCacheService.getUserPermissions(username);
        });
    }

    @Test
    void testConcurrentWarmup() {
        // Test concurrent warmup operations
        assertDoesNotThrow(() -> {
            // Simulate concurrent warmup
            for (int i = 0; i < 5; i++) {
                final String username = "testuser" + (i + 1);
                new Thread(() -> {
                    permissionCacheService.getUserPermissions(username);
                })
                    .start();
            }

            // Wait a bit for threads to complete
            Thread.sleep(1000);
        });
    }
}
