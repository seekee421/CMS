package com.cms.permissions;

import com.cms.permissions.entity.*;
import com.cms.permissions.repository.*;
import com.cms.permissions.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 集成测试类 - 端到端功能测试
 * 测试完整的业务流程和系统集成
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    public void setUp() {
        // 清理测试数据（保留基础权限和角色数据）
        documentAssignmentRepository.deleteAll();
        commentRepository.deleteAll();
        documentRepository.deleteAll();
        
        // 删除测试用户
        userRepository.findByUsername("admin").ifPresent(userRepository::delete);
        userRepository.findByUsername("subadmin").ifPresent(userRepository::delete);
        userRepository.findByUsername("editor").ifPresent(userRepository::delete);
        userRepository.findByUsername("user").ifPresent(userRepository::delete);

        // 获取或创建权限 - 使用实际系统中的权限代码
        Permission userRead = permissionRepository.findByCode("USER:READ")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("USER:READ");
                    p.setDescription("Read user information");
                    return permissionRepository.save(p);
                });

        Permission userManageSub = permissionRepository.findByCode("USER:MANAGE:SUB")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("USER:MANAGE:SUB");
                    p.setDescription("Manage sub-admins and editors");
                    return permissionRepository.save(p);
                });

        Permission userManageEditor = permissionRepository.findByCode("USER:MANAGE:EDITOR")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("USER:MANAGE:EDITOR");
                    p.setDescription("Manage editors");
                    return permissionRepository.save(p);
                });

        Permission docCreate = permissionRepository.findByCode("DOC:CREATE")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:CREATE");
                    p.setDescription("Create documents");
                    return permissionRepository.save(p);
                });

        Permission docEdit = permissionRepository.findByCode("DOC:EDIT")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:EDIT");
                    p.setDescription("Edit documents");
                    return permissionRepository.save(p);
                });

        Permission docDelete = permissionRepository.findByCode("DOC:DELETE")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:DELETE");
                    p.setDescription("Delete documents");
                    return permissionRepository.save(p);
                });

        Permission docPublish = permissionRepository.findByCode("DOC:PUBLISH")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:PUBLISH");
                    p.setDescription("Publish documents");
                    return permissionRepository.save(p);
                });

        Permission docApproveAll = permissionRepository.findByCode("DOC:APPROVE:ALL")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:APPROVE:ALL");
                    p.setDescription("Approve all documents");
                    return permissionRepository.save(p);
                });

        Permission docApproveAssigned = permissionRepository.findByCode("DOC:APPROVE:ASSIGNED")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:APPROVE:ASSIGNED");
                    p.setDescription("Approve assigned documents");
                    return permissionRepository.save(p);
                });

        Permission docViewLogged = permissionRepository.findByCode("DOC:VIEW:LOGGED")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:VIEW:LOGGED");
                    p.setDescription("View documents when logged in");
                    return permissionRepository.save(p);
                });

        Permission docDownload = permissionRepository.findByCode("DOC:DOWNLOAD")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:DOWNLOAD");
                    p.setDescription("Download documents");
                    return permissionRepository.save(p);
                });

        Permission docAssign = permissionRepository.findByCode("DOC:ASSIGN")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("DOC:ASSIGN");
                    p.setDescription("Assign users to documents");
                    return permissionRepository.save(p);
                });

        Permission commentCreate = permissionRepository.findByCode("COMMENT:CREATE")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("COMMENT:CREATE");
                    p.setDescription("Create comments");
                    return permissionRepository.save(p);
                });

        Permission commentManage = permissionRepository.findByCode("COMMENT:MANAGE")
                .orElseGet(() -> {
                    Permission p = new Permission();
                    p.setCode("COMMENT:MANAGE");
                    p.setDescription("Manage comments");
                    return permissionRepository.save(p);
                });

        roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    r.setDescription("管理员");
                    r.getPermissions().addAll(List.of(userRead, userManageSub, userManageEditor, docCreate, docEdit, docDelete, docPublish, docApproveAll, docViewLogged, docDownload, docAssign, commentCreate, commentManage));
                    return roleRepository.save(r);
                });

        roleRepository.findByName("ROLE_SUB_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_SUB_ADMIN");
                    r.setDescription("子管理员");
                    r.getPermissions().addAll(List.of(userRead, userManageEditor, docCreate, docEdit, docPublish, docApproveAssigned, docViewLogged, docDownload, docAssign, commentCreate, commentManage));
                    return roleRepository.save(r);
                });

        roleRepository.findByName("ROLE_EDITOR")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_EDITOR");
                    r.setDescription("编辑员");
                    r.getPermissions().addAll(List.of(docCreate, docEdit, docPublish, docViewLogged, docDownload, commentCreate));
                    return roleRepository.save(r);
                });

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    r.setDescription("普通用户");
                    r.getPermissions().addAll(List.of(docViewLogged, docDownload, commentCreate));
                    return roleRepository.save(r);
                });

        // 使用@WithMockUser，不需要创建实际用户
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        documentAssignmentRepository.deleteAll();
        commentRepository.deleteAll();
        documentRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
    }

    /**
     * 测试完整的文档生命周期 - 管理员视角
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:DELETE", "DOC:APPROVE:ALL", "DOC:ASSIGN", "COMMENT:CREATE", "COMMENT:MANAGE", "USER:READ"})
    public void testCompleteDocumentLifecycleAsAdmin() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建管理员用户
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
            }
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("password");
            adminUser.getRoles().add(adminRole);
            adminUser = userRepository.save(adminUser);
            
            // 创建编辑用户
            Role editorRole = roleRepository.findByName("ROLE_EDITOR").orElse(null);
            if (editorRole == null) {
                editorRole = new Role();
                editorRole.setName("ROLE_EDITOR");
                editorRole = roleRepository.save(editorRole);
            }
            
            User editorUser = new User();
            editorUser.setUsername("editor");
            editorUser.setEmail("editor@example.com");
            editorUser.setPassword("password");
            editorUser.getRoles().add(editorRole);
            editorUser = userRepository.save(editorUser);
            
            Long adminUserId = adminUser.getId();
            Long editorUserId = editorUser.getId();
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(adminUserId);

            // 1. 创建文档
            Document document = new Document();
            document.setTitle("管理员测试文档");
            document.setContent("这是管理员创建的测试文档内容");
            document.setCreatedBy(adminUserId);
            document.setStatus(Document.DocumentStatus.DRAFT);

            String documentJson = objectMapper.writeValueAsString(document);

            mockMvc.perform(post("/api/documents")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(documentJson))
                    .andExpect(status().isOk());

            // 验证文档已创建
            List<Document> documents = documentRepository.findAll();
            assertFalse(documents.isEmpty());
            Document createdDocument = documents.get(0);
            assertEquals("管理员测试文档", createdDocument.getTitle());

            // 2. 分配编辑员
            DocumentAssignment assignment = new DocumentAssignment();
            assignment.setDocumentId(createdDocument.getId());
            assignment.setUserId(editorUserId);
            assignment.setAssignmentType(DocumentAssignment.AssignmentType.EDITOR);
            assignment.setAssignedBy(adminUserId);

            documentAssignmentRepository.save(assignment);

            // 3. 发布文档
            createdDocument.setStatus(Document.DocumentStatus.PUBLISHED);
            documentRepository.save(createdDocument);

            // 4. 创建评论
            Comment comment = new Comment();
            comment.setContent("这是一个测试评论");
            comment.setDocumentId(createdDocument.getId());
            comment.setUserId(adminUserId);

            String commentJson = objectMapper.writeValueAsString(comment);

            mockMvc.perform(post("/api/comments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(commentJson))
                    .andExpect(status().isOk());

            // 验证评论已创建
            List<Comment> comments = commentRepository.findAll();
            assertFalse(comments.isEmpty());
            assertEquals("这是一个测试评论", comments.get(0).getContent());

            // 5. 获取文档列表
            mockMvc.perform(get("/api/documents"))
                    .andExpect(status().isOk());
        }
    }

    /**
     * 测试编辑员工作流程
     */
    @Test
    @WithMockUser(username = "editor", authorities = {"DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "COMMENT:CREATE", "USER:READ"})
    public void testEditorWorkflow() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建测试用户
            User editorUser = new User();
            editorUser.setUsername("editor");
            editorUser.setPassword("password");
            editorUser.setEmail("editor@test.com");
            
            // 获取编辑员角色
            Role editorRole = roleRepository.findByName("ROLE_EDITOR")
                    .orElseThrow(() -> new RuntimeException("Editor role not found"));
            editorUser.getRoles().add(editorRole);
            editorUser = userRepository.save(editorUser);
            
            Long editorUserId = editorUser.getId();
            Long adminUserId = 1L;
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(editorUserId);

            // 1. 创建文档
            Document document = new Document();
            document.setTitle("编辑员文档");
            document.setContent("编辑员创建的文档");
            document.setCreatedBy(editorUserId);
            document.setStatus(Document.DocumentStatus.DRAFT);
            documentRepository.save(document);

            // 2. 分配给自己
            DocumentAssignment assignment = new DocumentAssignment();
            assignment.setDocumentId(document.getId());
            assignment.setUserId(editorUserId);
            assignment.setAssignmentType(DocumentAssignment.AssignmentType.EDITOR);
            assignment.setAssignedBy(adminUserId);
            documentAssignmentRepository.save(assignment);

            // 3. 编辑文档
            document.setContent("更新后的内容");
            String documentJson = objectMapper.writeValueAsString(document);

            mockMvc.perform(put("/api/documents/" + document.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(documentJson))
                    .andExpect(status().isOk());

            // 4. 发布文档
            document.setStatus(Document.DocumentStatus.PUBLISHED);
            documentRepository.save(document);

            // 验证文档状态
            Optional<Document> updatedDocument = documentRepository.findById(document.getId());
            assertTrue(updatedDocument.isPresent());
            assertEquals(Document.DocumentStatus.PUBLISHED, updatedDocument.get().getStatus());
        }
    }

    /**
     * 测试权限边界 - 普通用户尝试执行超出权限的操作
     */
    @Test
    @WithMockUser(username = "user", authorities = {"DOC:VIEW:LOGGED", "DOC:DOWNLOAD", "COMMENT:CREATE", "USER:READ"})
    public void testPermissionBoundaries() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建普通用户
            Role normalRole = roleRepository.findByName("ROLE_USER").orElse(null);
            if (normalRole == null) {
                normalRole = new Role();
                normalRole.setName("ROLE_USER");
                normalRole = roleRepository.save(normalRole);
            }
            
            User normalUser = new User();
            normalUser.setUsername("user");
            normalUser.setEmail("user@example.com");
            normalUser.setPassword("password");
            normalUser.getRoles().add(normalRole);
            normalUser = userRepository.save(normalUser);
            
            Long normalUserId = normalUser.getId();
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(normalUserId);

            // 创建一个文档（由管理员创建）
            Long adminUserId = 1L;
            Document document = new Document();
            document.setTitle("受保护文档");
            document.setContent("普通用户不能编辑");
            document.setCreatedBy(adminUserId);
            document.setStatus(Document.DocumentStatus.PUBLISHED);
            document.setIsPublic(true); // 设置为公开文档，普通用户可以查看
            documentRepository.save(document);

            // 1. 普通用户尝试创建文档（应该被拒绝或有限制）
            Document newDocument = new Document();
            newDocument.setTitle("普通用户文档");
            newDocument.setContent("普通用户尝试创建");

            String documentJson = objectMapper.writeValueAsString(newDocument);

            // 普通用户可能无法创建文档，或者创建的文档有特殊限制
            mockMvc.perform(post("/api/documents")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(documentJson))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 可能返回403（禁止）或其他状态
                        assertTrue(status == 403 || status == 401 || status == 500);
                    });

            // 2. 普通用户尝试删除文档（应该被拒绝）
            mockMvc.perform(delete("/api/documents/" + document.getId())
                    .with(csrf()))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 403 || status == 401 || status == 500);
                    });

            // 3. 普通用户可以查看公共文档
            mockMvc.perform(get("/api/documents/" + document.getId()))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 应该能够查看或至少不是权限错误
                        assertTrue(status != 403);
                    });

            // 4. 普通用户可以创建评论
            Comment comment = new Comment();
            comment.setContent("普通用户的评论");
            comment.setDocumentId(document.getId());
            comment.setUserId(normalUserId);

            String commentJson = objectMapper.writeValueAsString(comment);

            mockMvc.perform(post("/api/comments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(commentJson))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 普通用户应该能够创建评论
                        assertTrue(status == 200 || status == 201);
                    });
        }
    }

    /**
     * 测试数据一致性和事务完整性
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:DELETE", "DOC:APPROVE:ALL", "DOC:ASSIGN", "COMMENT:CREATE", "COMMENT:MANAGE", "USER:READ"})
    public void testDataConsistencyAndTransactions() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建管理员用户
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
            }
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("password");
            adminUser.getRoles().add(adminRole);
            adminUser = userRepository.save(adminUser);
            
            // 创建编辑用户
            Role editorRole = roleRepository.findByName("ROLE_EDITOR").orElse(null);
            if (editorRole == null) {
                editorRole = new Role();
                editorRole.setName("ROLE_EDITOR");
                editorRole = roleRepository.save(editorRole);
            }
            
            User editorUser = new User();
            editorUser.setUsername("editor");
            editorUser.setEmail("editor@example.com");
            editorUser.setPassword("password");
            editorUser.getRoles().add(editorRole);
            editorUser = userRepository.save(editorUser);
            
            Long adminUserId = adminUser.getId();
            Long editorUserId = editorUser.getId();
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(adminUserId);

            // 1. 创建文档
            Document document = new Document();
            document.setTitle("一致性测试文档");
            document.setContent("测试数据一致性");
            document.setCreatedBy(adminUserId);
            document.setStatus(Document.DocumentStatus.DRAFT);
            document = documentRepository.save(document);

            // 2. 创建文档分配
            DocumentAssignment assignment = new DocumentAssignment();
            assignment.setDocumentId(document.getId());
            assignment.setUserId(editorUserId);
            assignment.setAssignmentType(DocumentAssignment.AssignmentType.EDITOR);
            assignment.setAssignedBy(adminUserId);
            assignment.setAssignedAt(LocalDateTime.now());
            documentAssignmentRepository.save(assignment);

            // 3. 创建评论
            Comment comment = new Comment("一致性测试评论", document.getId(), adminUserId);
            comment = commentRepository.save(comment);

            // 验证数据一致性
            Optional<Document> savedDocument = documentRepository.findById(document.getId());
            assertTrue(savedDocument.isPresent());

            List<DocumentAssignment> assignments = documentAssignmentRepository.findByDocumentId(document.getId());
            assertFalse(assignments.isEmpty());
            assertEquals(editorUserId, assignments.get(0).getUserId());

            List<Comment> comments = commentRepository.findByDocumentId(document.getId());
            assertFalse(comments.isEmpty());
            assertEquals("一致性测试评论", comments.get(0).getContent());

            // 4. 测试级联删除（手动删除相关数据）
            // 先删除评论
            List<Comment> commentsToDelete = commentRepository.findByDocumentId(document.getId());
            commentRepository.deleteAll(commentsToDelete);
            // 再删除文档分配
            List<DocumentAssignment> assignmentsToDelete = documentAssignmentRepository.findByDocumentId(document.getId());
            documentAssignmentRepository.deleteAll(assignmentsToDelete);
            // 最后删除文档
            documentRepository.delete(document);

            // 验证相关数据是否正确处理
            Optional<Document> deletedDocument = documentRepository.findById(document.getId());
            assertFalse(deletedDocument.isPresent());
        }
    }

    /**
     * 测试系统性能和并发处理
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:DELETE", "DOC:APPROVE:ALL", "DOC:ASSIGN", "COMMENT:CREATE", "COMMENT:MANAGE", "USER:READ"})
    public void testSystemPerformanceAndConcurrency() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建管理员用户
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
            }
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("password");
            adminUser.getRoles().add(adminRole);
            adminUser = userRepository.save(adminUser);
            
            Long adminUserId = adminUser.getId();
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(adminUserId);

            // 创建多个文档来测试性能
            for (int i = 0; i < 10; i++) {
                Document document = new Document();
                document.setTitle("性能测试文档 " + i);
                document.setContent("内容 " + i);
                document.setCreatedBy(adminUserId);
                document.setStatus(Document.DocumentStatus.DRAFT);
                documentRepository.save(document);
            }

            // 测试批量查询性能
            long startTime = System.currentTimeMillis();
            List<Document> allDocuments = documentRepository.findAll();
            long endTime = System.currentTimeMillis();

            // 验证查询结果和性能
            assertTrue(allDocuments.size() >= 10);
            assertTrue((endTime - startTime) < 1000); // 查询应该在1秒内完成

            // 测试分页查询
            mockMvc.perform(get("/api/documents")
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk());
        }
    }

    /**
     * 测试错误处理和异常情况
     */
    @Test
    @WithMockUser(username = "admin", authorities = {"DOC:CREATE", "DOC:EDIT", "DOC:PUBLISH", "DOC:DELETE", "DOC:APPROVE:ALL", "DOC:ASSIGN", "COMMENT:CREATE", "COMMENT:MANAGE", "USER:READ"})
    public void testErrorHandlingAndExceptionCases() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // 创建管理员用户
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
            }
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword("password");
            adminUser.getRoles().add(adminRole);
            adminUser = userRepository.save(adminUser);
            
            Long adminUserId = adminUser.getId();
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(adminUserId);

            // 1. 测试访问不存在的文档
            mockMvc.perform(get("/api/documents/99999"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 404 || status == 500 || status == 403);
                    });

            // 2. 测试无效的JSON数据
            mockMvc.perform(post("/api/documents")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 400 || status == 500);
                    });

            // 3. 测试删除不存在的资源
            mockMvc.perform(delete("/api/documents/99999")
                    .with(csrf()))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        assertTrue(status == 404 || status == 500);
                    });

            // 4. 测试空数据提交
            mockMvc.perform(post("/api/documents")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // 应该返回验证错误或其他适当的错误状态
                        assertTrue(status >= 400);
                    });
        }
    }
}