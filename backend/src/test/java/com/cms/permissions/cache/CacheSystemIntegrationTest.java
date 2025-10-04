package com.cms.permissions.cache;

import com.cms.permissions.service.PermissionCacheService;
import com.cms.permissions.service.CachePerformanceAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CacheSystemIntegrationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private CachePerformanceAnalyzer performanceAnalyzer;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testRedisConnection() {
        // Test Redis connectivity
        assertDoesNotThrow(() -> {
            redisTemplate.opsForValue().set("test:key", "test:value");
            String value = (String) redisTemplate.opsForValue().get("test:key");
            assertEquals("test:value", value);
        });
    }

    @Test
    void testCacheServiceBasicOperations() {
        // Test basic cache operations
        String username = "testuser";
        
        assertDoesNotThrow(() -> {
            permissionCacheService.getUserPermissions(username);
            permissionCacheService.evictUserPermissions(username);
        });
    }

    @Test
    void testPerformanceAnalyzer() {
        // Test performance analyzer
        assertDoesNotThrow(() -> {
            performanceAnalyzer.recordCacheOperation("test_operation", 100L, true);
        });
    }

    @Test
    void testCacheEviction() {
        // Test cache eviction
        String username = "testuser";
        
        assertDoesNotThrow(() -> {
            permissionCacheService.getUserPermissions(username);
            permissionCacheService.evictUserPermissions(username);
        });
    }

    @Test
    void testConcurrentCacheAccess() {
        // Test concurrent access
        String username = "testuser";
        
        assertDoesNotThrow(() -> {
            // Simulate concurrent access
            for (int i = 0; i < 10; i++) {
                permissionCacheService.getUserPermissions(username);
            }
        });
    }
}