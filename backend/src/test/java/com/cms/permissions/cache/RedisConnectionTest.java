package com.cms.permissions.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RedisConnectionTest {

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

    @Test
    public void testRedisHashOperations() {
        // Test Redis hash operations
        String testKey = "test:hash:" + System.currentTimeMillis();

        // Put values in hash
        redisTemplate.opsForHash().put(testKey, "field1", "value1");
        redisTemplate.opsForHash().put(testKey, "field2", "value2");

        // Retrieve values from hash
        Object value1 = redisTemplate.opsForHash().get(testKey, "field1");
        Object value2 = redisTemplate.opsForHash().get(testKey, "field2");

        // Verify the values
        assertEquals("value1", value1);
        assertEquals("value2", value2);

        // Clean up
        redisTemplate.delete(testKey);

        System.out.println("Redis hash operations test passed successfully!");
    }
}
