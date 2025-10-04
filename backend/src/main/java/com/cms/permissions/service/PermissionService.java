package com.cms.permissions.service;

import com.cms.permissions.entity.Permission;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.PermissionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private AuditService auditService;

    @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
    public Permission createPermission(String code, String description) {
        if (permissionRepository.findByCode(code).isPresent()) {
            throw new RuntimeException("Permission already exists: " + code);
        }

        Permission permission = new Permission(code, description);
        Permission savedPermission = permissionRepository.save(permission);

        // 清除所有用户权限缓存，因为新权限可能影响现有角色
        permissionCacheService.evictAllUserPermissions();

        // Log the permission creation
        auditService.logPermissionOperation(
            "CREATE",
            "PERMISSION",
            savedPermission.getId().toString(),
            code,
            null,
            null,
            code,
            "SUCCESS",
            "Created permission with code: " +
                code +
                " and description: " +
                description
        );

        return savedPermission;
    }

    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public Permission getPermissionById(Long permissionId) {
        return permissionRepository
            .findById(permissionId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Permission not found with id: " + permissionId
                )
            );
    }

    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public Permission getPermissionByCode(String code) {
        return permissionRepository
            .findByCode(code)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Permission not found with code: " + code
                )
            );
    }

    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
    public Permission updatePermission(
        Long permissionId,
        String code,
        String description
    ) {
        Permission permission = permissionRepository
            .findById(permissionId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Permission not found with id: " + permissionId
                )
            );

        if (code != null && !code.equals(permission.getCode())) {
            if (permissionRepository.findByCode(code).isPresent()) {
                throw new RuntimeException(
                    "Permission code already exists: " + code
                );
            }
            permission.setCode(code);
        }

        if (description != null) {
            permission.setDescription(description);
        }

        Permission savedPermission = permissionRepository.save(permission);

        // 清除所有用户权限缓存，因为权限信息已更改
        permissionCacheService.evictAllUserPermissions();

        // Log the permission update
        String details = "Updated permission - ";
        if (code != null) details += "Code: " + code + " ";
        if (description != null) details += "Description: " + description;

        auditService.logPermissionOperation(
            "UPDATE",
            "PERMISSION",
            savedPermission.getId().toString(),
            savedPermission.getCode(),
            null,
            null,
            savedPermission.getCode(),
            "SUCCESS",
            details
        );

        return savedPermission;
    }

    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    public boolean deletePermission(Long permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            return false;
        }

        // 清除所有用户权限缓存，因为权限将被删除
        permissionCacheService.evictAllUserPermissions();

        permissionRepository.deleteById(permissionId);

        // Log the permission deletion
        auditService.logPermissionOperation(
            "DELETE",
            "PERMISSION",
            permissionId.toString(),
            null,
            null,
            null,
            null,
            "SUCCESS",
            "Deleted permission with ID: " + permissionId
        );

        return true;
    }

    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    public boolean deletePermissionByCode(String code) {
        Permission permission = permissionRepository
            .findByCode(code)
            .orElse(null);
        if (permission == null) {
            return false;
        }

        // 清除所有用户权限缓存，因为权限将被删除
        permissionCacheService.evictAllUserPermissions();

        permissionRepository.delete(permission);

        // Log the permission deletion
        auditService.logPermissionOperation(
            "DELETE",
            "PERMISSION",
            permission.getId().toString(),
            code,
            null,
            null,
            code,
            "SUCCESS",
            "Deleted permission with code: " + code
        );

        return true;
    }
}
