package com.cms.permissions.util;

import com.cms.permissions.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    /**
     * Get the current user ID from the security context
     * @return the user ID of the currently authenticated user
     * @throws RuntimeException if no user is authenticated
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getId();
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            // Handle standard UserDetails - this case shouldn't happen with our custom implementation
            throw new RuntimeException("Expected CustomUserDetails but got standard UserDetails");
        } else {
            throw new RuntimeException("Unexpected principal type: " + authentication.getPrincipal().getClass().getName());
        }
    }
}