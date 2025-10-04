package com.cms.permissions.service;

import com.cms.permissions.entity.DocumentAssignment;
import com.cms.permissions.entity.User;
import com.cms.permissions.repository.DocumentAssignmentRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PermissionCacheService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentAssignmentRepository documentAssignmentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Lazy
    private CachePerformanceAnalyzer performanceAnalyzer;

    private static final String USER_PERMISSIONS_KEY_PREFIX =
        "user:permissions:";
    private static final String USER_DOCUMENT_PERMISSIONS_KEY_PREFIX =
        "user:doc:permissions:";
    private static final String DOCUMENT_PUBLIC_KEY_PREFIX = "document:public:";

    /**
     * 获取用户的所有权限代码（带缓存）
     */
    @Cacheable(value = "userPermissions", key = "#username")
    public Set<String> getUserPermissions(String username) {
        long startTime = System.currentTimeMillis();
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                performanceAnalyzer.recordCacheOperation(
                    "userPermissions",
                    System.currentTimeMillis() - startTime,
                    false
                );
                return Set.of();
            }

            User user = userOpt.get();
            Set<String> permissions = user
                .getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .collect(Collectors.toSet());

            // 记录缓存命中或未命中
            performanceAnalyzer.recordCacheOperation(
                "userPermissions",
                System.currentTimeMillis() - startTime,
                true
            );
            return permissions;
        } catch (Exception e) {
            performanceAnalyzer.recordCacheOperation(
                "userPermissions",
                System.currentTimeMillis() - startTime,
                false
            );
            throw e;
        }
    }

    /**
     * 检查用户是否具有特定权限（带缓存）
     */
    public boolean hasPermission(String username, String permissionCode) {
        Set<String> userPermissions = getUserPermissions(username);
        return userPermissions.contains(permissionCode);
    }

    /**
     * 获取用户对特定文档的权限分配（带缓存）
     */
    @Cacheable(
        value = "userDocumentPermissions",
        key = "#userId + ':' + #documentId"
    )
    public List<DocumentAssignment> getUserDocumentAssignments(
        Long userId,
        Long documentId
    ) {
        long startTime = System.currentTimeMillis();
        try {
            // 使用现有的方法获取用户的所有分配，然后过滤特定文档
            List<DocumentAssignment> assignments = documentAssignmentRepository
                .findByUserId(userId)
                .stream()
                .filter(assignment ->
                    assignment.getDocumentId().equals(documentId)
                )
                .collect(Collectors.toList());
            performanceAnalyzer.recordCacheOperation(
                "documentAssignments",
                System.currentTimeMillis() - startTime,
                true
            );
            return assignments;
        } catch (Exception e) {
            performanceAnalyzer.recordCacheOperation(
                "documentAssignments",
                System.currentTimeMillis() - startTime,
                false
            );
            throw e;
        }
    }

    /**
     * 检查用户是否对文档有特定类型的分配
     */
    public boolean hasDocumentAssignment(
        Long userId,
        Long documentId,
        DocumentAssignment.AssignmentType assignmentType
    ) {
        List<DocumentAssignment> assignments = getUserDocumentAssignments(
            userId,
            documentId
        );
        return assignments
            .stream()
            .anyMatch(
                assignment -> assignment.getAssignmentType() == assignmentType
            );
    }

    /**
     * 检查文档是否为公开文档（带缓存）
     */
    @Cacheable(value = "documentPublic", key = "#documentId")
    public boolean isDocumentPublic(Long documentId) {
        long startTime = System.currentTimeMillis();
        try {
            boolean isPublic = documentRepository
                .findById(documentId)
                .map(document -> document.getIsPublic())
                .orElse(false);
            performanceAnalyzer.recordCacheOperation(
                "documentPublicStatus",
                System.currentTimeMillis() - startTime,
                true
            );
            return isPublic;
        } catch (Exception e) {
            performanceAnalyzer.recordCacheOperation(
                "documentPublicStatus",
                System.currentTimeMillis() - startTime,
                false
            );
            throw e;
        }
    }

    /**
     * 获取文档的所有权限分配（带缓存）
     */
    @Cacheable(value = "documentAssignments", key = "#documentId")
    public List<DocumentAssignment> getDocumentAssignments(Long documentId) {
        return documentAssignmentRepository.findByDocumentId(documentId);
    }

    /**
     * 检查用户是否被分配到特定文档（任何角色）
     */
    public boolean isUserAssignedToDocument(Long userId, Long documentId) {
        List<DocumentAssignment> assignments = getDocumentAssignments(
            documentId
        );
        return assignments
            .stream()
            .anyMatch(assignment -> assignment.getUserId().equals(userId));
    }

    /**
     * 清除用户权限缓存
     */
    @CacheEvict(value = "userPermissions", key = "#username")
    public void evictUserPermissions(String username) {
        // 缓存注解会自动处理清除
    }

    /**
     * 清除用户的所有文档权限缓存
     */
    public void evictUserDocumentPermissions(Long userId) {
        String pattern = USER_DOCUMENT_PERMISSIONS_KEY_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除特定文档的缓存
     */
    @CacheEvict(
        value = { "documentPublic", "documentAssignments" },
        key = "#documentId"
    )
    public void evictDocumentCache(Long documentId) {
        // 缓存注解会自动处理清除
    }

    /**
     * 清除所有用户对特定文档的权限缓存
     */
    public void evictAllUserDocumentPermissions(Long documentId) {
        String pattern =
            USER_DOCUMENT_PERMISSIONS_KEY_PREFIX + "*:" + documentId;
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除用户的所有相关缓存
     */
    public void evictAllUserCaches(String username, Long userId) {
        evictUserPermissions(username);
        evictUserDocumentPermissions(userId);
    }

    /**
     * 手动设置缓存（用于预热）
     */
    public void preloadUserPermissions(String username) {
        getUserPermissions(username); // 触发缓存加载
    }

    /**
     * 根据用户ID清除用户权限缓存
     */
    public void evictUserPermissions(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            evictUserPermissions(userOpt.get().getUsername());
        }
    }

    /**
     * 根据用户ID清除用户文档分配缓存
     */
    public void evictUserDocumentAssignments(Long userId) {
        evictUserDocumentPermissions(userId);
    }

    /**
     * 清除文档公开状态缓存
     */
    public void evictDocumentPublicStatus(Long documentId) {
        String key = DOCUMENT_PUBLIC_KEY_PREFIX + documentId;
        redisTemplate.delete(key);
    }

    /**
     * 清除所有用户权限缓存
     */
    @CacheEvict(value = "userPermissions", allEntries = true)
    public void evictAllUserPermissions() {
        // 缓存注解会自动处理清除
    }

    /**
     * 清除所有用户文档分配缓存
     */
    public void evictAllUserDocumentAssignments() {
        String pattern = USER_DOCUMENT_PERMISSIONS_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除所有文档公开状态缓存
     */
    public void evictAllDocumentPublicStatus() {
        String pattern = DOCUMENT_PUBLIC_KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats() {
        CacheStats stats = new CacheStats();

        // 统计用户权限缓存数量
        Set<String> userPermissionKeys = redisTemplate.keys(
            USER_PERMISSIONS_KEY_PREFIX + "*"
        );
        stats.setUserPermissionsCacheSize(
            userPermissionKeys != null ? userPermissionKeys.size() : 0
        );

        // 统计文档权限缓存数量
        Set<String> docPermissionKeys = redisTemplate.keys(
            USER_DOCUMENT_PERMISSIONS_KEY_PREFIX + "*"
        );
        stats.setDocumentPermissionsCacheSize(
            docPermissionKeys != null ? docPermissionKeys.size() : 0
        );

        // 统计文档公开状态缓存数量
        Set<String> docPublicKeys = redisTemplate.keys(
            DOCUMENT_PUBLIC_KEY_PREFIX + "*"
        );
        stats.setDocumentPublicCacheSize(
            docPublicKeys != null ? docPublicKeys.size() : 0
        );

        // 从Redis获取命中率统计（这里简化处理，实际项目中可以使用Redis的INFO命令或自定义计数器）
        // 为了演示，我们设置一些模拟数据
        stats.setUserPermissionHits(
            getCounterValue("cache:hits:userPermissions")
        );
        stats.setUserPermissionMisses(
            getCounterValue("cache:misses:userPermissions")
        );
        stats.setDocumentAssignmentHits(
            getCounterValue("cache:hits:documentAssignments")
        );
        stats.setDocumentAssignmentMisses(
            getCounterValue("cache:misses:documentAssignments")
        );
        stats.setDocumentPublicStatusHits(
            getCounterValue("cache:hits:documentPublicStatus")
        );
        stats.setDocumentPublicStatusMisses(
            getCounterValue("cache:misses:documentPublicStatus")
        );

        return stats;
    }

    /**
     * 获取计数器值
     */
    private long getCounterValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    /**
     * 增加计数器
     */
    public void incrementCounter(String key) {
        redisTemplate.opsForValue().increment(key);
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {

        private int userPermissionsCacheSize;
        private int documentPermissionsCacheSize;
        private int documentPublicCacheSize;
        private long userPermissionHits = 0;
        private long userPermissionMisses = 0;
        private long documentAssignmentHits = 0;
        private long documentAssignmentMisses = 0;
        private long documentPublicStatusHits = 0;
        private long documentPublicStatusMisses = 0;

        // Getters and Setters
        public int getUserPermissionsCacheSize() {
            return userPermissionsCacheSize;
        }

        public void setUserPermissionsCacheSize(int userPermissionsCacheSize) {
            this.userPermissionsCacheSize = userPermissionsCacheSize;
        }

        public int getDocumentPermissionsCacheSize() {
            return documentPermissionsCacheSize;
        }

        public void setDocumentPermissionsCacheSize(
            int documentPermissionsCacheSize
        ) {
            this.documentPermissionsCacheSize = documentPermissionsCacheSize;
        }

        public int getDocumentPublicCacheSize() {
            return documentPublicCacheSize;
        }

        public void setDocumentPublicCacheSize(int documentPublicCacheSize) {
            this.documentPublicCacheSize = documentPublicCacheSize;
        }

        public long getUserPermissionHits() {
            return userPermissionHits;
        }

        public void setUserPermissionHits(long userPermissionHits) {
            this.userPermissionHits = userPermissionHits;
        }

        public long getUserPermissionMisses() {
            return userPermissionMisses;
        }

        public void setUserPermissionMisses(long userPermissionMisses) {
            this.userPermissionMisses = userPermissionMisses;
        }

        public long getDocumentAssignmentHits() {
            return documentAssignmentHits;
        }

        public void setDocumentAssignmentHits(long documentAssignmentHits) {
            this.documentAssignmentHits = documentAssignmentHits;
        }

        public long getDocumentAssignmentMisses() {
            return documentAssignmentMisses;
        }

        public void setDocumentAssignmentMisses(long documentAssignmentMisses) {
            this.documentAssignmentMisses = documentAssignmentMisses;
        }

        public long getDocumentPublicStatusHits() {
            return documentPublicStatusHits;
        }

        public void setDocumentPublicStatusHits(long documentPublicStatusHits) {
            this.documentPublicStatusHits = documentPublicStatusHits;
        }

        public long getDocumentPublicStatusMisses() {
            return documentPublicStatusMisses;
        }

        public void setDocumentPublicStatusMisses(
            long documentPublicStatusMisses
        ) {
            this.documentPublicStatusMisses = documentPublicStatusMisses;
        }

        @Override
        public String toString() {
            return String.format(
                "CacheStats{userPermissions=%d, documentPermissions=%d, documentPublic=%d, " +
                    "userPermHits=%d, userPermMisses=%d, docAssignHits=%d, docAssignMisses=%d, " +
                    "docPublicHits=%d, docPublicMisses=%d}",
                userPermissionsCacheSize,
                documentPermissionsCacheSize,
                documentPublicCacheSize,
                userPermissionHits,
                userPermissionMisses,
                documentAssignmentHits,
                documentAssignmentMisses,
                documentPublicStatusHits,
                documentPublicStatusMisses
            );
        }
    }
}
