package com.cms.permissions;

import com.cms.permissions.controller.*;
import com.cms.permissions.entity.*;
import com.cms.permissions.service.*;
import com.cms.permissions.security.CustomPermissionEvaluator;
import com.cms.permissions.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.MockedStatic;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DocumentService documentService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private CustomPermissionEvaluator permissionEvaluator;
    




    private User testUser;
    private Document testDocument;
    private Comment testComment;

    @BeforeEach
    public void setUp() {
        // 创建测试数据
        long uniqueId = System.currentTimeMillis();
        
        Role testRole = new Role("ROLE_TEST_" + uniqueId, "Test Role " + uniqueId);
        testRole.setId(1L);

        testUser = new User("testuser_" + uniqueId, "encodedPassword", "test_" + uniqueId + "@example.com");
        testUser.setId(1L);
        testUser.setRoles(Set.of(testRole));

        testDocument = new Document("Test Document " + uniqueId, "Test content " + uniqueId, 1L);
        testDocument.setId(1L);
        testDocument.setStatus(Document.DocumentStatus.DRAFT);
        testDocument.setCreatedAt(LocalDateTime.now());

        testComment = new Comment("Test comment " + uniqueId, 1L, 1L);
        testComment.setId(1L);
        testComment.setCreatedAt(LocalDateTime.now());

        // 配置权限评估器模拟行为 - 让所有权限检查都通过
        when(permissionEvaluator.hasPermission(any(), any(), any(), any())).thenReturn(true);
        when(permissionEvaluator.hasPermission(any(), any(), any())).thenReturn(true);
    }

    // ========== UserController 测试 ==========

    @Test
    @WithMockUser(authorities = {"USER:MANAGE:SUB"})
    public void testCreateUser() throws Exception {
        // 准备测试数据
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("newuser@example.com");
        request.setRoleNames(Set.of("ROLE_USER"));

        User createdUser = new User("newuser", "encodedPassword", "newuser@example.com");
        createdUser.setId(2L);

        // 模拟service行为
        when(userService.createUser(anyString(), anyString(), anyString(), any()))
                .thenReturn(createdUser);

        // 执行测试
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));

        // 验证service调用
        verify(userService, times(1)).createUser("newuser", "password123", "newuser@example.com", Set.of("ROLE_USER"));
    }

    @Test
    @WithMockUser(authorities = {"USER:READ"})
    public void testGetUser() throws Exception {
        // 模拟service行为
        when(userService.getUserById(1L)).thenReturn(testUser);

        // 执行测试
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()));

        // 验证service调用
        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER:MANAGE:SUB"})
    public void testUpdateRolesForUser() throws Exception {
        // 准备测试数据
        UserController.UpdateRolesRequest request = new UserController.UpdateRolesRequest();
        request.setRoleNames(Set.of("ROLE_EDITOR"));

        User updatedUser = new User(testUser.getUsername(), testUser.getPassword(), testUser.getEmail());
        updatedUser.setId(1L);

        // 模拟service行为
        when(userService.updateRolesForUser(1L, Set.of("ROLE_EDITOR"))).thenReturn(updatedUser);

        // 执行测试
        mockMvc.perform(put("/api/users/1/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        // 验证service调用
        verify(userService, times(1)).updateRolesForUser(1L, Set.of("ROLE_EDITOR"));
    }

    @Test
    @WithMockUser(authorities = {"USER:MANAGE:SUB"})
    public void testDeleteUser() throws Exception {
        // 模拟service行为
        when(userService.deleteUser(1L)).thenReturn(true);

        // 执行测试
        mockMvc.perform(delete("/api/users/1")
                .with(csrf()))
                .andExpect(status().isOk());

        // 验证service调用
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @WithMockUser(authorities = {"USER:MANAGE:SUB"})
    public void testDeleteUserNotFound() throws Exception {
        // 模拟service行为
        when(userService.deleteUser(999L)).thenReturn(false);

        // 执行测试
        mockMvc.perform(delete("/api/users/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        // 验证service调用
        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    public void testCreateUserWithoutPermission() throws Exception {
        // 准备测试数据
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("newuser@example.com");

        // 执行测试（没有权限）
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 验证service没有被调用
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), any());
    }

    // ========== DocumentController 测试 ==========

    @Test
    @WithMockUser(authorities = {"DOC:CREATE"})
    public void testCreateDocument() throws Exception {
        // 准备测试数据
        Document newDocument = new Document("New Document", "New content", null);
        Document createdDocument = new Document("New Document", "New content", 1L);
        createdDocument.setId(2L);

        // 模拟service行为
        when(documentService.createDocument(any(Document.class), anyLong())).thenReturn(createdDocument);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
             mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
             
             // 执行测试
             mockMvc.perform(post("/api/documents")
                     .with(csrf())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(objectMapper.writeValueAsString(newDocument)))
                     .andExpect(status().isOk())
                     .andExpect(jsonPath("$.id").value(2L))
                     .andExpect(jsonPath("$.title").value("New Document"))
                     .andExpect(jsonPath("$.content").value("New content"));

             // 验证service调用
             verify(documentService, times(1)).createDocument(any(Document.class), anyLong());
         }
    }

    @Test
    @WithMockUser(authorities = {"DOC:VIEW:LOGGED"})
    public void testGetDocument() throws Exception {
        // 模拟service行为
        when(documentService.getDocument(1L)).thenReturn(testDocument);

        // 执行测试
        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value(testDocument.getTitle()))
                .andExpect(jsonPath("$.content").value(testDocument.getContent()));

        // 验证service调用
        verify(documentService, times(1)).getDocument(1L);
    }

    @Test
    @WithMockUser(authorities = {"DOC:EDIT"})
    public void testUpdateDocument() throws Exception {
        // 准备测试数据
        Document updatedDocument = new Document("Updated Title", "Updated content", null);
        Document resultDocument = new Document("Updated Title", "Updated content", 1L);
        resultDocument.setId(1L);

        // 模拟service行为
        when(documentService.updateDocument(eq(1L), any(Document.class))).thenReturn(resultDocument);

        // 执行测试
        mockMvc.perform(put("/api/documents/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDocument)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated content"));

        // 验证service调用
        verify(documentService, times(1)).updateDocument(eq(1L), any(Document.class));
    }

    @Test
    @WithMockUser(authorities = {"DOC:DELETE"})
    public void testDeleteDocument() throws Exception {
        // 模拟service行为
        doNothing().when(documentService).deleteDocument(1L);

        // 执行测试
        mockMvc.perform(delete("/api/documents/1")
                .with(csrf()))
                .andExpect(status().isOk());

        // 验证service调用
        verify(documentService, times(1)).deleteDocument(1L);
    }

    @Test
    @WithMockUser(authorities = {"DOC:PUBLISH"})
    public void testPublishDocument() throws Exception {
        // 准备测试数据
        Document publishedDocument = new Document(testDocument.getTitle(), testDocument.getContent(), testDocument.getCreatedBy());
        publishedDocument.setId(1L);
        publishedDocument.setStatus(Document.DocumentStatus.PUBLISHED);

        // 模拟service行为
        when(documentService.publishDocument(1L)).thenReturn(publishedDocument);

        // 执行测试
        mockMvc.perform(put("/api/documents/1/publish")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        // 验证service调用
        verify(documentService, times(1)).publishDocument(1L);
    }

    @Test
    @WithMockUser
    public void testGetDocumentsForUser() throws Exception {
        // 准备测试数据
        List<Document> documents = Arrays.asList(testDocument);

        // 模拟service行为
        when(documentService.getDocumentsForUser(anyLong())).thenReturn(documents);

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);
            
            // 执行测试
            mockMvc.perform(get("/api/documents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value(testDocument.getTitle()));

            // 验证service调用
            verify(documentService, times(1)).getDocumentsForUser(anyLong());
        }
    }

    // ========== CommentController 测试 ==========

    @Test
    @WithMockUser(authorities = {"COMMENT:CREATE"})
    public void testCreateComment() throws Exception {
        // 准备测试数据
        CommentController.CreateCommentRequest request = new CommentController.CreateCommentRequest();
        request.setDocumentId(1L);
        request.setContent("New comment");
        request.setUserId(1L);

        Comment createdComment = new Comment("New comment", 1L, 1L);
        createdComment.setId(2L);

        // 模拟service行为
        when(commentService.createComment(1L, "New comment", 1L)).thenReturn(createdComment);

        // 执行测试
        mockMvc.perform(post("/api/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.content").value("New comment"))
                .andExpect(jsonPath("$.documentId").value(1L))
                .andExpect(jsonPath("$.userId").value(1L));

        // 验证service调用
        verify(commentService, times(1)).createComment(1L, "New comment", 1L);
    }

    @Test
    @WithMockUser(authorities = {"COMMENT:READ"})
    public void testGetCommentsForDocument() throws Exception {
        // 准备测试数据
        List<Comment> comments = Arrays.asList(testComment);

        // 模拟service行为
        when(commentService.getCommentsForDocument(1L)).thenReturn(comments);

        // 执行测试
        mockMvc.perform(get("/api/comments/document/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].content").value(testComment.getContent()));

        // 验证service调用
        verify(commentService, times(1)).getCommentsForDocument(1L);
    }

    @Test
    public void testCreateCommentWithoutPermission() throws Exception {
        // 准备测试数据
        CommentController.CreateCommentRequest request = new CommentController.CreateCommentRequest();
        request.setDocumentId(1L);
        request.setContent("New comment");
        request.setUserId(1L);

        // 执行测试（没有权限）
        mockMvc.perform(post("/api/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 验证service没有被调用
        verify(commentService, never()).createComment(anyLong(), anyString(), anyLong());
    }

    // ========== 验证测试 ==========

    @Test
    @WithMockUser(authorities = {"USER:MANAGE:SUB"})
    public void testCreateUserWithInvalidData() throws Exception {
        // 准备无效测试数据
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUsername(""); // 无效：空用户名
        request.setPassword("123"); // 无效：密码太短
        request.setEmail("invalid-email"); // 无效：邮箱格式错误

        // 执行测试
        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证service没有被调用
        verify(userService, never()).createUser(anyString(), anyString(), anyString(), any());
    }


}