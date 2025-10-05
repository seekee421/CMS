package com.cms.permissions.service;

import com.cms.permissions.entity.Role;
import com.cms.permissions.entity.User;
import com.cms.permissions.exception.UserNotFoundException;
import com.cms.permissions.repository.RoleRepository;
import com.cms.permissions.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Autowired
    private AuditService auditService;

    @PreAuthorize(
        "hasAuthority('USER:MANAGE:SUB') OR hasAuthority('USER:MANAGE:EDITOR')"
    )
    public User createUser(
        String username,
        String rawPassword,
        String email,
        Set<String> roleNames
    ) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(
            username,
            passwordEncoder.encode(rawPassword),
            email
        );

        Set<Role> roles = new HashSet<>();
        if (roleNames != null) {
            for (String roleName : roleNames) {
                Role role = roleRepository
                    .findByName(roleName)
                    .orElseThrow(() ->
                        new RuntimeException("Role not found: " + roleName)
                    );
                roles.add(role);
            }
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // 清除新用户的权限缓存（对缓存异常进行保护，避免影响数据库事务提交）
        try {
            permissionCacheService.evictUserPermissions(savedUser.getId());
        } catch (Exception e) {
            auditService.logPermissionOperation(
                "CACHE_EVICT",
                "USER",
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getUsername(),
                null,
                null,
                "FAILURE",
                "Evict user permission cache failed: " + e.getMessage()
            );
        }

        return savedUser;
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    public User getUserById(Long userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + userId)
            );
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    public User getUserByUsername(String username) {
        return userRepository
            .findByUsername(username)
            .orElseThrow(() ->
                new UserNotFoundException(
                    "User not found with username: " + username
                )
            );
    }

    // 用于认证的方法，不需要权限检查
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // 用于认证的方法，不需要权限检查
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // 用于认证的方法，不需要权限检查
    public User save(User user) {
        return userRepository.save(user);
    }

    @PreAuthorize("hasAuthority('USER:MANAGE:SUB')")
    public User updateRolesForUser(Long userId, Set<String> roleNames) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + userId)
            );

        Set<Role> roles = new HashSet<>();
        if (roleNames != null) {
            for (String roleName : roleNames) {
                Role role = roleRepository
                    .findByName(roleName)
                    .orElseThrow(() ->
                        new RuntimeException("Role not found: " + roleName)
                    );
                roles.add(role);
            }
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // 清除用户权限缓存，因为角色已更改（对缓存异常进行保护）
        try {
            permissionCacheService.evictUserPermissions(userId);
        } catch (Exception e) {
            auditService.logPermissionOperation(
                "CACHE_EVICT",
                "USER",
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getUsername(),
                null,
                null,
                "FAILURE",
                "Evict user permission cache failed: " + e.getMessage()
            );
        }

        return savedUser;
    }

    @PreAuthorize("hasAuthority('USER:STATUS:UPDATE') OR hasAuthority('USER:MANAGE:SUB') OR hasAuthority('USER:MANAGE:EDITOR')")
    public User updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new UserNotFoundException("User not found with id: " + userId)
            );

        User.UserStatus oldStatus = user.getStatus();
        user.setStatus(status);
        User savedUser = userRepository.save(user);

        // 清除用户权限缓存，确保状态变更实时生效（对缓存异常进行保护）
        try {
            permissionCacheService.evictUserPermissions(userId);
        } catch (Exception e) {
            auditService.logPermissionOperation(
                "CACHE_EVICT",
                "USER",
                savedUser.getId().toString(),
                savedUser.getUsername(),
                savedUser.getUsername(),
                null,
                null,
                "FAILURE",
                "Evict user permission cache failed: " + e.getMessage()
            );
        }

        // 记录审计日志
        String details = "User status changed from " + oldStatus + " to " + status;
        auditService.logPermissionOperation(
            "UPDATE_STATUS",
            "USER",
            savedUser.getId().toString(),
            savedUser.getUsername(),
            savedUser.getUsername(),
            null,
            null,
            "SUCCESS",
            details
        );

        return savedUser;
    }

    @PreAuthorize("hasAuthority('USER:MANAGE:SUB')")
    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            // 先清除用户权限缓存（对缓存异常进行保护）
            try {
                permissionCacheService.evictUserPermissions(userId);
                permissionCacheService.evictUserDocumentAssignments(userId);
            } catch (Exception e) {
                auditService.logPermissionOperation(
                    "CACHE_EVICT",
                    "USER",
                    userId.toString(),
                    null,
                    null,
                    null,
                    null,
                    "FAILURE",
                    "Evict user-related caches failed: " + e.getMessage()
                );
            }

            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }
}
