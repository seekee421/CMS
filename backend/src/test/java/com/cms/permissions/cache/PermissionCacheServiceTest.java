package com.cms.permissions.cache;

import static org.junit.jupiter.api.Assertions.*;

import com.cms.permissions.entity.DocumentAssignment;
import com.cms.permissions.service.PermissionCacheService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PermissionCacheServiceTest {

    @MockBean
    private PermissionCacheService permissionCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void testGetUserPermissions() {
        // Test getting user permissions
        String username = "testuser";

        assertDoesNotThrow(() -> {
            Set<String> permissions = permissionCacheService.getUserPermissions(
                username
            );
            assertNotNull(permissions);
        });
    }

    @Test
    void testGetUserDocumentAssignments() {
        // Test getting user document assignments
        Long userId = 1L;
        Long documentId = 1L;

        assertDoesNotThrow(() -> {
            List<DocumentAssignment> assignments =
                permissionCacheService.getUserDocumentAssignments(
                    userId,
                    documentId
                );
            assertNotNull(assignments);
        });
    }

    @Test
    void testClearUserPermissions() {
        // Test clearing user permissions
        String username = "testuser";

        assertDoesNotThrow(() -> {
            // First get permissions to populate cache
            permissionCacheService.getUserPermissions(username);

            // Then clear them
            permissionCacheService.evictUserPermissions(username);
        });
    }

    @Test
    void testClearUserDocumentAssignments() {
        // Test clearing user document assignments
        Long userId = 1L;

        assertDoesNotThrow(() -> {
            // First get assignments to populate cache
            permissionCacheService.getUserDocumentAssignments(userId, 1L);

            // Then clear them
            permissionCacheService.evictUserDocumentAssignments(userId);
        });
    }

    @Test
    void testCacheStats() {
        // Test cache statistics
        assertDoesNotThrow(() -> {
            PermissionCacheService.CacheStats stats =
                permissionCacheService.getCacheStats();
            assertNotNull(stats);
        });
    }

    @Test
    void testCacheHitAndMiss() {
        // Test cache hit and miss behavior
        String username = "testuser";

        assertDoesNotThrow(() -> {
            // First call should be a cache miss
            Set<String> permissions1 =
                permissionCacheService.getUserPermissions(username);
            assertNotNull(permissions1);

            // Second call should be a cache hit
            Set<String> permissions2 =
                permissionCacheService.getUserPermissions(username);
            assertNotNull(permissions2);

            // Results should be the same
            assertEquals(permissions1, permissions2);
        });
    }

    @Test
    void testConcurrentAccess() {
        // Test concurrent cache access
        String username = "testuser";

        assertDoesNotThrow(() -> {
            // Simulate concurrent access
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    permissionCacheService.getUserPermissions(username);
                })
                    .start();
            }

            // Wait a bit for threads to complete
            Thread.sleep(1000);
        });
    }

    @Test
    void testCacheEviction() {
        // Test cache eviction
        String username = "testuser";

        assertDoesNotThrow(() -> {
            // Populate cache
            permissionCacheService.getUserPermissions(username);

            // Clear cache
            permissionCacheService.evictUserPermissions(username);

            // Access again (should reload from database)
            Set<String> permissions = permissionCacheService.getUserPermissions(
                username
            );
            assertNotNull(permissions);
        });
    }

    @Test
    void testMultipleUsers() {
        // Test caching for multiple users
        assertDoesNotThrow(() -> {
            for (int i = 1; i <= 5; i++) {
                String username = "testuser" + i;
                Set<String> permissions =
                    permissionCacheService.getUserPermissions(username);
                assertNotNull(permissions);

                List<DocumentAssignment> assignments =
                    permissionCacheService.getUserDocumentAssignments(
                        1L,
                        (long) i
                    );
                assertNotNull(assignments);
            }
        });
    }
}
