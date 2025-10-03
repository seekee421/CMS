package com.cms.permissions;

import com.cms.permissions.entity.*;
import com.cms.permissions.repository.*;
import com.cms.permissions.security.CustomPermissionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class PermissionTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentAssignmentRepository documentAssignmentRepository;

    @InjectMocks
    private CustomPermissionEvaluator permissionEvaluator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // 使用反射注入mock的依赖
        ReflectionTestUtils.setField(permissionEvaluator, "userRepository", userRepository);
        ReflectionTestUtils.setField(permissionEvaluator, "documentRepository", documentRepository);
        ReflectionTestUtils.setField(permissionEvaluator, "documentAssignmentRepository", documentAssignmentRepository);
    }

    @Test
    public void testDocumentPermissionCheck() {
        // Create a mock user with permissions
        User user = new User("testuser", "password", "test@example.com");
        user.setId(1L); // 设置用户ID
        Role editorRole = new Role("ROLE_EDITOR", "Editor role");

        Permission editPermission = new Permission("DOC:EDIT", "Edit document permission");
        editorRole.addPermission(editPermission);
        user.getRoles().add(editorRole);

        // Mock repository call
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
                1L, user.getId(), DocumentAssignment.AssignmentType.EDITOR)).thenReturn(true);

        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("DOC:EDIT")));

        // Test the permission evaluation
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, 1L, "document", "DOC:EDIT");

        // The permission should be granted since:
        // 1. User has the DOC:EDIT authority
        // 2. User is assigned as an EDITOR for document 1
        assertTrue(hasPermission);
    }

    @Test
    public void testDocumentPermissionDenied() {
        // Create a mock user without the required permissions
        User user = new User("testuser", "password", "test@example.com");
        user.setId(2L); // 设置用户ID
        Role userRole = new Role("ROLE_USER", "User role");

        Permission viewPermission = new Permission("DOC:VIEW", "View document permission");
        userRole.addPermission(viewPermission);
        user.getRoles().add(userRole);

        // Mock repository call
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Create authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("DOC:VIEW")));

        // Test the permission evaluation - should be denied because user doesn't have DOC:EDIT permission
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, 1L, "document", "DOC:EDIT");

        assertFalse(hasPermission);
    }
}