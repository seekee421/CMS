package com.cms.permissions.service;

import com.cms.permissions.entity.AuditLog;
import com.cms.permissions.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog sampleAuditLog;

    @BeforeEach
    void setUp() {
        sampleAuditLog = new AuditLog();
        sampleAuditLog.setId(1L);
        sampleAuditLog.setTimestamp(LocalDateTime.now());
        sampleAuditLog.setUsername("testuser");
        sampleAuditLog.setOperationType("CREATE");
        sampleAuditLog.setResourceType("PERMISSION");
        sampleAuditLog.setResourceId("1");
        sampleAuditLog.setResult("SUCCESS");
        sampleAuditLog.setDetails("Test permission creation");
    }

    @Test
    void testLogPermissionOperation() {
        // When
        auditService.logPermissionOperation("CREATE", "PERMISSION", "1", "SUCCESS", "Test details");

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testFindAll() {
        // Given
        Page<AuditLog> mockPage = new PageImpl<>(Arrays.asList(sampleAuditLog));
        when(auditLogRepository.findByOrderByTimestampDesc(any(Pageable.class))).thenReturn(mockPage);

        // When
        Page<AuditLog> result = auditService.findAll(Pageable.ofSize(10));

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        verify(auditLogRepository).findByOrderByTimestampDesc(any(Pageable.class));
    }

    @Test
    void testFindByOperationType() {
        // Given
        List<AuditLog> expectedLogs = Arrays.asList(sampleAuditLog);
        when(auditLogRepository.findByOperationType("CREATE")).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.findByOperationType("CREATE");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CREATE", result.get(0).getOperationType());
        verify(auditLogRepository).findByOperationType("CREATE");
    }

    @Test
    void testFindByResourceType() {
        // Given
        List<AuditLog> expectedLogs = Arrays.asList(sampleAuditLog);
        when(auditLogRepository.findByResourceType("PERMISSION")).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.findByResourceType("PERMISSION");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PERMISSION", result.get(0).getResourceType());
        verify(auditLogRepository).findByResourceType("PERMISSION");
    }

    @Test
    void testFindByUsername() {
        // Given
        List<AuditLog> expectedLogs = Arrays.asList(sampleAuditLog);
        when(auditLogRepository.findByUsername("testuser")).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.findByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(auditLogRepository).findByUsername("testuser");
    }

    @Test
    void testFindByDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<AuditLog> expectedLogs = Arrays.asList(sampleAuditLog);
        when(auditLogRepository.findByTimestampBetween(start, end)).thenReturn(expectedLogs);

        // When
        List<AuditLog> result = auditService.findByDateRange(start, end);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository).findByTimestampBetween(start, end);
    }

    @Test
    void testCountByOperationType() {
        // Given
        when(auditLogRepository.countByOperationType("CREATE")).thenReturn(5L);

        // When
        long count = auditService.countByOperationType("CREATE");

        // Then
        assertEquals(5L, count);
        verify(auditLogRepository).countByOperationType("CREATE");
    }

    @Test
    void testCountByResourceType() {
        // Given
        when(auditLogRepository.countByResourceType("PERMISSION")).thenReturn(3L);

        // When
        long count = auditService.countByResourceType("PERMISSION");

        // Then
        assertEquals(3L, count);
        verify(auditLogRepository).countByResourceType("PERMISSION");
    }

    @Test
    void testCountByResult() {
        // Given
        when(auditLogRepository.countByResult("SUCCESS")).thenReturn(10L);

        // When
        long count = auditService.countByResult("SUCCESS");

        // Then
        assertEquals(10L, count);
        verify(auditLogRepository).countByResult("SUCCESS");
    }
}
