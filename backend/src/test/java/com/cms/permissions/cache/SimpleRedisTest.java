package com.cms.permissions.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {})
@ContextConfiguration(classes = RedisTestApplication.class)
public class SimpleRedisTest {

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
        assertNotNull(retrievedValue, "Value should be retrieved from Redis");
        assertEquals(testValue, retrievedValue, "Retrieved value should match the stored value");

        // Clean up
        redisTemplate.delete(testKey);

        System.out.println("Redis connection test passed successfully!");
    }
}
