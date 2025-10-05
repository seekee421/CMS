package com.cms.permissions.integration;

import com.cms.permissions.entity.User;
import com.cms.permissions.entity.Role;
import com.cms.permissions.entity.AuditLog;
import com.cms.permissions.repository.UserRepository;
import com.cms.permissions.repository.RoleRepository;
import com.cms.permissions.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class SecurityAnnotationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("类级别 @PreAuthorize(USER:READ) 生效：拥有 USER:READ 可访问 /api/users")
    @WithMockUser(username = "reader", authorities = {"USER:READ"})
    void classLevelPreAuthorizeAllowsRead() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("类级别 @PreAuthorize(USER:READ) 生效：无 USER:READ 访问 /api/users 返回 403")
    @WithMockUser(username = "noReader", authorities = {"USER:CREATE"})
    void classLevelPreAuthorizeDeniesWithoutRead() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:CREATE)：拥有 USER:CREATE 可创建用户")
    @WithMockUser(username = "creator", authorities = {"USER:CREATE", "USER:READ"})
    void createUserRequiresCreateAuthority() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "sec_user1")
                .param("password", "P@ssw0rd!")
                .param("email", "sec_user1@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("sec_user1"));
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:CREATE)：无 USER:CREATE 创建用户返回 403")
    @WithMockUser(username = "readerOnly", authorities = {"USER:READ"})
    void createUserDeniedWithoutCreateAuthority() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "sec_user2")
                .param("password", "P@ssw0rd!")
                .param("email", "sec_user2@example.com"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:UPDATE/USER:MANAGE:SUB/USER:MANAGE:EDITOR)：拥有 USER:UPDATE 可更新角色")
    @WithMockUser(username = "roleUpdater", authorities = {"USER:UPDATE", "USER:READ"})
    void updateUserRolesRequiresProperAuthority_update() throws Exception {
        // 准备：创建目标用户与角色
        Role editorRole = roleRepository.findByName("ROLE_EDITOR").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_EDITOR");
            return roleRepository.save(r);
        });
        User user = new User();
        user.setUsername("roles_target1");
        user.setPassword("P@ssw0rd!");
        user.setEmail("roles_target1@example.com");
        user = userRepository.save(user);

        mockMvc.perform(put("/api/users/" + user.getId() + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(editorRole.getName()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("roles_target1"));
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:UPDATE/USER:MANAGE:SUB/USER:MANAGE:EDITOR)：无权限更新角色返回 403")
    @WithMockUser(username = "roleReader", authorities = {"USER:READ"})
    void updateUserRolesDeniedWithoutAuthority() throws Exception {
        Role editorRole = roleRepository.findByName("ROLE_EDITOR").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_EDITOR");
            return roleRepository.save(r);
        });
        User user = new User();
        user.setUsername("roles_target2");
        user.setPassword("P@ssw0rd!");
        user.setEmail("roles_target2@example.com");
        user = userRepository.save(user);

        mockMvc.perform(put("/api/users/" + user.getId() + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(editorRole.getName()))))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:MANAGE:SUB)：拥有 USER:MANAGE:SUB 可更新角色")
    @WithMockUser(username = "subManager", authorities = {"USER:MANAGE:SUB", "USER:READ"})
    void updateUserRolesAllowedWithManageSub() throws Exception {
        Role editorRole = roleRepository.findByName("ROLE_EDITOR").orElseGet(() -> {
            Role r = new Role();
            r.setName("ROLE_EDITOR");
            return roleRepository.save(r);
        });
        User user = new User();
        user.setUsername("roles_target3");
        user.setPassword("P@ssw0rd!");
        user.setEmail("roles_target3@example.com");
        user = userRepository.save(user);

        mockMvc.perform(put("/api/users/" + user.getId() + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(editorRole.getName()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("roles_target3"));
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:STATUS:UPDATE/USER:MANAGE:SUB/USER:MANAGE:EDITOR)：拥有 USER:STATUS:UPDATE 可更新状态")
    @WithMockUser(username = "statusUpdater", authorities = {"USER:STATUS:UPDATE", "USER:READ"})
    void updateUserStatusRequiresProperAuthority_statusUpdate() throws Exception {
        User user = new User();
        user.setUsername("status_target1");
        user.setPassword("P@ssw0rd!");
        user.setEmail("status_target1@example.com");
        user = userRepository.save(user);

        mockMvc.perform(put("/api/users/" + user.getId() + "/status")
                .param("status", User.UserStatus.INACTIVE.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(User.UserStatus.INACTIVE.name()));
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:STATUS:UPDATE/USER:MANAGE:SUB/USER:MANAGE:EDITOR)：无权限更新状态返回 403，且不产生审计日志")
    @WithMockUser(username = "statusReader", authorities = {"USER:READ"})
    void updateUserStatusDeniedWithoutAuthority_noAudit() throws Exception {
        User user = new User();
        user.setUsername("status_target2");
        user.setPassword("P@ssw0rd!");
        user.setEmail("status_target2@example.com");
        user = userRepository.save(user);

        mockMvc.perform(put("/api/users/" + user.getId() + "/status")
                .param("status", User.UserStatus.SUSPENDED.name()))
            .andExpect(status().isForbidden());

        // 验证未产生审计日志（AOP仅在服务方法执行时触发，403拒绝在控制器层）
        List<AuditLog> logs = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER");
        final String targetResourceId = String.valueOf(user.getId());
        boolean hasAuditForUser = logs.stream()
            .anyMatch(a -> targetResourceId.equals(a.getResourceId()));
        assertThat(hasAuditForUser).isFalse();
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:DELETE)：拥有 USER:DELETE 可删除用户")
    @WithMockUser(username = "deleter", authorities = {"USER:DELETE", "USER:READ"})
    void deleteUserRequiresDeleteAuthority() throws Exception {
        User user = new User();
        user.setUsername("delete_target1");
        user.setPassword("P@ssw0rd!");
        user.setEmail("delete_target1@example.com");
        user = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$." + "boolean").doesNotExist()); // 仅校验200
    }

    @Test
    @DisplayName("方法级别 @PreAuthorize(USER:DELETE)：无 USER:DELETE 删除用户返回 403")
    @WithMockUser(username = "noDeleter", authorities = {"USER:READ"})
    void deleteUserDeniedWithoutDeleteAuthority() throws Exception {
        User user = new User();
        user.setUsername("delete_target2");
        user.setPassword("P@ssw0rd!");
        user.setEmail("delete_target2@example.com");
        user = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + user.getId()))
            .andExpect(status().isForbidden());
    }
}