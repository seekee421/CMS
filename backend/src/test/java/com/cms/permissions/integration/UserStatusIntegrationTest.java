package com.cms.permissions.integration;

import com.cms.permissions.controller.AuthController.LoginRequest;
import com.cms.permissions.entity.User;
import com.cms.permissions.entity.AuditLog;
import com.cms.permissions.repository.AuditLogRepository;
import com.cms.permissions.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        String body = objectMapper.writeValueAsString(req);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        return node.get("token").asText();
    }
    @Test
    @DisplayName("状态变更后100ms内重试登录仍返回401，验证实时生效")
    void suspendedUserRetryLoginWithin100msUnauthorized() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();
        Long userId = registerUser("user_rt2", "user_rt2@example.com", "pass123");
        assertThat(userId).isNotNull();
        String userToken = loginAndGetToken("user_rt2", "pass123");
        assertThat(userToken).isNotEmpty();

        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", User.UserStatus.SUSPENDED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));

        LoginRequest req1 = new LoginRequest();
        req1.setUsername("user_rt2");
        req1.setPassword("pass123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req1)))
            .andExpect(status().isUnauthorized());

        Thread.sleep(50);

        LoginRequest req2 = new LoginRequest();
        req2.setUsername("user_rt2");
        req2.setPassword("pass123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req2)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("多次错误密码登录不产生LOGIN审计，且未触发锁定策略（如未实现）")
    void repeatedWrongPasswordDoesNotLockAccount_noLoginAudit() throws Exception {
        Long userId = registerUser("fail_user", "fail_user@example.com", "GoodPass1!");
        assertThat(userId).isNotNull();
        long beforeLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        for (int i = 0; i < 3; i++) {
            com.cms.permissions.controller.AuthController.LoginRequest req = new com.cms.permissions.controller.AuthController.LoginRequest();
            req.setUsername("fail_user");
            req.setPassword("WrongPass!" + i);
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
        }
        long afterLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        assertThat(afterLoginAudit).isEqualTo(beforeLoginAudit);
        String token = loginAndGetToken("fail_user", "GoodPass1!");
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("已删除用户登录返回401且不产生LOGIN审计日志")
    void deletedUserLoginUnauthorizedNoAudit() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();
        Long userId = registerUser("deleted_user", "deleted_user@example.com", "DelPass1!");
        assertThat(userId).isNotNull();
        long beforeLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        mockMvc.perform(delete("/api/users/" + userId)
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
        com.cms.permissions.controller.AuthController.LoginRequest req = new com.cms.permissions.controller.AuthController.LoginRequest();
        req.setUsername("deleted_user");
        req.setPassword("DelPass1!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
        long afterLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        assertThat(afterLoginAudit).isEqualTo(beforeLoginAudit);
    }
    @Test
    @DisplayName("管理员更新用户状态为INACTIVE并记录审计日志")
    void adminUpdatesUserStatus_andAuditLogged() throws Exception {
        // 登录管理员
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();

        // 创建一个普通用户供测试（使用管理员权限）
        MvcResult createRes = mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("username", "testuser_status")
                .param("password", "pass123")
                .param("email", "testuser_status@example.com"))
            .andExpect(status().isOk())
            .andReturn();
        String createJson = createRes.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode createdNode = objectMapper.readTree(createJson);
        Long userId = createdNode.get("id").asLong();
        String username = createdNode.get("username").asText();

        // 自定义请求头以便审计记录IP与UA
        String ip = "203.0.113.7";
        String ua = "JUnit/MockMvc Test Agent";

        // 调用状态更新接口
        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Forwarded-For", ip)
                .header("User-Agent", ua)
                .param("status", User.UserStatus.INACTIVE.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));

        // 验证数据库中的状态已变更
        User updated = userRepository.findById(userId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(User.UserStatus.INACTIVE);

        // 验证审计日志写入（精确字段断言）
        AuditLog log = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER")
            .stream()
            .filter(a -> String.valueOf(userId).equals(a.getResourceId()))
            .reduce((a, b) -> a.getTimestamp().isAfter(b.getTimestamp()) ? a : b)
            .orElse(null);
        assertThat(log).isNotNull();
        assertThat(log.getOperationType()).isEqualTo("UPDATE_STATUS");
        assertThat(log.getResourceType()).isEqualTo("USER");
        assertThat(log.getResourceId()).isEqualTo(String.valueOf(userId));
        assertThat(log.getResourceName()).isEqualTo(username);
        assertThat(log.getTargetUser()).isEqualTo(username);
        assertThat(log.getUsername()).isEqualTo("admin");
        assertThat(log.getResult()).isEqualTo("SUCCESS");
        assertThat(log.getDetails()).isEqualTo("User status changed from ACTIVE to INACTIVE");
        assertThat(log.getIpAddress()).isEqualTo(ip);
        assertThat(log.getUserAgent()).isEqualTo(ua);
        assertThat(log.getTimestamp()).isNotNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    private Long registerUser(String username, String email, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        String json = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
        return node.get("user").get("id").asLong();
    }

    @Test
    @DisplayName("用户冻结后立即无法再次登录（实时生效）")
    void userSuspensionBlocksLoginRealTime() throws Exception {
        // 管理员登录
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();

        // 注册一个普通用户
        Long userId = registerUser("user_rt", "user_rt@example.com", "pass123");
        assertThat(userId).isNotNull();

        // 初次登录应成功
        String userToken = loginAndGetToken("user_rt", "pass123");
        assertThat(userToken).isNotEmpty();

        // 管理员将其状态更新为 SUSPENDED
        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", User.UserStatus.SUSPENDED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));

        // 再次尝试登录，应该返回 401
        LoginRequest req = new LoginRequest();
        req.setUsername("user_rt");
        req.setPassword("pass123");
        String body = objectMapper.writeValueAsString(req);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("无权限用户尝试更新他人状态应返回403")
    void unauthorizedUserCannotUpdateOthersStatus() throws Exception {
        // 注册两个普通用户
        Long targetUserId = registerUser("target_user", "target_user@example.com", "tpass123");
        Long attackerUserId = registerUser("attacker_user", "attacker_user@example.com", "apass123");
        assertThat(targetUserId).isNotNull();
        assertThat(attackerUserId).isNotNull();

        // 攻击者登录获取令牌
        String attackerToken = loginAndGetToken("attacker_user", "apass123");
        assertThat(attackerToken).isNotEmpty();

        // 尝试更新他人状态，应403（缺少权限）
        mockMvc.perform(put("/api/users/" + targetUserId + "/status")
                .header("Authorization", "Bearer " + attackerToken)
                .param("status", User.UserStatus.INACTIVE.name()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("登录行为验证：ACTIVE→INACTIVE→ACTIVE→SUSPENDED")
    void loginBehaviorAcrossStatuses() throws Exception {
        // 管理员登录
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();

        // 注册普通用户
        Long userId = registerUser("stateUser","state.user@example.com","P@ssw0rd!");
        assertThat(userId).isNotNull();

        // 初始 ACTIVE 登录成功
        LoginRequest req = new LoginRequest();
        req.setUsername("stateUser");
        req.setPassword("P@ssw0rd!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").isNotEmpty());

        // 更新为 INACTIVE，登录应401
        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", User.UserStatus.INACTIVE.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());

        // 恢复为 ACTIVE，登录应成功
        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", User.UserStatus.ACTIVE.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.token").isNotEmpty());

        // 变更为 SUSPENDED，登录应401
        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .param("status", User.UserStatus.SUSPENDED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("审计日志覆盖：ACTIVE→SUSPENDED→ACTIVE 的 details 与字段断言")
    void auditDetailsActiveSuspendedActive() throws Exception {
        String adminToken = loginAndGetToken("admin", "admin123");
        assertThat(adminToken).isNotEmpty();

        MvcResult createRes = mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("username", "audit_cycle")
                .param("password", "pass123")
                .param("email", "audit_cycle@example.com"))
            .andExpect(status().isOk())
            .andReturn();
        String createJson = createRes.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode createdNode = objectMapper.readTree(createJson);
        Long userId = createdNode.get("id").asLong();
        String username = createdNode.get("username").asText();

        String ip = "198.51.100.42";
        String ua = "JUnit/MockMvc Audit Agent";

        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Forwarded-For", ip)
                .header("User-Agent", ua)
                .param("status", User.UserStatus.SUSPENDED.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUSPENDED"));

        User suspended = userRepository.findById(userId).orElseThrow();
        assertThat(suspended.getStatus()).isEqualTo(User.UserStatus.SUSPENDED);

        AuditLog log1 = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER")
            .stream()
            .filter(a -> String.valueOf(userId).equals(a.getResourceId()))
            .reduce((a, b) -> a.getTimestamp().isAfter(b.getTimestamp()) ? a : b)
            .orElse(null);
        assertThat(log1).isNotNull();
        assertThat(log1.getOperationType()).isEqualTo("UPDATE_STATUS");
        assertThat(log1.getResourceType()).isEqualTo("USER");
        assertThat(log1.getResourceId()).isEqualTo(String.valueOf(userId));
        assertThat(log1.getResourceName()).isEqualTo(username);
        assertThat(log1.getTargetUser()).isEqualTo(username);
        assertThat(log1.getUsername()).isEqualTo("admin");
        assertThat(log1.getResult()).isEqualTo("SUCCESS");
        assertThat(log1.getDetails()).isEqualTo("User status changed from ACTIVE to SUSPENDED");
        assertThat(log1.getIpAddress()).isEqualTo(ip);
        assertThat(log1.getUserAgent()).isEqualTo(ua);
        assertThat(log1.getTimestamp()).isNotNull();
        assertThat(log1.getCreatedAt()).isNotNull();

        mockMvc.perform(put("/api/users/" + userId + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Forwarded-For", ip)
                .header("User-Agent", ua)
                .param("status", User.UserStatus.ACTIVE.name()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"));

        User activeAgain = userRepository.findById(userId).orElseThrow();
        assertThat(activeAgain.getStatus()).isEqualTo(User.UserStatus.ACTIVE);

        AuditLog log2 = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER")
            .stream()
            .filter(a -> String.valueOf(userId).equals(a.getResourceId()))
            .reduce((a, b) -> a.getTimestamp().isAfter(b.getTimestamp()) ? a : b)
            .orElse(null);
        assertThat(log2).isNotNull();
        assertThat(log2.getOperationType()).isEqualTo("UPDATE_STATUS");
        assertThat(log2.getResourceType()).isEqualTo("USER");
        assertThat(log2.getResourceId()).isEqualTo(String.valueOf(userId));
        assertThat(log2.getResourceName()).isEqualTo(username);
        assertThat(log2.getTargetUser()).isEqualTo(username);
        assertThat(log2.getUsername()).isEqualTo("admin");
        assertThat(log2.getResult()).isEqualTo("SUCCESS");
        assertThat(log2.getDetails()).isEqualTo("User status changed from SUSPENDED to ACTIVE");
        assertThat(log2.getIpAddress()).isEqualTo(ip);
        assertThat(log2.getUserAgent()).isEqualTo(ua);
        assertThat(log2.getTimestamp()).isNotNull();
        assertThat(log2.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("403禁止的状态更新不产生审计日志")
    void forbiddenUpdateDoesNotCreateAudit() throws Exception {
        Long targetUserId = registerUser("forbid_target", "forbid_target@example.com", "T@123456");
        Long attackerUserId = registerUser("forbid_attacker", "forbid_attacker@example.com", "A@123456");
        assertThat(targetUserId).isNotNull();
        assertThat(attackerUserId).isNotNull();

        String attackerToken = loginAndGetToken("forbid_attacker", "A@123456");
        assertThat(attackerToken).isNotEmpty();

        java.util.List<AuditLog> before = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER")
            .stream()
            .filter(a -> String.valueOf(targetUserId).equals(a.getResourceId()))
            .collect(java.util.stream.Collectors.toList());

        mockMvc.perform(put("/api/users/" + targetUserId + "/status")
                .header("Authorization", "Bearer " + attackerToken)
                .param("status", User.UserStatus.INACTIVE.name()))
            .andExpect(status().isForbidden());

        java.util.List<AuditLog> after = auditLogRepository
            .findByOperationTypeAndResourceType("UPDATE_STATUS", "USER")
            .stream()
            .filter(a -> String.valueOf(targetUserId).equals(a.getResourceId()))
            .collect(java.util.stream.Collectors.toList());
        assertThat(after.size()).isEqualTo(before.size());

        boolean attackerLogExists = after.stream().anyMatch(a -> "forbid_attacker".equals(a.getUsername()));
        assertThat(attackerLogExists).isFalse();
    }

    @Test
    @DisplayName("登录密码错误返回401且不产生LOGIN审计日志")
    void loginWrongPasswordNoAudit() throws Exception {
        Long userId = registerUser("badpwd_user", "badpwd_user@example.com", "GoodPass1!");
        assertThat(userId).isNotNull();

        long beforeLoginAudit = auditLogRepository.countByOperationType("LOGIN");

        LoginRequest req = new LoginRequest();
        req.setUsername("badpwd_user");
        req.setPassword("WrongPass!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());

        long afterLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        assertThat(afterLoginAudit).isEqualTo(beforeLoginAudit);
    }

    @Test
    @DisplayName("当日错误密码达到5次后返回429，正确密码也被阻断，且不产生LOGIN审计日志")
    void dailyWrongPasswordLimitBlocksLoginEvenWithCorrectPassword429_noLoginAudit() throws Exception {
        Long userId = registerUser("limit_user", "limit_user@example.com", "GoodPass1!");
        assertThat(userId).isNotNull();

        long beforeLoginAudit = auditLogRepository.countByOperationType("LOGIN");

        LoginRequest req = new LoginRequest();
        req.setUsername("limit_user");

        // 前5次错误密码返回401
        for (int i = 0; i < 5; i++) {
            req.setPassword("WrongPass!" + i);
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
        }

        // 第6次错误密码返回429（超过当日上限）
        req.setPassword("WrongPass!final");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isTooManyRequests());

        // 即使使用正确密码也返回429（当日阻断）
        req.setPassword("GoodPass1!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isTooManyRequests());

        long afterLoginAudit = auditLogRepository.countByOperationType("LOGIN");
        assertThat(afterLoginAudit).isEqualTo(beforeLoginAudit);
    }
}