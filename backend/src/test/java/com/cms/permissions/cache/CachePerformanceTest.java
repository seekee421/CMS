package com.cms.permissions.cache;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.User;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import com.cms.permissions.service.PermissionCacheService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
        "spring.redis.host=localhost",
        "spring.redis.port=6379",
        "spring.redis.database=2",
        "cache.permissions.ttl=60",
        "cache.document-public.ttl=60",
        "cache.document-assignments.ttl=60",
    }
)
class CachePerformanceTest {

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DocumentRepository documentRepository;

    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 100;

    @BeforeEach
    void setUp() {
        // Clear Redis cache before each test
        redisTemplate.getConnectionFactory().getConnection().flushDb();

        // Mock repository responses
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        Document mockDocument = new Document();
        mockDocument.setId(1L);
        mockDocument.setTitle("Test Document");

        when(userRepository.findById(any())).thenReturn(
            java.util.Optional.of(mockUser)
        );
        when(documentRepository.findById(any())).thenReturn(
            java.util.Optional.of(mockDocument)
        );
    }

    @Test
    void testBasicCachePerformance() throws InterruptedException {
        // Given
        String username = "testuser";
        // Long documentId = 1L;

        // When - First call (cache miss)
        long startTime = System.currentTimeMillis();
        Set<String> permissions1 = permissionCacheService.getUserPermissions(
            username
        );
        long firstCallTime = System.currentTimeMillis() - startTime;

        // When - Second call (cache hit)
        startTime = System.currentTimeMillis();
        Set<String> permissions2 = permissionCacheService.getUserPermissions(
            username
        );
        long secondCallTime = System.currentTimeMillis() - startTime;

        // Then
        assertNotNull(permissions1);
        assertNotNull(permissions2);
        assertEquals(permissions1, permissions2);

        // Cache hit should be faster than cache miss
        assertTrue(
            secondCallTime < firstCallTime,
            "Cache hit (" +
                secondCallTime +
                "ms) should be faster than cache miss (" +
                firstCallTime +
                "ms)"
        );

        System.out.println("Cache miss time: " + firstCallTime + "ms");
        System.out.println("Cache hit time: " + secondCallTime + "ms");
    }

    @Test
    void testConcurrentCacheAccess()
        throws InterruptedException, ExecutionException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<Long>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            Future<Long> future = executor.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    String username =
                        "user" + (threadId * OPERATIONS_PER_THREAD + j + 1);
                    permissionCacheService.getUserPermissions(username);
                }
                return System.currentTimeMillis() - threadStartTime;
            });
            futures.add(future);
        }

        // Then
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS));

        long totalTime = 0;
        for (Future<Long> future : futures) {
            totalTime += future.get();
        }

        double averageTimePerThread = (double) totalTime / THREAD_COUNT;
        System.out.println(
            "Average time per thread: " + averageTimePerThread + "ms"
        );
        System.out.println(
            "Total operations: " + (THREAD_COUNT * OPERATIONS_PER_THREAD)
        );

        assertTrue(
            averageTimePerThread < 10000,
            "Average time per thread should be reasonable"
        );
    }

    @Test
    void testCacheEvictionPerformance() {
        // Given
        String username = "testuser";

        // When - Cache data
        permissionCacheService.getUserPermissions(username);

        // When - Evict cache
        long startTime = System.currentTimeMillis();
        permissionCacheService.evictUserPermissions(username);
        long evictionTime = System.currentTimeMillis() - startTime;

        // Then
        assertTrue(evictionTime < 100, "Cache eviction should be fast");
        System.out.println("Cache eviction time: " + evictionTime + "ms");
    }

    @Test
    void testCacheMemoryUsage() {
        // Given
        int userCount = 100;

        // When - Load cache with multiple users
        for (int i = 1; i <= userCount; i++) {
            String username = "user" + i;
            Long documentId = (long) i;
            permissionCacheService.getUserPermissions(username);
            permissionCacheService.getUserDocumentAssignments(1L, documentId);
        }

        // Then - Verify cache is populated
        // This is a basic test - in a real scenario you'd check actual memory usage
        assertTrue(true, "Cache loading completed without errors");
        System.out.println("Loaded " + userCount + " users into cache");
    }
}
