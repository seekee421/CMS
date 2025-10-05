package com.cms.permissions.controller;

import com.cms.permissions.entity.User;
import com.cms.permissions.repository.UserRepository;
import com.cms.permissions.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('USER:READ')")
@Tag(name = "用户管理", description = "用户管理 API")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "创建用户", description = "创建一个新的用户")
    @PostMapping
    @PreAuthorize("hasAuthority('USER:CREATE')")
    public ResponseEntity<User> createUser(
        @Parameter(
            description = "用户名",
            example = "john_doe"
        ) @RequestParam String username,
        @Parameter(description = "用户密码") @RequestParam String password,
        @Parameter(
            description = "用户邮箱",
            example = "john@example.com"
        ) @RequestParam String email,
        @Parameter(description = "角色名称集合") @RequestParam(
            required = false
        ) Set<String> roleNames
    ) {
        User user = userService.createUser(
            username,
            password,
            email,
            roleNames
        );
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "根据ID获取用户",
        description = "根据用户ID获取用户详情"
    )
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
        @Parameter(description = "用户ID", example = "1") @PathVariable Long id
    ) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "根据用户名获取用户",
        description = "根据用户名获取用户详情"
    )
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(
        @Parameter(
            description = "用户名",
            example = "john_doe"
        ) @PathVariable String username
    ) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "获取所有用户", description = "获取系统中的所有用户")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "更新用户角色", description = "更新用户的角色集合")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER:UPDATE') OR hasAuthority('USER:MANAGE:SUB') OR hasAuthority('USER:MANAGE:EDITOR')")
    public ResponseEntity<User> updateUserRoles(
        @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
        @Parameter(description = "角色名称集合") @RequestBody Set<
            String
        > roleNames
    ) {
        User user = userService.updateRolesForUser(id, roleNames);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "更新用户状态", description = "更新用户启用/停用/冻结状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER:STATUS:UPDATE') OR hasAuthority('USER:MANAGE:SUB') OR hasAuthority('USER:MANAGE:EDITOR')")
    public ResponseEntity<User> updateUserStatus(
        @Parameter(description = "用户ID", example = "1") @PathVariable Long id,
        @Parameter(description = "用户状态: ACTIVE/INACTIVE/SUSPENDED", example = "ACTIVE") @RequestParam User.UserStatus status
    ) {
        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:DELETE')")
    public ResponseEntity<Boolean> deleteUser(
        @Parameter(description = "用户ID", example = "1") @PathVariable Long id
    ) {
        boolean result = userService.deleteUser(id);
        return ResponseEntity.ok(result);
    }
}
