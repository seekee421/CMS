package com.cms.permissions.service;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.DocumentBackup;
import com.cms.permissions.entity.BackupConfiguration;
import com.cms.permissions.repository.DocumentBackupRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.exception.BackupException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

@Service
@Transactional
public class DocumentBackupService {

    @Autowired
    private DocumentBackupRepository backupRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private BackupConfigurationService configService;

    @Autowired
    private AuditService auditService;

    private static final String BACKUP_BASE_PATH = "backups/documents/";

    /**
     * 创建文档备份
     */
    public DocumentBackup createBackup(Long documentId, DocumentBackup.BackupType backupType, String reason) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        DocumentBackup backup = new DocumentBackup(document, backupType, reason);
        backup.setBackupStatus(DocumentBackup.BackupStatus.IN_PROGRESS);

        // 保存备份记录
        backup = backupRepository.save(backup);

        try {
            // 创建备份文件
            String filePath = createBackupFile(backup);
            backup.setFilePath(filePath);
            
            // 计算文件大小和校验和
            Path path = Paths.get(filePath);
            backup.setFileSize(Files.size(path));
            backup.setChecksum(calculateChecksum(filePath));
            
            backup.setBackupStatus(DocumentBackup.BackupStatus.COMPLETED);
            
            // 记录审计日志
            auditService.logPermissionOperation(
                "BACKUP_CREATE",
                "DOCUMENT",
                document.getId().toString(),
                document.getTitle(),
                "SUCCESS",
                "Document backup created successfully. Backup ID: " + backup.getId()
            );
            
        } catch (Exception e) {
            backup.setBackupStatus(DocumentBackup.BackupStatus.FAILED);
            auditService.logPermissionOperation(
                "BACKUP_CREATE",
                "DOCUMENT",
                documentId.toString(),
                "FAILED",
                "Failed to create backup: " + e.getMessage()
            );
            throw new BackupException("Failed to create backup for document " + documentId, e);
        } finally {
            backup = backupRepository.save(backup);
        }

        // 清理旧备份
        cleanupOldBackups(documentId);

        return backup;
    }

    /**
     * 异步创建备份
     */
    @Async
    public CompletableFuture<DocumentBackup> createBackupAsync(Long documentId, 
                                                              DocumentBackup.BackupType backupType, 
                                                              String reason) {
        return CompletableFuture.completedFuture(createBackup(documentId, backupType, reason));
    }

    /**
     * 恢复文档从备份
     */
    public Document restoreFromBackup(Long documentId, String backupVersion) {
        DocumentBackup backup = backupRepository.findByDocumentIdAndBackupVersion(documentId, backupVersion)
            .orElseThrow(() -> new ResourceNotFoundException("Backup not found"));

        if (backup.getBackupStatus() != DocumentBackup.BackupStatus.COMPLETED) {
            throw new BackupException("Cannot restore from incomplete backup");
        }

        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        try {
            // 在恢复前创建当前状态的备份
            createBackup(documentId, DocumentBackup.BackupType.BEFORE_UPDATE, "Before restore from " + backupVersion);

            // 恢复文档内容
             document.setTitle(backup.getTitle());
             document.setContent(backup.getContent());
             document.setStatus(convertToDocumentStatus(backup.getStatus()));
             document.setIsPublic(backup.getIsPublic());
             document.setUpdatedAt(LocalDateTime.now());

            document = documentRepository.save(document);

            // 记录审计日志
            auditService.logPermissionOperation(
                "BACKUP_RESTORE",
                "DOCUMENT",
                documentId.toString(),
                document.getTitle(),
                "SUCCESS",
                "Document restored from backup version: " + backupVersion
            );

            return document;
        } catch (Exception e) {
            auditService.logPermissionOperation(
                "BACKUP_RESTORE",
                "DOCUMENT",
                backup.getDocumentId().toString(),
                "FAILED",
                "Failed to restore from backup ID: " + backup.getId() + ". Error: " + e.getMessage()
            );
            throw new BackupException("Failed to restore document: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文档的所有备份
     */
    public List<DocumentBackup> getDocumentBackups(Long documentId) {
        return backupRepository.findByDocumentIdOrderByBackupCreatedAtDesc(documentId);
    }

    /**
     * 获取文档的最新备份
     */
    public Optional<DocumentBackup> getLatestBackup(Long documentId) {
        return backupRepository.findTopByDocumentIdOrderByBackupCreatedAtDesc(documentId);
    }

    /**
     * 删除备份
     */
    public void deleteBackup(Long backupId) {
        DocumentBackup backup = backupRepository.findById(backupId)
            .orElseThrow(() -> new ResourceNotFoundException("Backup not found"));

        try {
            // 删除备份文件
            if (backup.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(backup.getFilePath()));
            }
            
            // 删除数据库记录
            backupRepository.delete(backup);
            
            // 记录审计日志
            auditService.logPermissionOperation(
                "BACKUP_DELETE",
                "DOCUMENT",
                backup.getDocumentId().toString(),
                "SUCCESS",
                "Backup deleted. Backup ID: " + backupId
            );
            
        } catch (IOException e) {
            throw new BackupException("Failed to delete backup file", e);
        }
    }

    /**
     * 验证备份完整性
     */
    public boolean verifyBackupIntegrity(Long backupId) {
        DocumentBackup backup = backupRepository.findById(backupId)
            .orElseThrow(() -> new ResourceNotFoundException("Backup not found"));

        if (backup.getFilePath() == null || backup.getChecksum() == null) {
            return false;
        }

        try {
            String currentChecksum = calculateChecksum(backup.getFilePath());
            boolean isValid = backup.getChecksum().equals(currentChecksum);
            
            if (!isValid) {
                backup.setBackupStatus(DocumentBackup.BackupStatus.CORRUPTED);
                backupRepository.save(backup);
            }
            
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清理过期备份
     */
    public void cleanupExpiredBackups() {
        BackupConfiguration config = configService.getCurrentConfiguration();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(config.getBackupRetentionDays());
        
        List<DocumentBackup> expiredBackups = backupRepository.findExpiredBackups(cutoffDate);
        
        for (DocumentBackup backup : expiredBackups) {
            try {
                deleteBackup(backup.getId());
            } catch (Exception e) {
                // 记录错误但继续处理其他备份
                System.err.println("Failed to delete expired backup: " + backup.getId());
            }
        }
    }

    /**
     * 清理旧备份（保持最大版本数限制）
     */
    private void cleanupOldBackups(Long documentId) {
        BackupConfiguration config = configService.getCurrentConfiguration();
        long backupCount = backupRepository.countByDocumentId(documentId);
        
        if (backupCount > config.getMaxBackupVersions()) {
            Pageable pageable = PageRequest.of(config.getMaxBackupVersions(), 
                                             (int)(backupCount - config.getMaxBackupVersions()));
            List<DocumentBackup> oldBackups = backupRepository.findBackupsForCleanup(documentId, pageable);
            
            for (DocumentBackup backup : oldBackups) {
                try {
                    deleteBackup(backup.getId());
                } catch (Exception e) {
                    // 记录错误但继续处理
                    System.err.println("Failed to cleanup old backup: " + backup.getId());
                }
            }
        }
    }

    /**
     * 创建备份文件
     */
    private String createBackupFile(DocumentBackup backup) throws IOException {
        // 创建备份目录
        Path backupDir = Paths.get(BACKUP_BASE_PATH, backup.getDocumentId().toString());
        Files.createDirectories(backupDir);

        // 生成备份文件名
        String fileName = String.format("backup_%s_%s.json", 
                                       backup.getBackupVersion(), 
                                       System.currentTimeMillis());
        Path filePath = backupDir.resolve(fileName);

        // 创建备份内容
        String backupContent = createBackupContent(backup);

        // 根据配置决定是否压缩
        BackupConfiguration config = configService.getCurrentConfiguration();
        if (config.getCompressionEnabled()) {
            fileName += ".gz";
            filePath = backupDir.resolve(fileName);
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                 GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
                gzos.write(backupContent.getBytes());
            }
        } else {
            Files.write(filePath, backupContent.getBytes());
        }

        return filePath.toString();
    }

    /**
     * 创建备份内容（JSON格式）
     */
    private String createBackupContent(DocumentBackup backup) {
        return String.format("""
            {
                "documentId": %d,
                "backupVersion": "%s",
                "title": "%s",
                "content": "%s",
                "status": "%s",
                "isPublic": %b,
                "createdBy": %d,
                "originalCreatedAt": "%s",
                "originalUpdatedAt": "%s",
                "backupCreatedAt": "%s",
                "backupType": "%s",
                "backupReason": "%s"
            }
            """,
            backup.getDocumentId(),
            backup.getBackupVersion(),
            escapeJson(backup.getTitle()),
            escapeJson(backup.getContent()),
            backup.getStatus(),
            backup.getIsPublic(),
            backup.getCreatedBy(),
            backup.getOriginalCreatedAt(),
            backup.getOriginalUpdatedAt(),
            backup.getBackupCreatedAt(),
            backup.getBackupType(),
            escapeJson(backup.getBackupReason())
        );
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 计算文件校验和
     */
    private String calculateChecksum(String filePath) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 获取备份统计信息
     */
    public BackupStatistics getBackupStatistics() {
        long totalBackups = backupRepository.count();
        long completedBackups = backupRepository.findByBackupStatus(DocumentBackup.BackupStatus.COMPLETED).size();
        long failedBackups = backupRepository.findByBackupStatus(DocumentBackup.BackupStatus.FAILED).size();
        
        return new BackupStatistics(totalBackups, completedBackups, failedBackups);
    }

    /**
     * 转换DocumentStatus枚举 - 从DocumentBackup到Document
     */
    private Document.DocumentStatus convertToDocumentStatus(DocumentBackup.DocumentStatus status) {
        if (status == null) {
            return Document.DocumentStatus.DRAFT;
        }
        
        switch (status) {
            case DRAFT:
                return Document.DocumentStatus.DRAFT;
            case PENDING_APPROVAL:
                return Document.DocumentStatus.PENDING_APPROVAL;
            case PUBLISHED:
                return Document.DocumentStatus.PUBLISHED;
            case REJECTED:
                return Document.DocumentStatus.REJECTED;
            default:
                return Document.DocumentStatus.DRAFT;
        }
    }

    /**
     * 备份统计信息类
     */
    public static class BackupStatistics {
        private final long totalBackups;
        private final long completedBackups;
        private final long failedBackups;

        public BackupStatistics(long totalBackups, long completedBackups, long failedBackups) {
            this.totalBackups = totalBackups;
            this.completedBackups = completedBackups;
            this.failedBackups = failedBackups;
        }

        public long getTotalBackups() { return totalBackups; }
        public long getCompletedBackups() { return completedBackups; }
        public long getFailedBackups() { return failedBackups; }
        public double getSuccessRate() { 
            return totalBackups > 0 ? (double) completedBackups / totalBackups * 100 : 0; 
        }
    }
}