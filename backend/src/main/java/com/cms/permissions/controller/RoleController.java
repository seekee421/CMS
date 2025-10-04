package com.cms.permissions.controller;

import com.cms.permissions.entity.Role;
import com.cms.permissions.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAuthority('ROLE:READ')")
@Tag(name = "角色管理", description = "角色管理 API")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "创建角色", description = "创建一个新的角色")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    public ResponseEntity<Role> createRole(
            @Parameter(description = "角色名称", example = "ADMIN") @RequestParam String name,
            @Parameter(description = "角色描述", example = "系统管理员") @RequestParam String description,
            @Parameter(description = "权限代码集合") @RequestBody(required = false) Set<String> permissionCodes) {
        Role role = roleService.createRole(name, description, permissionCodes);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "根据ID获取角色", description = "根据角色ID获取角色详情")
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "根据名称获取角色", description = "根据角色名称获取角色详情")
    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getRoleByName(
            @Parameter(description = "角色名称", example = "ADMIN") @PathVariable String name) {
        Role role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "获取所有角色", description = "获取系统中的所有角色")
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "更新角色权限", description = "更新角色的权限集合")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<Role> updateRolePermissions(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id,
            @Parameter(description = "权限代码集合") @RequestBody Set<String> permissionCodes) {
        Role role = roleService.updateRolePermissions(id, permissionCodes);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "更新角色信息", description = "更新角色的基本信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<Role> updateRole(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id,
            @Parameter(description = "角色名称", example = "ADMIN") @RequestParam(required = false) String name,
            @Parameter(description = "角色描述", example = "系统管理员") @RequestParam(required = false) String description) {
        Role role = roleService.updateRole(id, name, description);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "删除角色", description = "根据角色ID删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE:DELETE')")
    public ResponseEntity<Boolean> deleteRole(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id) {
        boolean result = roleService.deleteRole(id);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "为角色添加权限", description = "为指定角色添加权限")
    @PostMapping("/{id}/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<Role> addPermissionToRole(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id,
            @Parameter(description = "权限代码", example = "USER:READ") @PathVariable String permissionCode) {
        Role role = roleService.addPermissionToRole(id, permissionCode);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "从角色移除权限", description = "从指定角色移除权限")
    @DeleteMapping("/{id}/permissions/{permissionCode}")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<Role> removePermissionFromRole(
            @Parameter(description = "角色ID", example = "1") @PathVariable Long id,
            @Parameter(description = "权限代码", example = "USER:READ") @PathVariable String permissionCode) {
        Role role = roleService.removePermissionFromRole(id, permissionCode);
        return ResponseEntity.ok(role);
    }
}
