package com.cms.permissions.controller;

import com.cms.permissions.entity.BackupConfiguration;
import com.cms.permissions.service.BackupConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/backup/config")
@Tag(name = "备份配置管理", description = "备份配置相关API")
@SecurityRequirement(name = "bearerAuth")
public class BackupConfigController {

    @Autowired
    private BackupConfigurationService backupConfigService;

    @Operation(
        summary = "获取当前备份配置",
        description = "获取当前活跃的备份配置"
    )
    @GetMapping
    @PreAuthorize("hasAuthority('BACKUP:VIEW') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BackupConfiguration> getCurrentConfiguration() {
        BackupConfiguration config = backupConfigService.getCurrentConfiguration();
        return ResponseEntity.ok(config);
    }

    @Operation(
        summary = "更新备份配置",
        description = "更新系统备份配置"
    )
    @PutMapping
    @PreAuthorize("hasAuthority('BACKUP:MANAGE') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BackupConfiguration> updateConfiguration(
        @Valid @RequestBody BackupConfiguration newConfig
    ) {
        BackupConfiguration updatedConfig = backupConfigService.updateConfiguration(newConfig);
        return ResponseEntity.ok(updatedConfig);
    }

    @Operation(
        summary = "获取备份配置状态",
        description = "获取备份配置的详细状态信息"
    )
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('BACKUP:VIEW') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BackupConfigurationService.ConfigStatus> getConfigurationStatus() {
        BackupConfigurationService.ConfigStatus status = backupConfigService.getConfigurationStatus();
        return ResponseEntity.ok(status);
    }

    @Operation(
        summary = "验证备份配置",
        description = "验证备份配置的有效性"
    )
    @PostMapping("/validate")
    @PreAuthorize("hasAuthority('BACKUP:MANAGE') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<BackupConfigurationService.ValidationResult> validateConfiguration(
        @Valid @RequestBody BackupConfiguration config
    ) {
        BackupConfigurationService.ValidationResult result = backupConfigService.validateConfiguration(config);
        return ResponseEntity.ok(result);
    }
}