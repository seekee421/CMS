package com.cms.permissions.controller;

import com.cms.permissions.entity.User;
import com.cms.permissions.service.UserService;
import com.cms.permissions.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户认证相关API")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @org.springframework.beans.factory.annotation.Value("${security.login.maxWrongAttemptsPerDay:5}")
    private int maxWrongAttemptsPerDay;

    private String failedKeyForToday(String username) {
        String day = java.time.LocalDate.now().toString();
        return "login:failed:" + username + ":" + day;
    }

    @Operation(summary = "用户登录", description = "用户登录获取JWT令牌")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for username: " + loginRequest.getUsername());

            // 每日错误次数预检：当日达到上限则直接返回429，阻断后续认证与审计
            String username = loginRequest.getUsername();
            if (redisTemplate != null) {
                String failedKey = failedKeyForToday(username);
                Object val = redisTemplate.opsForValue().get(failedKey);
                long failed = 0L;
                if (val instanceof Number) {
                    failed = ((Number) val).longValue();
                } else if (val != null) {
                    try { failed = Long.parseLong(val.toString()); } catch (NumberFormatException ignore) {}
                }
                if (failed >= maxWrongAttemptsPerDay) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "当日密码错误次数已达上限，请明天再试");
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
                }
            }
            
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            System.out.println("Authentication successful");

            // 获取用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            if (user == null) {
                System.out.println("User not found: " + loginRequest.getUsername());
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户不存在");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            System.out.println("User found: " + user.getUsername());

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getUsername());
            System.out.println("JWT token generated");

            // 构建响应 - 简化roles处理
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            
            // 简化用户信息，避免序列化问题
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            // 暂时不包含roles，避免序列化问题
            response.put("user", userInfo);

            System.out.println("Response prepared successfully");
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            // 认证失败后递增当日错误次数，并设置在当日结束时过期
            try {
                if (redisTemplate != null) {
                    String key = failedKeyForToday(loginRequest.getUsername());
                    Long newCount = redisTemplate.opsForValue().increment(key);
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl == null || ttl <= 0) {
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();
                        java.time.LocalDateTime tomorrowStart = java.time.LocalDate.now().plusDays(1).atStartOfDay();
                        long secondsToMidnight = java.time.Duration.between(now, tomorrowStart).getSeconds();
                        redisTemplate.expire(key, secondsToMidnight, java.util.concurrent.TimeUnit.SECONDS);
                    }
                    System.out.println("Incremented failed login count to: " + newCount);
                }
            } catch (Exception ex) {
                System.out.println("Failed to update failed login counter: " + ex.getMessage());
            }
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        } catch (Exception e) {
            System.out.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "登录过程中发生错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            if (userService.findByUsername(registerRequest.getUsername()) != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "用户名已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // 检查邮箱是否已存在
            if (userService.findByEmail(registerRequest.getEmail()) != null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "邮箱已存在");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // 创建新用户
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setStatus(User.UserStatus.ACTIVE);

            User savedUser = userService.save(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "注册失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 登录请求DTO
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    // 注册请求DTO
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @Operation(summary = "密码找回", description = "预留密码找回接口：受理找回请求")
    @PostMapping("/recover")
    public ResponseEntity<Map<String, Object>> recover(@Valid @RequestBody RecoverPasswordRequest request) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "密码找回请求已受理");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
    }

    public static class RecoverPasswordRequest {
        private String identifier; // 用户名或邮箱
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
    }
}