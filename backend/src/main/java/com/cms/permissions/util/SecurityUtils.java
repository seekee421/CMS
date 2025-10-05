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

        System.out.println("DEBUG: Authentication object: " + authentication);
        if (authentication != null) {
            System.out.println("DEBUG: Authentication class: " + authentication.getClass().getName());
            System.out.println("DEBUG: Principal: " + authentication.getPrincipal());
            System.out.println("DEBUG: Principal class: " + authentication.getPrincipal().getClass().getName());
            System.out.println("DEBUG: Is authenticated: " + authentication.isAuthenticated());
        }

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("User not authenticated");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            System.out.println("DEBUG: CustomUserDetails ID: " + customUserDetails.getId());
            return customUserDetails.getId();
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            // Handle standard UserDetails - this case shouldn't happen with our custom implementation
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            System.out.println("DEBUG: Standard UserDetails username: " + userDetails.getUsername());
            throw new RuntimeException("Expected CustomUserDetails but got standard UserDetails");
        } else {
            System.out.println("DEBUG: Unexpected principal type: " + authentication.getPrincipal().getClass().getName());
            System.out.println("DEBUG: Principal toString: " + authentication.getPrincipal().toString());
            throw new RuntimeException("Unexpected principal type: " + authentication.getPrincipal().getClass().getName());
        }
    }
}