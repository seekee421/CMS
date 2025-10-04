package com.cms.permissions;

import com.cms.permissions.entity.*;
import com.cms.permissions.exception.ResourceNotFoundException;
import com.cms.permissions.exception.UserNotFoundException;
import com.cms.permissions.repository.*;
import com.cms.permissions.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ServiceTest {

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private DocumentAssignmentRepository documentAssignmentRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private CommentRepository commentRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private DocumentService documentService;
    private UserService userService;
    private CommentService commentService;

    private User testUser;
    private Role testRole;
    private Document testDocument;
    private Comment testComment;

    @BeforeEach
    public void setUp() {
        // 初始化服务
        documentService = new DocumentService();
        userService = new UserService();
        commentService = new CommentService();

        // 使用反射设置私有字段
        setField(documentService, "documentRepository", documentRepository);
        setField(documentService, "documentAssignmentRepository", documentAssignmentRepository);
        setField(userService, "userRepository", userRepository);
        setField(userService, "roleRepository", roleRepository);
        setField(userService, "passwordEncoder", passwordEncoder);
        setField(commentService, "commentRepository", commentRepository);
        setField(commentService, "documentRepository", documentRepository);
        setField(commentService, "userRepository", userRepository);

        // 创建测试数据
        long uniqueId = System.currentTimeMillis();
        
        testRole = new Role("ROLE_TEST_" + uniqueId, "Test Role " + uniqueId);
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
    }

    // 辅助方法：使用反射设置私有字段
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    // ========== DocumentService 测试 ==========

    @Test
    public void testCreateDocument() {
        // 准备测试数据
        Document newDocument = new Document("New Document", "New content", 1L);
        Document savedDocument = new Document("New Document", "New content", 1L);
        savedDocument.setId(2L);

        DocumentAssignment assignment = new DocumentAssignment(2L, 1L, DocumentAssignment.AssignmentType.EDITOR, 1L);

        // 模拟repository行为
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentAssignmentRepository.save(any(DocumentAssignment.class))).thenReturn(assignment);

        // 执行测试
        Document result = documentService.createDocument(newDocument, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Document", result.getTitle());
        assertEquals(1L, result.getCreatedBy());

        // 验证repository调用
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(documentAssignmentRepository, times(1)).save(any(DocumentAssignment.class));
    }

    @Test
    public void testGetDocument() {
        // 模拟repository行为
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDocument));

        // 执行测试
        Document result = documentService.getDocument(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testDocument.getId(), result.getId());
        assertEquals(testDocument.getTitle(), result.getTitle());

        // 验证repository调用
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetDocumentNotFound() {
        // 模拟repository行为
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.getDocument(999L);
        });

        // 验证repository调用
        verify(documentRepository, times(1)).findById(999L);
    }

    @Test
    public void testUpdateDocument() {
        // 准备测试数据
        Document updatedDocument = new Document("Updated Title", "Updated content", 1L);
        Document existingDocument = new Document("Old Title", "Old content", 1L);
        existingDocument.setId(1L);

        // 模拟repository行为
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existingDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(existingDocument);

        // 执行测试
        Document result = documentService.updateDocument(1L, updatedDocument);

        // 验证结果
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated content", result.getContent());

        // 验证repository调用
        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    public void testDeleteDocument() {
        // 模拟repository行为
        when(documentRepository.existsById(1L)).thenReturn(true);

        // 执行测试
        assertDoesNotThrow(() -> {
            documentService.deleteDocument(1L);
        });

        // 验证repository调用
        verify(documentRepository, times(1)).existsById(1L);
        verify(documentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteDocumentNotFound() {
        // 模拟repository行为
        when(documentRepository.existsById(999L)).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            documentService.deleteDocument(999L);
        });

        // 验证repository调用
        verify(documentRepository, times(1)).existsById(999L);
        verify(documentRepository, never()).deleteById(999L);
    }

    @Test
    public void testPublishDocument() {
        // 准备测试数据
        Document draftDocument = new Document("Draft Document", "Draft content", 1L);
        draftDocument.setId(1L);
        draftDocument.setStatus(Document.DocumentStatus.DRAFT);

        // 模拟repository行为
        when(documentRepository.findById(1L)).thenReturn(Optional.of(draftDocument));
        when(documentRepository.save(any(Document.class))).thenReturn(draftDocument);

        // 执行测试
        Document result = documentService.publishDocument(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(Document.DocumentStatus.PUBLISHED, result.getStatus());

        // 验证repository调用
        verify(documentRepository, times(1)).findById(1L);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    // ========== UserService 测试 ==========

    @Test
    public void testCreateUser() {
        // 准备测试数据
        Set<String> roleNames = Set.of("ROLE_TEST_" + System.currentTimeMillis());
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        User newUser = new User("newuser", encodedPassword, "newuser@example.com");
        newUser.setId(2L);

        // 模拟repository行为
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // 执行测试
        User result = userService.createUser("newuser", rawPassword, "newuser@example.com", roleNames);

        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());

        // 验证repository调用
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUserWithExistingUsername() {
        // 模拟repository行为
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.createUser("existinguser", "password", "email@example.com", null);
        });

        // 验证repository调用
        verify(userRepository, times(1)).existsByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testGetUserById() {
        // 模拟repository行为
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行测试
        User result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        // 验证repository调用
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserByIdNotFound() {
        // 模拟repository行为
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        // 验证repository调用
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    public void testGetUserByUsername() {
        // 模拟repository行为
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // 执行测试
        User result = userService.getUserByUsername(testUser.getUsername());

        // 验证结果
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());

        // 验证repository调用
        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
    }

    @Test
    public void testDeleteUser() {
        // 模拟repository行为
        when(userRepository.existsById(1L)).thenReturn(true);

        // 执行测试
        boolean result = userService.deleteUser(1L);

        // 验证结果
        assertTrue(result);

        // 验证repository调用
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUserNotFound() {
        // 模拟repository行为
        when(userRepository.existsById(999L)).thenReturn(false);

        // 执行测试
        boolean result = userService.deleteUser(999L);

        // 验证结果
        assertFalse(result);

        // 验证repository调用
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(999L);
    }

    // ========== CommentService 测试 ==========

    @Test
    public void testCreateComment() {
        // 准备测试数据
        Comment newComment = new Comment("New comment", 1L, 1L);
        newComment.setId(2L);

        // 模拟repository行为
        when(documentRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(newComment);

        // 执行测试
        Comment result = commentService.createComment(1L, "New comment", 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New comment", result.getContent());
        assertEquals(1L, result.getDocumentId());
        assertEquals(1L, result.getUserId());

        // 验证repository调用
        verify(documentRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).existsById(1L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void testCreateCommentWithInvalidDocument() {
        // 模拟repository行为
        when(documentRepository.existsById(999L)).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(999L, "Comment", 1L);
        });

        // 验证repository调用
        verify(documentRepository, times(1)).existsById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void testCreateCommentWithInvalidUser() {
        // 模拟repository行为
        when(documentRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(999L)).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(1L, "Comment", 999L);
        });

        // 验证repository调用
        verify(documentRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).existsById(999L);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    public void testGetCommentsForDocument() {
        // 准备测试数据
        List<Comment> comments = Arrays.asList(testComment);

        // 模拟repository行为
        when(commentRepository.findByDocumentId(1L)).thenReturn(comments);

        // 执行测试
        List<Comment> result = commentService.getCommentsForDocument(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getId(), result.get(0).getId());

        // 验证repository调用
        verify(commentRepository, times(1)).findByDocumentId(1L);
    }

    @Test
    public void testGetCommentsByUser() {
        // 准备测试数据
        List<Comment> comments = Arrays.asList(testComment);

        // 模拟repository行为
        when(commentRepository.findByUserId(1L)).thenReturn(comments);

        // 执行测试
        List<Comment> result = commentService.getCommentsByUser(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testComment.getId(), result.get(0).getId());

        // 验证repository调用
        verify(commentRepository, times(1)).findByUserId(1L);
    }
}