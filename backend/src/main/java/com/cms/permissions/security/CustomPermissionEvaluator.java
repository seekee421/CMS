package com.cms.permissions.security;

import com.cms.permissions.entity.Document;
import com.cms.permissions.entity.DocumentAssignment;
import com.cms.permissions.repository.UserRepository;
import com.cms.permissions.service.PermissionCacheService;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Object targetDomainObject,
        Object permission
    ) {
        if (
            authentication == null ||
            targetDomainObject == null ||
            permission == null
        ) {
            return false;
        }

        String permissionCode = permission.toString();
        String username = authentication.getName();

        // 使用缓存服务检查用户权限
        if (!permissionCacheService.hasPermission(username, permissionCode)) {
            return false;
        }

        // Additional resource-level checks
        if (targetDomainObject instanceof Document) {}

        return true;
    }

    @Override
    public boolean hasPermission(
        Authentication authentication,
        Serializable targetId,
        String targetType,
        Object permission
    ) {
        if (
            authentication == null ||
            targetId == null ||
            targetType == null ||
            permission == null
        ) {
            return false;
        }

        String permissionCode = permission.toString();
        String username = authentication.getName();

        // 使用缓存服务检查用户权限
        if (!permissionCacheService.hasPermission(username, permissionCode)) {
            return false;
        }

        // 获取用户ID用于资源级权限检查
        var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }
        Long userId = user.get().getId();

        // Additional resource-level checks based on target type
        if ("document".equalsIgnoreCase(targetType)) {
            Long documentId = (Long) targetId;
            return checkDocumentPermission(userId, documentId, permissionCode);
        } else if ("comment".equalsIgnoreCase(targetType)) {
            // For comments, we need to check if the user has management rights for the document that contains the comment
            // Long commentId = (Long) targetId; // 暂时未使用，后续实现评论权限检查时再启用
            // This would require a comment repository to get the document ID from the comment
            // For now, we'll assume it's handled in a different way in the service layer
            return true; // Simplified for now
        }

        return true;
    }

    private boolean checkDocumentPermission(
        Long userId,
        Long documentId,
        String permissionCode
    ) {
        // 使用缓存服务检查文档权限
        switch (permissionCode) {
            case "DOC:EDIT":
            case "DOC:PUBLISH":
            case "DOC:MANAGE_COMMENTS":
                // 检查用户是否被分配为该文档的编辑者
                return permissionCacheService.hasDocumentAssignment(
                    userId,
                    documentId,
                    DocumentAssignment.AssignmentType.EDITOR
                );
            case "DOC:APPROVE:ASSIGNED":
                // 检查用户是否被分配为该文档的审批者
                return permissionCacheService.hasDocumentAssignment(
                    userId,
                    documentId,
                    DocumentAssignment.AssignmentType.APPROVER
                );
            case "DOC:VIEW":
            case "DOC:VIEW:LOGGED":
            case "DOC:DOWNLOAD":
                // 检查文档是否公开或用户是否被分配到该文档
                if (permissionCacheService.isDocumentPublic(documentId)) {
                    return true;
                }
                // 检查用户是否被分配到该文档（任何角色）
                return permissionCacheService.isUserAssignedToDocument(
                    userId,
                    documentId
                );
            default:
                return false;
        }
    }
}
