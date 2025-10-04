package com.cms.permissions.service;

import com.cms.permissions.entity.AuditLog;
import com.cms.permissions.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(
        AuditService.class
    );

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log a permission operation
     */
    public void logPermissionOperation(
        String operationType,
        String resourceType,
        String resourceId,
        String resourceName,
        String targetUser,
        String targetRole,
        String targetPermission,
        String result,
        String details
    ) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setOperationType(operationType);
            auditLog.setResourceType(resourceType);
            auditLog.setResourceId(resourceId);
            auditLog.setResourceName(resourceName);
            auditLog.setTargetUser(targetUser);
            auditLog.setTargetRole(targetRole);
            auditLog.setTargetPermission(targetPermission);
            auditLog.setResult(result);
            auditLog.setDetails(details);

            // Set user context
            String username = getCurrentUsername();
            auditLog.setUsername(username != null ? username : "SYSTEM");

            // Set request context if available
            try {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest request = attributes.getRequest();

                if (request != null) {
                    auditLog.setIpAddress(getClientIpAddress());
                    auditLog.setUserAgent(request.getHeader("User-Agent"));
                }
            } catch (IllegalStateException e) {
                // Request context not available (e.g., in async operations)
                auditLog.setIpAddress("UNKNOWN");
                auditLog.setUserAgent("SYSTEM");
            }

            auditLogRepository.save(auditLog);

            logger.info(
                "Audit log created: {} operation on {} {} by user {}",
                operationType,
                resourceType,
                resourceId,
                auditLog.getUsername()
            );
        } catch (Exception e) {
            logger.error(
                "Failed to create audit log for operation: {}",
                operationType,
                e
            );
        }
    }

    /**
     * Log a permission operation with minimal details
     */
    public void logPermissionOperation(
        String operationType,
        String resourceType,
        String resourceId,
        String result
    ) {
        logPermissionOperation(
            operationType,
            resourceType,
            resourceId,
            null,
            null,
            null,
            null,
            result,
            null
        );
    }

    /**
     * Log a permission operation with resource name
     */
    public void logPermissionOperation(
        String operationType,
        String resourceType,
        String resourceId,
        String resourceName,
        String result
    ) {
        logPermissionOperation(
            operationType,
            resourceType,
            resourceId,
            resourceName,
            null,
            null,
            null,
            result,
            null
        );
    }

    /**
     * Log a permission operation with details
     */
    public void logPermissionOperation(
        String operationType,
        String resourceType,
        String resourceId,
        String targetUser,
        String result,
        String details
    ) {
        logPermissionOperation(
            operationType,
            resourceType,
            resourceId,
            null,
            targetUser,
            null,
            null,
            result,
            details
        );
    }

    /**
     * Get current authenticated username
     */
    private String getCurrentUsername() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication != null &&
            authentication.isAuthenticated() &&
            !"anonymousUser".equals(authentication.getName())
        ) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            if (request == null) {
                return "UNKNOWN";
            }

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        } catch (IllegalStateException e) {
            // Request context not available (e.g., in async operations)
            return "UNKNOWN";
        }
    }

    /**
     * Find audit logs by operation type
     */
    public List<AuditLog> findByOperationType(String operationType) {
        return auditLogRepository.findByOperationType(operationType);
    }

    /**
     * Find audit logs by resource type
     */
    public List<AuditLog> findByResourceType(String resourceType) {
        return auditLogRepository.findByResourceType(resourceType);
    }

    /**
     * Find audit logs by username
     */
    public List<AuditLog> findByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }

    /**
     * Find audit logs within a date range
     */
    public List<AuditLog> findByDateRange(
        LocalDateTime start,
        LocalDateTime end
    ) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    /**
     * Find audit logs with pagination
     */
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findByOrderByTimestampDesc(pageable);
    }

    /**
     * Find audit logs by resource type with pagination
     */
    public Page<AuditLog> findByResourceType(
        String resourceType,
        Pageable pageable
    ) {
        return auditLogRepository.findByResourceTypeOrderByTimestampDesc(
            resourceType,
            pageable
        );
    }

    /**
     * Find audit logs by username with pagination
     */
    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(
            username,
            pageable
        );
    }

    /**
     * Find audit logs by filters with pagination
     */
    public Page<AuditLog> findByFilters(
        String username,
        String operationType,
        String resourceType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String result,
        Pageable pageable
    ) {
        return auditLogRepository.findByFilters(
            username,
            operationType,
            resourceType,
            startDate,
            endDate,
            result,
            pageable
        );
    }

    /**
     * Find recent audit logs (last N hours)
     */
    public List<AuditLog> findRecentLogs(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findRecentLogs(since);
    }

    /**
     * Count audit logs by operation type
     */
    public long countByOperationType(String operationType) {
        return auditLogRepository.countByOperationType(operationType);
    }

    /**
     * Count audit logs by resource type
     */
    public long countByResourceType(String resourceType) {
        return auditLogRepository.countByResourceType(resourceType);
    }

    /**
     * Count audit logs by result
     */
    public long countByResult(String result) {
        return auditLogRepository.countByResult(result);
    }

    /**
     * Count audit logs by operation type and result
     */
    public long countByOperationTypeAndResult(
        String operationType,
        String result
    ) {
        return auditLogRepository.countByOperationTypeAndResult(
            operationType,
            result
        );
    }

    /**
     * Get distinct usernames from audit logs
     */
    public List<String> findDistinctUsernames() {
        return auditLogRepository.findDistinctUsernames();
    }

    /**
     * Get distinct operation types from audit logs
     */
    public List<String> findDistinctOperationTypes() {
        return auditLogRepository.findDistinctOperationTypes();
    }

    /**
     * Get distinct resource types from audit logs
     */
    public List<String> findDistinctResourceTypes() {
        return auditLogRepository.findDistinctResourceTypes();
    }
}
