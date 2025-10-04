package com.cms.permissions.controller;

import com.cms.permissions.entity.Permission;
import com.cms.permissions.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@PreAuthorize("hasAuthority('PERMISSION:READ')")
@Tag(name = "权限管理", description = "权限管理 API")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @Operation(summary = "创建权限", description = "创建一个新的权限")
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
    public ResponseEntity<Permission> createPermission(
            @Parameter(description = "权限代码", example = "USER:READ") @RequestParam String code,
            @Parameter(description = "权限描述", example = "读取用户信息") @RequestParam String description) {
        Permission permission = permissionService.createPermission(code, description);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "根据ID获取权限", description = "根据权限ID获取权限详情")
    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long id) {
        Permission permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "根据代码获取权限", description = "根据权限代码获取权限详情")
    @GetMapping("/code/{code}")
    public ResponseEntity<Permission> getPermissionByCode(
            @Parameter(description = "权限代码", example = "USER:READ") @PathVariable String code) {
        Permission permission = permissionService.getPermissionByCode(code);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "获取所有权限", description = "获取系统中的所有权限")
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @Operation(summary = "更新权限", description = "更新现有权限的信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
    public ResponseEntity<Permission> updatePermission(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long id,
            @Parameter(description = "权限代码", example = "USER:READ") @RequestParam(required = false) String code,
            @Parameter(description = "权限描述", example = "读取用户信息") @RequestParam(required = false) String description) {
        Permission permission = permissionService.updatePermission(id, code, description);
        return ResponseEntity.ok(permission);
    }

    @Operation(summary = "删除权限", description = "根据权限ID删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    public ResponseEntity<Boolean> deletePermission(
            @Parameter(description = "权限ID", example = "1") @PathVariable Long id) {
        boolean result = permissionService.deletePermission(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "根据代码删除权限", description = "根据权限代码删除权限")
    @DeleteMapping("/code/{code}")
    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    public ResponseEntity<Boolean> deletePermissionByCode(
            @Parameter(description = "权限代码", example = "USER:READ") @PathVariable String code) {
        boolean result = permissionService.deletePermissionByCode(code);
        return ResponseEntity.ok(result);
    }
}
