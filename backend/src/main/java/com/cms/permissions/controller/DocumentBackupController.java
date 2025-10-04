package com.cms.permissions.controller;

import com.cms.permissions.entity.DocumentBackup;
import com.cms.permissions.entity.Document;
import com.cms.permissions.service.DocumentBackupService;
import com.cms.permissions.dto.BackupRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/documents/backup")
@Tag(name = "文档备份管理", description = "文档备份和恢复相关API")
@SecurityRequirement(name = "bearerAuth")
public class DocumentBackupController {

    @Autowired
    private DocumentBackupService backupService;

    @Operation(
        summary = "创建文档备份",
        description = "为指定文档创建备份"
    )
    @PostMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOC:BACKUP') or hasAuthority('DOC:EDIT')")
    public ResponseEntity<DocumentBackup> createBackup(
        @Parameter(description = "文档ID") @PathVariable Long documentId,
        @Valid @RequestBody BackupRequest request
    ) {
        DocumentBackup backup = backupService.createBackup(
            documentId, 
            request.getBackupType(), 
            request.getReason()
        );
        return ResponseEntity.ok(backup);
    }

    @Operation(
        summary = "异步创建文档备份",
        description = "异步为指定文档创建备份"
    )
    @PostMapping("/{documentId}/async")
    @PreAuthorize("hasAuthority('DOC:BACKUP') or hasAuthority('DOC:EDIT')")
    public ResponseEntity<CompletableFuture<DocumentBackup>> createBackupAsync(
        @Parameter(description = "文档ID") @PathVariable Long documentId,
        @Valid @RequestBody BackupRequest request
    ) {
        CompletableFuture<DocumentBackup> backup = backupService.createBackupAsync(
            documentId, 
            request.getBackupType(), 
            request.getReason()
        );
        return ResponseEntity.ok(backup);
    }

    @Operation(
        summary = "获取文档备份列表",
        description = "获取指定文档的所有备份"
    )
    @GetMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOC:VIEW:LOGGED') or hasAuthority('DOC:BACKUP')")
    public ResponseEntity<List<DocumentBackup>> getDocumentBackups(
        @Parameter(description = "文档ID") @PathVariable Long documentId
    ) {
        List<DocumentBackup> backups = backupService.getDocumentBackups(documentId);
        return ResponseEntity.ok(backups);
    }

    @Operation(
        summary = "恢复文档",
        description = "从指定备份版本恢复文档"
    )
    @PostMapping("/{documentId}/restore/{backupVersion}")
    @PreAuthorize("hasAuthority('DOC:RESTORE') or hasAuthority('DOC:EDIT')")
    public ResponseEntity<Document> restoreFromBackup(
        @Parameter(description = "文档ID") @PathVariable Long documentId,
        @Parameter(description = "备份版本") @PathVariable String backupVersion
    ) {
        Document document = backupService.restoreFromBackup(documentId, backupVersion);
        return ResponseEntity.ok(document);
    }

    @Operation(
        summary = "删除备份",
        description = "删除指定的备份"
    )
    @DeleteMapping("/{backupId}")
    @PreAuthorize("hasAuthority('DOC:BACKUP:DELETE') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBackup(
        @Parameter(description = "备份ID") @PathVariable Long backupId
    ) {
        backupService.deleteBackup(backupId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "验证备份完整性",
        description = "验证指定备份的完整性"
    )
    @PostMapping("/verify/{backupId}")
    @PreAuthorize("hasAuthority('DOC:BACKUP') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Boolean> verifyBackupIntegrity(
        @Parameter(description = "备份ID") @PathVariable Long backupId
    ) {
        boolean isValid = backupService.verifyBackupIntegrity(backupId);
        return ResponseEntity.ok(isValid);
    }

    @Operation(
        summary = "获取备份统计信息",
        description = "获取系统备份统计信息"
    )
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('DOC:BACKUP') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<DocumentBackupService.BackupStatistics> getBackupStatistics() {
        DocumentBackupService.BackupStatistics stats = backupService.getBackupStatistics();
        return ResponseEntity.ok(stats);
    }

    @Operation(
        summary = "清理过期备份",
        description = "手动触发清理过期备份"
    )
    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('DOC:BACKUP:CLEANUP') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> cleanupExpiredBackups() {
        backupService.cleanupExpiredBackups();
        return ResponseEntity.ok().build();
    }
}