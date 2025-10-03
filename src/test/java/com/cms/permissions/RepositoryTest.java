package com.cms.permissions;

import com.cms.permissions.entity.*;
import com.cms.permissions.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentAssignmentRepository documentAssignmentRepository;

    private User testUser;
    private Role testRole;
    private Permission testPermission;
    private Document testDocument;

    @BeforeEach
    public void setUp() {
        // 创建测试数据，使用唯一标识符避免冲突
        String uniqueId = String.valueOf(System.currentTimeMillis());
        testUser = new User("testuser_" + uniqueId, "password123", "test_" + uniqueId + "@example.com");
        testRole = new Role("ROLE_TEST_" + uniqueId, "Test Role " + uniqueId);
        testPermission = new Permission("TEST_PERMISSION_" + uniqueId, "Test Permission " + uniqueId);
        testDocument = new Document();
        testDocument.setTitle("Test Document " + uniqueId);
        testDocument.setContent("Test content");
        testDocument.setCreatedBy(1L);
        
        // 保存测试数据
        userRepository.save(testUser);
        roleRepository.save(testRole);
        permissionRepository.save(testPermission);
        documentRepository.save(testDocument);
    }

    @Test
    public void testUserRepository() {
        // 测试用户查找
        Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());

        // 测试用户不存在
        Optional<User> notFoundUser = userRepository.findByUsername("nonexistent");
        assertFalse(notFoundUser.isPresent());

        // 测试邮箱查找
        Optional<User> foundByEmail = userRepository.findByEmail(testUser.getEmail());
        assertTrue(foundByEmail.isPresent());
        assertEquals(testUser.getUsername(), foundByEmail.get().getUsername());
    }

    @Test
    public void testRoleRepository() {
        // 测试角色查找
        Optional<Role> foundRole = roleRepository.findByName(testRole.getName());
        assertTrue(foundRole.isPresent());
        assertEquals(testRole.getName(), foundRole.get().getName());
        assertEquals(testRole.getDescription(), foundRole.get().getDescription());
        
        // 测试角色不存在
        Optional<Role> notFound = roleRepository.findByName("ROLE_NONEXISTENT");
        assertFalse(notFound.isPresent());
    }

    @Test
    public void testPermissionRepository() {
        // 测试权限查找
        Optional<Permission> foundPermission = permissionRepository.findByCode(testPermission.getCode());
        assertTrue(foundPermission.isPresent());
        assertEquals(testPermission.getCode(), foundPermission.get().getCode());
        assertEquals(testPermission.getDescription(), foundPermission.get().getDescription());

        // 测试权限不存在
        Optional<Permission> notFoundPermission = permissionRepository.findByCode("NONEXISTENT:PERMISSION");
        assertFalse(notFoundPermission.isPresent());
    }

    @Test
    public void testDocumentRepository() {
        // 测试基本的CRUD操作
        Optional<Document> foundDocument = documentRepository.findById(testDocument.getId());
        assertTrue(foundDocument.isPresent());
        assertEquals(testDocument.getTitle(), foundDocument.get().getTitle());
        assertEquals(testDocument.getContent(), foundDocument.get().getContent());
        
        // 测试查找所有文档
        List<Document> allDocuments = documentRepository.findAll();
        assertFalse(allDocuments.isEmpty());
        assertTrue(allDocuments.stream().anyMatch(d -> d.getId().equals(testDocument.getId())));
    }

    @Test
    public void testCommentRepository() {
        // 创建评论
        Comment comment = new Comment("Test comment", testDocument.getId(), testUser.getId());
        commentRepository.save(comment);

        // 测试按文档ID查找评论
        List<Comment> documentComments = commentRepository.findByDocumentId(testDocument.getId());
        assertFalse(documentComments.isEmpty());
        assertEquals("Test comment", documentComments.get(0).getContent());

        // 测试按用户ID查找评论
        List<Comment> userComments = commentRepository.findByUserId(testUser.getId());
        assertFalse(userComments.isEmpty());
        assertEquals("Test comment", userComments.get(0).getContent());
    }

    @Test
    public void testDocumentAssignmentRepository() {
        // 创建文档分配
        DocumentAssignment assignment = new DocumentAssignment(
            testDocument.getId(), 
            testUser.getId(), 
            DocumentAssignment.AssignmentType.EDITOR, 
            testUser.getId()
        );
        documentAssignmentRepository.save(assignment);

        // 测试按用户ID查找分配
        List<DocumentAssignment> userAssignments = documentAssignmentRepository.findByUserId(testUser.getId());
        assertFalse(userAssignments.isEmpty());
        assertEquals(DocumentAssignment.AssignmentType.EDITOR, userAssignments.get(0).getAssignmentType());

        // 测试按文档ID查找分配
        List<DocumentAssignment> documentAssignments = documentAssignmentRepository.findByDocumentId(testDocument.getId());
        assertFalse(documentAssignments.isEmpty());
        assertEquals(testUser.getId(), documentAssignments.get(0).getUserId());

        // 测试特定分配查找
        Optional<DocumentAssignment> specificAssignment = documentAssignmentRepository
            .findByDocumentIdAndUserIdAndAssignmentType(
                testDocument.getId(), 
                testUser.getId(), 
                DocumentAssignment.AssignmentType.EDITOR
            );
        assertTrue(specificAssignment.isPresent());

        // 测试分配存在性检查
        boolean exists = documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
            testDocument.getId(), 
            testUser.getId(), 
            DocumentAssignment.AssignmentType.EDITOR
        );
        assertTrue(exists);
    }

    @Test
    public void testUserRoleRelationship() {
        // 建立用户-角色关系
        testUser.getRoles().add(testRole);
        userRepository.save(testUser);

        // 验证关系
        User savedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(savedUser);
        assertFalse(savedUser.getRoles().isEmpty());
        assertTrue(savedUser.getRoles().contains(testRole));
    }

    @Test
    public void testRolePermissionRelationship() {
        // 建立角色-权限关系
        testRole.addPermission(testPermission);
        roleRepository.save(testRole);

        // 验证关系
        Role savedRole = roleRepository.findById(testRole.getId()).orElse(null);
        assertNotNull(savedRole);
        assertFalse(savedRole.getPermissions().isEmpty());
        assertTrue(savedRole.getPermissions().contains(testPermission));
    }

    @Test
    public void testDocumentRepositoryCustomQueries() {
        // 创建用户分配
        DocumentAssignment assignment = new DocumentAssignment(
            testDocument.getId(), 
            testUser.getId(), 
            DocumentAssignment.AssignmentType.EDITOR, 
            testUser.getId()
        );
        documentAssignmentRepository.save(assignment);

        // 测试查找用户文档
        List<Document> userDocuments = documentRepository.findDocumentsForUser(testUser.getId());
        assertFalse(userDocuments.isEmpty());
        assertEquals(testDocument.getTitle(), userDocuments.get(0).getTitle());
    }
}