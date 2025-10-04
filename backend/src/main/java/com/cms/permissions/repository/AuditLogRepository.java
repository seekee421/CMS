package com.cms.permissions.repository;

import com.cms.permissions.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find audit logs by operation type
    List<AuditLog> findByOperationType(String operationType);

    // Find audit logs by resource type
    List<AuditLog> findByResourceType(String resourceType);

    // Find audit logs by username
    List<AuditLog> findByUsername(String username);

    // Find audit logs by operation type and resource type
    List<AuditLog> findByOperationTypeAndResourceType(String operationType, String resourceType);

    // Find audit logs within a date range
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Find audit logs by username and date range
    List<AuditLog> findByUsernameAndTimestampBetween(String username, LocalDateTime start, LocalDateTime end);

    // Find audit logs by operation type and date range
    List<AuditLog> findByOperationTypeAndTimestampBetween(String operationType, LocalDateTime start, LocalDateTime end);

    // Find audit logs with pagination
    Page<AuditLog> findByOrderByTimestampDesc(Pageable pageable);

    // Find audit logs by resource type with pagination
    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);

    // Find audit logs by username with pagination
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    // Custom query to find audit logs by multiple criteria
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:operationType IS NULL OR a.operationType = :operationType) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) AND " +
           "(:result IS NULL OR a.result = :result)")
    Page<AuditLog> findByFilters(@Param("username") String username,
                                 @Param("operationType") String operationType,
                                 @Param("resourceType") String resourceType,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("result") String result,
                                 Pageable pageable);

    // Find recent audit logs (last N hours)
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("since") LocalDateTime since);

    // Count audit logs by operation type
    long countByOperationType(String operationType);

    // Count audit logs by resource type
    long countByResourceType(String resourceType);

    // Count audit logs by result
    long countByResult(String result);

    // Count audit logs by operation type and result
    long countByOperationTypeAndResult(String operationType, String result);

    // Find distinct usernames in audit logs
    @Query("SELECT DISTINCT a.username FROM AuditLog a")
    List<String> findDistinctUsernames();

    // Find distinct operation types
    @Query("SELECT DISTINCT a.operationType FROM AuditLog a")
    List<String> findDistinctOperationTypes();

    // Find distinct resource types
    @Query("SELECT DISTINCT a.resourceType FROM AuditLog a")
    List<String> findDistinctResourceTypes();
}
