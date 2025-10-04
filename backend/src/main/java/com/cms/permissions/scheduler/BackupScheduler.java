package com.cms.permissions.scheduler;

import com.cms.permissions.entity.DocumentBackup;
import com.cms.permissions.service.DocumentBackupService;
import com.cms.permissions.service.BackupConfigurationService;
import com.cms.permissions.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BackupScheduler {

    @Autowired
    private DocumentBackupService backupService;

    @Autowired
    private BackupConfigurationService configService;

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * 每小时检查是否需要执行自动备份
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void scheduleAutomaticBackups() {
        if (!configService.getCurrentConfiguration().getAutoBackupEnabled()) {
            return;
        }

        // 获取所有需要备份的文档
        List<Long> documentIds = documentRepository.findAll()
            .stream()
            .map(doc -> doc.getId())
            .toList();

        for (Long documentId : documentIds) {
            try {
                // 检查是否需要备份
                if (shouldCreateBackup(documentId)) {
                    backupService.createBackupAsync(
                        documentId, 
                        DocumentBackup.BackupType.SCHEDULED,
                        "Scheduled automatic backup"
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to create scheduled backup for document: " + documentId);
            }
        }
    }

    /**
     * 每天清理过期备份
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    public void cleanupExpiredBackups() {
        try {
            backupService.cleanupExpiredBackups();
        } catch (Exception e) {
            System.err.println("Failed to cleanup expired backups: " + e.getMessage());
        }
    }

    /**
     * 每周验证备份完整性
     */
    @Scheduled(cron = "0 0 3 * * SUN") // 每周日凌晨3点
    public void verifyBackupIntegrity() {
        // 实现备份完整性验证逻辑
    }

    /**
     * 判断是否需要创建备份
     */
    private boolean shouldCreateBackup(Long documentId) {
        var config = configService.getCurrentConfiguration();
        var latestBackup = backupService.getLatestBackup(documentId);
        
        if (latestBackup.isEmpty()) {
            return true; // 没有备份，需要创建
        }
        
        var backup = latestBackup.get();
        var hoursSinceLastBackup = java.time.Duration.between(
            backup.getBackupCreatedAt(), 
            java.time.LocalDateTime.now()
        ).toHours();
        
        return hoursSinceLastBackup >= config.getBackupIntervalHours();
    }
}