package com.cms.permissions;

import com.cms.permissions.config.SecurityConfig;
import com.cms.permissions.security.CustomPermissionEvaluator;
import com.cms.permissions.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MethodSecurityExpressionHandler methodSecurityExpressionHandler;

    @MockitoBean
    private CustomPermissionEvaluator permissionEvaluator;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    /**
     * 测试安全配置Bean是否正确创建
     */
    @Test
    public void testSecurityBeansCreation() {
        assertNotNull(securityConfig, "SecurityConfig should be created");
        assertNotNull(passwordEncoder, "PasswordEncoder should be created");
        assertNotNull(authenticationManager, "AuthenticationManager should be created");
        assertNotNull(methodSecurityExpressionHandler, "MethodSecurityExpressionHandler should be created");
    }

    /**
     * 测试密码编码器功能
     */
    @Test
    public void testPasswordEncoder() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword, "Encoded password should not be null");
        assertNotEquals(rawPassword, encodedPassword, "Encoded password should be different from raw password");
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword), "Password should match");
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword), "Wrong password should not match");
    }

    /**
     * 测试公共端点访问（无需认证）
     */
    @Test
    public void testPublicEndpointsAccess() throws Exception {
        // 测试认证端点 - 这些端点可能不存在或返回错误，但不应该是403 Forbidden
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

        // 测试公共端点
        mockMvc.perform(get("/api/public/info"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));

        // 测试注册端点
        mockMvc.perform(get("/api/register"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    /**
     * 测试受保护端点需要认证
     */
    @Test
    public void testProtectedEndpointsRequireAuthentication() throws Exception {
        // 测试用户管理端点需要认证 - 可能返回401或403
        mockMvc.perform(get("/api/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Should return 401 or 403 for unauthenticated access");
                });

        // 测试文档端点需要认证
        mockMvc.perform(get("/api/documents"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Should return 401 or 403 for unauthenticated access");
                });

        // 测试评论端点需要认证
        mockMvc.perform(get("/api/comments"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Should return 401 or 403 for unauthenticated access");
                });
    }

    /**
     * 测试管理员端点需要ADMIN角色
     */
    @Test
    @WithMockUser(roles = {"USER"})
    public void testAdminEndpointsRequireAdminRole() throws Exception {
        // 普通用户访问管理员端点应该被拒绝
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试管理员端点允许ADMIN角色访问
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testAdminEndpointsAllowAdminRole() throws Exception {
        // ADMIN角色访问管理员端点应该被允许（不应该是403 Forbidden）
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    /**
     * 测试子管理员端点需要SUB_ADMIN角色
     */
    @Test
    @WithMockUser(roles = {"USER"})
    public void testSubAdminEndpointsRequireSubAdminRole() throws Exception {
        // 普通用户访问子管理员端点应该被拒绝
        mockMvc.perform(get("/api/subadmin/documents"))
                .andExpect(status().isForbidden());
    }

    /**
     * 测试子管理员端点允许SUB_ADMIN角色访问
     */
    @Test
    @WithMockUser(roles = {"SUB_ADMIN"})
    public void testSubAdminEndpointsAllowSubAdminRole() throws Exception {
        // SUB_ADMIN角色访问子管理员端点应该被允许（不应该是403 Forbidden）
        mockMvc.perform(get("/api/subadmin/documents"))
                .andExpect(result -> assertNotEquals(403, result.getResponse().getStatus()));
    }

    /**
     * 测试认证用户可以访问一般受保护的端点
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"USER:READ"})
    public void testAuthenticatedUserCanAccessProtectedEndpoints() throws Exception {
        // 认证用户访问受保护端点应该不会返回401（未认证）
        mockMvc.perform(get("/api/users/1"))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));
    }

    /**
     * 测试方法级安全表达式处理器配置
     */
    @Test
    public void testMethodSecurityExpressionHandler() {
        assertNotNull(methodSecurityExpressionHandler, "MethodSecurityExpressionHandler should not be null");
        // 验证表达式处理器已正确配置
        assertTrue(methodSecurityExpressionHandler instanceof DefaultMethodSecurityExpressionHandler, 
                  "Should be DefaultMethodSecurityExpressionHandler instance");
    }

    /**
     * 测试CSRF禁用配置
     */
    @Test
    @WithMockUser(username = "testuser", authorities = {"DOC:CREATE"})
    public void testCSRFDisabled() throws Exception {
        // POST请求不需要CSRF token
        mockMvc.perform(post("/api/documents")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest()); // 400因为请求体无效，但不是403 CSRF错误
    }

    /**
     * 测试会话管理配置（无状态）
     */
    @Test
    public void testStatelessSessionManagement() throws Exception {
        // 第一次请求 - 应该需要认证
        mockMvc.perform(get("/api/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Should require authentication");
                });

        // 第二次请求应该仍然是未认证状态（无状态）
        mockMvc.perform(get("/api/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403, "Should still require authentication (stateless)");
                });
    }
}