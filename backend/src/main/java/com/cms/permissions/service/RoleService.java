package com.cms.permissions.service;

import com.cms.permissions.entity.Permission;
import com.cms.permissions.entity.Role;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.repository.PermissionRepository;
import com.cms.permissions.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    public Role createRole(String name, String description, Set<String> permissionCodes) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Role already exists: " + name);
        }

        Role role = new Role(name, description);

        Set<Permission> permissions = new HashSet<>();
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                Permission permission = permissionRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + code));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);
        
        // 清除所有用户权限缓存，因为新角色可能影响现有用户
        permissionCacheService.evictAllUserPermissions();
        
        return savedRole;
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public Role updateRolePermissions(Long roleId, Set<String> permissionCodes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Set<Permission> permissions = new HashSet<>();
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                Permission permission = permissionRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + code));
                permissions.add(permission);
            }
        }
        role.setPermissions(permissions);

        Role savedRole = roleRepository.save(role);
        
        // 清除所有用户权限缓存，因为角色权限已更改
        permissionCacheService.evictAllUserPermissions();
        
        return savedRole;
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public Role updateRole(Long roleId, String name, String description) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (name != null && !name.equals(role.getName())) {
            if (roleRepository.findByName(name).isPresent()) {
                throw new RuntimeException("Role name already exists: " + name);
            }
            role.setName(name);
        }

        if (description != null) {
            role.setDescription(description);
        }

        Role savedRole = roleRepository.save(role);
        
        // 清除所有用户权限缓存，因为角色信息已更改
        permissionCacheService.evictAllUserPermissions();
        
        return savedRole;
    }

    @PreAuthorize("hasAuthority('ROLE:DELETE')")
    public boolean deleteRole(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            return false;
        }

        // 清除所有用户权限缓存，因为角色将被删除
        permissionCacheService.evictAllUserPermissions();
        
        roleRepository.deleteById(roleId);
        return true;
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public Role addPermissionToRole(Long roleId, String permissionCode) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + permissionCode));

        role.addPermission(permission);
        Role savedRole = roleRepository.save(role);
        
        // 清除所有用户权限缓存
        permissionCacheService.evictAllUserPermissions();
        
        return savedRole;
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public Role removePermissionFromRole(Long roleId, String permissionCode) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + permissionCode));

        role.removePermission(permission);
        Role savedRole = roleRepository.save(role);
        
        // 清除所有用户权限缓存
        permissionCacheService.evictAllUserPermissions();
        
        return savedRole;
    }
}