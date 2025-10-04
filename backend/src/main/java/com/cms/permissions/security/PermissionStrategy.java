package com.cms.permissions.security;

import org.springframework.security.core.Authentication;

public interface PermissionStrategy {
    boolean checkPermission(Authentication authentication, Long resourceId, String permissionCode);
}