package com.cms.permissions.security;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.DocumentAssignment;
import com.cms.permissions.repository.DocumentAssignmentRepository;
import com.cms.permissions.repository.DocumentRepository;
import com.cms.permissions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentAssignmentRepository documentAssignmentRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        String permissionCode = permission.toString();
        String username = authentication.getName();

        // Get user details
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }

        // Check if user has the specific permission
        boolean hasPermission = user.get().getRoles().stream()
                .anyMatch(role -> role.getPermissions().stream()
                        .anyMatch(perm -> perm.getCode().equals(permissionCode)));

        if (!hasPermission) {
            return false;
        }

        // Additional resource-level checks
        if (targetDomainObject instanceof Document) {
        }

        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || permission == null) {
            return false;
        }

        String permissionCode = permission.toString();
        String username = authentication.getName();

        // Get user details
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }

        // Check if user has the specific permission (function-level check)
        boolean hasPermission = user.get().getRoles().stream()
                .anyMatch(role -> role.getPermissions().stream()
                        .anyMatch(perm -> perm.getCode().equals(permissionCode)));

        if (!hasPermission) {
            return false;
        }

        // Additional resource-level checks based on target type
        if ("document".equalsIgnoreCase(targetType)) {
            Long documentId = (Long) targetId;
            return checkDocumentPermission(user.get().getId(), documentId, permissionCode);
        } else if ("comment".equalsIgnoreCase(targetType)) {
            // For comments, we need to check if the user has management rights for the document that contains the comment
            // Long commentId = (Long) targetId; // 暂时未使用，后续实现评论权限检查时再启用
            // This would require a comment repository to get the document ID from the comment
            // For now, we'll assume it's handled in a different way in the service layer
            return true; // Simplified for now
        }

        return true;
    }

    private boolean checkDocumentPermission(Long userId, Long documentId, String permissionCode) {
        // Check if user has specific assignment for this document
        switch (permissionCode) {
            case "DOC:EDIT":
            case "DOC:PUBLISH":
            case "DOC:MANAGE_COMMENTS":
                // Check if user is assigned as an EDITOR for this document
                return documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
                        documentId, userId, DocumentAssignment.AssignmentType.EDITOR);
            case "DOC:APPROVE:ASSIGNED":
                // Check if user is assigned as an APPROVER for this document
                return documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
                        documentId, userId, DocumentAssignment.AssignmentType.APPROVER);
            case "DOC:VIEW":
            case "DOC:VIEW:LOGGED":
            case "DOC:DOWNLOAD":
                // Check if document is public or user is assigned to it
                var document = documentRepository.findById(documentId);
                if (document.isPresent() && document.get().getIsPublic()) {
                    return true;
                }
                // Check if user is assigned to this document in any role
                List<DocumentAssignment> assignments = documentAssignmentRepository.findByDocumentId(documentId);
                return assignments.stream().anyMatch(assignment -> assignment.getUserId().equals(userId));
            default:
                return false;
        }
    }
}