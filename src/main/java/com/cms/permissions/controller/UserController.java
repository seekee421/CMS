package com.cms.permissions.controller;

import com.cms.permissions.entity.User;
import com.cms.permissions.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('USER:MANAGE:SUB') OR hasAuthority('USER:MANAGE:EDITOR')")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRoleNames());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:READ')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER:MANAGE:SUB')")
    public ResponseEntity<User> updateRolesForUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolesRequest request) {
        User updatedUser = userService.updateRolesForUser(id, request.getRoleNames());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:MANAGE:SUB')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // Request DTOs
    public static class CreateUserRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        private String password;

        @jakarta.validation.constraints.Email(message = "Email should be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;

        private Set<String> roleNames;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Set<String> getRoleNames() { return roleNames; }
        public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }
    }

    public static class UpdateRolesRequest {
        @jakarta.validation.constraints.NotEmpty(message = "At least one role must be specified")
        private Set<String> roleNames;

        // Getters and setters
        public Set<String> getRoleNames() { return roleNames; }
        public void setRoleNames(Set<String> roleNames) { this.roleNames = roleNames; }
    }
}