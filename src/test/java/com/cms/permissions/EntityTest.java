package com.cms.permissions;

import com.cms.permissions.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EntityTest {

    @Test
    public void testUserEntityCreation() {
        // 测试用户实体创建
        User user = new User("testuser", "password123", "test@example.com");
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    public void testRoleEntityCreation() {
        // 测试角色实体创建
        Role role = new Role("ROLE_ADMIN", "Administrator role");
        
        assertNotNull(role);
        assertEquals("ROLE_ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertNotNull(role.getPermissions());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    public void testPermissionEntityCreation() {
        // 测试权限实体创建
        Permission permission = new Permission("DOC:EDIT", "Edit document permission");
        
        assertNotNull(permission);
        assertEquals("DOC:EDIT", permission.getCode());
        assertEquals("Edit document permission", permission.getDescription());
        assertNotNull(permission.getRoles());
        assertTrue(permission.getRoles().isEmpty());
    }

    @Test
    public void testDocumentEntityCreation() {
        // 测试文档实体创建
        Document document = new Document();
        document.setTitle("Test Document");
        document.setContent("This is test content");
        document.setCreatedBy(1L);
        document.setIsPublic(false);
        
        assertNotNull(document);
        assertEquals("Test Document", document.getTitle());
        assertEquals("This is test content", document.getContent());
        assertEquals(1L, document.getCreatedBy());
        assertFalse(document.getIsPublic());
        assertEquals(Document.DocumentStatus.DRAFT, document.getStatus());
    }

    @Test
    public void testCommentEntityCreation() {
        // 测试评论实体创建
        Comment comment = new Comment("This is a test comment", 1L, 1L);
        
        assertNotNull(comment);
        assertEquals("This is a test comment", comment.getContent());
        assertEquals(1L, comment.getDocumentId());
        assertEquals(1L, comment.getUserId());
        assertNotNull(comment.getCreatedAt());
        assertNotNull(comment.getUpdatedAt());
    }

    @Test
    public void testDocumentAssignmentEntityCreation() {
        // 测试文档分配实体创建
        DocumentAssignment assignment = new DocumentAssignment(1L, 1L, DocumentAssignment.AssignmentType.EDITOR, 2L);
        
        assertNotNull(assignment);
        assertEquals(1L, assignment.getDocumentId());
        assertEquals(1L, assignment.getUserId());
        assertEquals(DocumentAssignment.AssignmentType.EDITOR, assignment.getAssignmentType());
        assertEquals(2L, assignment.getAssignedBy());
        assertNotNull(assignment.getAssignedAt());
    }

    @Test
    public void testUserRoleRelationship() {
        // 测试用户-角色关系
        User user = new User("testuser", "password123", "test@example.com");
        Role role = new Role("ROLE_EDITOR", "Editor role");
        
        user.getRoles().add(role);
        
        assertTrue(user.getRoles().contains(role));
        assertTrue(user.hasRole("ROLE_EDITOR"));
    }

    @Test
    public void testRolePermissionRelationship() {
        // 测试角色-权限关系
        Role role = new Role("ROLE_EDITOR", "Editor role");
        Permission permission = new Permission("DOC:EDIT", "Edit document permission");
        
        role.addPermission(permission);
        
        assertTrue(role.getPermissions().contains(permission));
        assertTrue(permission.getRoles().contains(role));
    }

    @Test
    public void testUserPermissionThroughRole() {
        // 测试用户通过角色获得权限
        User user = new User("testuser", "password123", "test@example.com");
        Role role = new Role("ROLE_EDITOR", "Editor role");
        Permission editPermission = new Permission("DOC:EDIT", "Edit document permission");
        Permission publishPermission = new Permission("DOC:PUBLISH", "Publish document permission");
        
        role.addPermission(editPermission);
        role.addPermission(publishPermission);
        user.getRoles().add(role);
        
        // 验证用户通过角色拥有权限
        Set<Permission> userPermissions = role.getPermissions();
        assertTrue(userPermissions.contains(editPermission));
        assertTrue(userPermissions.contains(publishPermission));
        assertEquals(2, userPermissions.size());
    }

    @Test
    public void testDocumentStatusTransition() {
        // 测试文档状态转换
        Document document = new Document();
        document.setTitle("Test Document");
        document.setContent("Content");
        document.setCreatedBy(1L);
        
        // 初始状态应该是DRAFT
        assertEquals(Document.DocumentStatus.DRAFT, document.getStatus());
        
        // 测试状态转换
        document.setStatus(Document.DocumentStatus.PUBLISHED);
        assertEquals(Document.DocumentStatus.PUBLISHED, document.getStatus());
        
        document.setStatus(Document.DocumentStatus.REJECTED);
        assertEquals(Document.DocumentStatus.REJECTED, document.getStatus());
    }

    @Test
    public void testUserStatusManagement() {
        // 测试用户状态管理
        User user = new User("testuser", "password123", "test@example.com");
        
        // 默认状态应该是ACTIVE
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        
        // 测试状态变更
        user.setStatus(User.UserStatus.INACTIVE);
        assertEquals(User.UserStatus.INACTIVE, user.getStatus());
        
        user.setStatus(User.UserStatus.SUSPENDED);
        assertEquals(User.UserStatus.SUSPENDED, user.getStatus());
    }

    @Test
    public void testDocumentAssignmentTypes() {
        // 测试文档分配类型
        DocumentAssignment editorAssignment = new DocumentAssignment();
        editorAssignment.setAssignmentType(DocumentAssignment.AssignmentType.EDITOR);
        assertEquals(DocumentAssignment.AssignmentType.EDITOR, editorAssignment.getAssignmentType());
        
        DocumentAssignment approverAssignment = new DocumentAssignment();
        approverAssignment.setAssignmentType(DocumentAssignment.AssignmentType.APPROVER);
        assertEquals(DocumentAssignment.AssignmentType.APPROVER, approverAssignment.getAssignmentType());
    }

    @Test
    public void testEntityValidation() {
        // 测试实体验证（基本的非空检查）
        User user = new User();
        
        // 测试空构造函数
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getRoles());
        
        // 设置值后验证
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEmail("test@example.com");
        
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
    }
}