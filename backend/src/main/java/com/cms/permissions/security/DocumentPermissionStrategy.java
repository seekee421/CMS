package com.cms.permissions.security;

import com.cms.permissions.entity.DocumentAssignment;
import com.cms.permissions.repository.DocumentAssignmentRepository;
import com.cms.permissions.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class DocumentPermissionStrategy implements PermissionStrategy {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentAssignmentRepository documentAssignmentRepository;

    @Override
    public boolean checkPermission(Authentication authentication, Long resourceId, String permissionCode) {
        if (authentication == null || resourceId == null || permissionCode == null) {
            return false;
        }

        String username = authentication.getName();
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }

        Long userId = user.get().getId();

        // Check if user has specific assignment for this document
        switch (permissionCode) {
            case "DOC:EDIT":
            case "DOC:PUBLISH":
            case "DOC:MANAGE_COMMENTS":
                // Check if user is assigned as an EDITOR for this document
                return documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
                        resourceId, userId, DocumentAssignment.AssignmentType.EDITOR);
            case "DOC:APPROVE:ASSIGNED":
                // Check if user is assigned as an APPROVER for this document
                return documentAssignmentRepository.existsByDocumentIdAndUserIdAndAssignmentType(
                        resourceId, userId, DocumentAssignment.AssignmentType.APPROVER);
            case "DOC:VIEW":
            case "DOC:DOWNLOAD":
                // For viewing and downloading, we check if the document is public
                // or if the user is assigned to it - this would require DocumentRepository
                // For this strategy, we assume that the checkDocumentAccess method in the service layer handles this
                return true; // Defer to service layer for public document access check
            default:
                return false;
        }
    }
}