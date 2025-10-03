package com.cms.permissions.util;

import com.cms.permissions.entity.*;
import com.cms.permissions.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();
        initializeAdminUser();
    }

    private void initializePermissions() {
        // Define all required permissions
        List<Permission> permissions = Arrays.asList(
            new Permission("DOC:CREATE", "Create documents"),
            new Permission("DOC:EDIT", "Edit documents"),
            new Permission("DOC:PUBLISH", "Publish documents"),
            new Permission("DOC:DELETE", "Delete documents"),
            new Permission("DOC:APPROVE:ALL", "Approve all documents"),
            new Permission("DOC:APPROVE:ASSIGNED", "Approve assigned documents"),
            new Permission("DOC:VIEW:LOGGED", "View documents when logged in"),
            new Permission("DOC:DOWNLOAD", "Download documents"),
            new Permission("DOC:ASSIGN", "Assign users to documents"),
            new Permission("COMMENT:CREATE", "Create comments"),
            new Permission("COMMENT:MANAGE", "Manage comments"),
            new Permission("USER:MANAGE:SUB", "Manage sub-admins and editors"),
            new Permission("USER:MANAGE:EDITOR", "Manage editors"),
            new Permission("USER:READ", "Read user information")
        );

        for (Permission permission : permissions) {
            permissionRepository.findByCode(permission.getCode())
                .orElseGet(() -> {
                    System.out.println("Creating permission: " + permission.getCode());
                    return permissionRepository.save(permission);
                });
        }
    }

    private void initializeRoles() {
        // Create roles and assign permissions
        createRoleIfNotExists("ROLE_ADMIN", "System Administrator with all permissions",
            getAllPermissionCodes());

        createRoleIfNotExists("ROLE_SUB_ADMIN", "Sub Administrator with limited permissions",
            "DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:APPROVE:ASSIGNED",
            "DOC:VIEW:LOGGED", "DOC:DOWNLOAD", "DOC:ASSIGN", "COMMENT:CREATE",
            "COMMENT:MANAGE", "USER:MANAGE:EDITOR", "USER:READ");

        createRoleIfNotExists("ROLE_EDITOR", "Editor with document editing permissions",
            "DOC:EDIT", "DOC:PUBLISH", "DOC:VIEW:LOGGED", "DOC:DOWNLOAD",
            "COMMENT:CREATE", "COMMENT:MANAGE", "USER:READ");

        createRoleIfNotExists("ROLE_USER", "Regular user with viewing permissions",
            "DOC:VIEW:LOGGED", "DOC:DOWNLOAD", "COMMENT:CREATE", "USER:READ");
    }

    private void createRoleIfNotExists(String roleName, String description, String... permissionCodes) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role role = new Role(roleName, description);
            for (String code : permissionCodes) {
                Optional<Permission> permission = permissionRepository.findByCode(code);
                permission.ifPresent(role::addPermission);
            }
            System.out.println("Creating role: " + roleName);
            roleRepository.save(role);
        }
    }

    private void initializeAdminUser() {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isEmpty()) {
            // Find the admin role
            Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole.isPresent()) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), "admin@cms.com");
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.getRoles().add(adminRole.get());

                System.out.println("Creating admin user with username 'admin' and password 'admin123'");
                userRepository.save(admin);
            }
        }
    }

    private String[] getAllPermissionCodes() {
        return new String[] {
            "DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:DELETE", "DOC:APPROVE:ALL",
            "DOC:APPROVE:ASSIGNED", "DOC:VIEW:LOGGED", "DOC:DOWNLOAD", "DOC:ASSIGN",
            "COMMENT:CREATE", "COMMENT:MANAGE", "USER:MANAGE:SUB", "USER:MANAGE:EDITOR", "USER:READ"
        };
    }
}