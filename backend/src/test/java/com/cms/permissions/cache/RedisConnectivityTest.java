package com.cms.permissions.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Redis connectivity test that excludes problematic cache optimization components
 */
@SpringBootTest(
    classes = RedisConnectivityTest.TestConfig.class,
    properties = {
        "cache.optimization.enabled=false",
        "cache.optimization.monitoring.enabled=false"
    }
)
@ActiveProfiles("test")
public class RedisConnectivityTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        // Test basic Redis connectivity
        String testKey = "test:connection:" + System.currentTimeMillis();
        String testValue = "Redis is working!";

        // Set a value
        redisTemplate.opsForValue().set(testKey, testValue);

        // Get the value back
        Object retrievedValue = redisTemplate.opsForValue().get(testKey);

        // Verify the value
        assert retrievedValue != null : "Value should be retrieved from Redis";
        assert testValue.equals(retrievedValue) : "Retrieved value should match the stored value";

        // Clean up
        redisTemplate.delete(testKey);

        System.out.println("Redis connection test passed successfully!");
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(
        basePackages = "com.cms.permissions",
        excludeFilters = @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = "com\\.cms\\.permissions\\.cache\\.(CacheOptimizationScheduler|CacheMemoryOptimizer|CacheWarmupStrategy)"
        )
    )
    static class TestConfig {
        // Test configuration that excludes problematic components
    }
}
