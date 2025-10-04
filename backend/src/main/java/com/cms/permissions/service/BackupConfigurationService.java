package com.cms.permissions.service;

import com.cms.permissions.entity.BackupConfiguration;
import com.cms.permissions.repository.BackupConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class BackupConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BackupConfigurationService.class);
    
    @Autowired
    private BackupConfigurationRepository configRepository;
    
    /**
     * 获取当前备份配置
     */
    public BackupConfiguration getCurrentConfiguration() {
        Optional<BackupConfiguration> config = configRepository.findByIsActiveTrue();
        if (config.isPresent()) {
            return config.get();
        }
        
        // 如果没有配置，创建默认配置
        return createDefaultConfiguration();
    }
    
    /**
     * 更新备份配置
     */
    public BackupConfiguration updateConfiguration(BackupConfiguration newConfig) {
        // 先禁用所有现有配置
        configRepository.deactivateAllConfigurations();
        
        // 设置新配置并更新时间
        newConfig.setUpdatedAt(LocalDateTime.now());
        
        BackupConfiguration saved = configRepository.save(newConfig);
        logger.info("Backup configuration updated: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * 创建默认配置
     */
    private BackupConfiguration createDefaultConfiguration() {
        BackupConfiguration defaultConfig = new BackupConfiguration();
        defaultConfig.setConfigName("default");
        defaultConfig.setAutoBackupEnabled(true);
        defaultConfig.setBackupIntervalHours(24);
        defaultConfig.setMaxBackupVersions(10);
        defaultConfig.setBackupRetentionDays(30);
        defaultConfig.setBackupStoragePath("/backup/documents");
        defaultConfig.setCompressionEnabled(true);
        defaultConfig.setEncryptionEnabled(false);
        defaultConfig.setIsActive(true);
        defaultConfig.setCreatedAt(LocalDateTime.now());
        defaultConfig.setUpdatedAt(LocalDateTime.now());
        
        BackupConfiguration saved = configRepository.save(defaultConfig);
        logger.info("Default backup configuration created: {}", saved.getId());
        
        return saved;
    }
    
    /**
     * 检查是否启用了自动备份
     */
    public boolean isAutoBackupEnabled() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getAutoBackupEnabled();
    }
    
    /**
     * 获取备份间隔（小时）
     */
    public int getBackupIntervalHours() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getBackupIntervalHours();
    }
    
    /**
     * 获取最大备份版本数
     */
    public int getMaxBackupVersions() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getMaxBackupVersions();
    }
    
    /**
     * 获取保留天数
     */
    public int getRetentionDays() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getBackupRetentionDays();
    }
    
    /**
     * 获取存储路径
     */
    public String getStoragePath() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getBackupStoragePath();
    }
    
    /**
     * 检查是否启用压缩
     */
    public boolean isCompressionEnabled() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getCompressionEnabled();
    }
    
    /**
     * 检查是否启用加密
     */
    public boolean isEncryptionEnabled() {
        BackupConfiguration config = getCurrentConfiguration();
        return config.getEncryptionEnabled();
    }
    
    /**
     * 获取配置状态
     */
    public ConfigStatus getConfigurationStatus() {
        BackupConfiguration config = getCurrentConfiguration();
        return new ConfigStatus(config);
    }
    
    /**
     * 验证配置
     */
    public ValidationResult validateConfiguration(BackupConfiguration config) {
        ValidationResult result = new ValidationResult();
        
        // 验证配置名称
        if (config.getConfigName() == null || config.getConfigName().trim().isEmpty()) {
            result.addError("配置名称不能为空");
        }
        
        // 验证备份间隔
        if (config.getBackupIntervalHours() == null || config.getBackupIntervalHours() < 1) {
            result.addError("备份间隔必须大于0小时");
        }
        
        // 验证最大版本数
        if (config.getMaxBackupVersions() == null || config.getMaxBackupVersions() < 1) {
            result.addError("最大备份版本数必须大于0");
        }
        
        // 验证保留天数
        if (config.getBackupRetentionDays() == null || config.getBackupRetentionDays() < 1) {
            result.addError("备份保留天数必须大于0");
        }
        
        // 验证存储路径
        if (config.getBackupStoragePath() == null || config.getBackupStoragePath().trim().isEmpty()) {
            result.addError("备份存储路径不能为空");
        }
        
        result.setValid(result.getErrors().isEmpty());
        return result;
    }
    
    /**
     * 配置状态类
     */
    public static class ConfigStatus {
        private final BackupConfiguration config;
        private final boolean isActive;
        private final LocalDateTime lastUpdated;
        private final String status;
        
        public ConfigStatus(BackupConfiguration config) {
            this.config = config;
            this.isActive = config.getIsActive();
            this.lastUpdated = config.getUpdatedAt();
            this.status = isActive ? "活跃" : "非活跃";
        }
        
        // Getters
        public BackupConfiguration getConfig() { return config; }
        public boolean isActive() { return isActive; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public String getStatus() { return status; }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private java.util.List<String> errors = new java.util.ArrayList<>();
        private java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
    }
}