package com.cms.permissions.repository;

import com.cms.permissions.entity.BackupConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackupConfigurationRepository extends JpaRepository<BackupConfiguration, Long> {
    
    /**
     * 查找当前活跃的备份配置
     */
    Optional<BackupConfiguration> findByIsActiveTrue();
    
    /**
     * 禁用所有配置
     */
    @Modifying
    @Query("UPDATE BackupConfiguration bc SET bc.isActive = false")
    void deactivateAllConfigurations();
}