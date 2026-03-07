package com.ganithyanthram.modularapp.security.service;

import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for evaluating permissions at runtime.
 * Used by @PreAuthorize annotations for fine-grained access control.
 */
@Service("permissionEvaluator")
@RequiredArgsConstructor
@Slf4j
public class PermissionEvaluationService {
    
    private final PermissionOverrideService permissionOverrideService;
    
    /**
     * Check if user has specific permission for a resource
     */
    public boolean hasPermission(UUID userId, UUID orgId, String resource, String action) {
        if (userId == null || orgId == null) {
            return false;
        }
        
        try {
            List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(userId, orgId);
            return checkPermission(permissions, resource, action);
        } catch (Exception e) {
            log.error("Error checking permission for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if user has admin role (can manage all resources)
     */
    public boolean isAdmin(UUID userId, UUID orgId) {
        if (userId == null || orgId == null) {
            return false;
        }
        
        try {
            List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(userId, orgId);
            return hasAdminPermission(permissions);
        } catch (Exception e) {
            log.error("Error checking admin permission for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Recursively check permission in permission tree
     */
    private boolean checkPermission(List<RoleNode> permissions, String resource, String action) {
        if (permissions == null) {
            return false;
        }
        
        for (RoleNode node : permissions) {
            if (node.getName().equals(resource)) {
                return checkActionPermission(node, action);
            }
            
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                if (checkPermission(node.getChildren(), resource, action)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if permission tree contains admin permission
     */
    private boolean hasAdminPermission(List<RoleNode> permissions) {
        if (permissions == null) {
            return false;
        }
        
        for (RoleNode node : permissions) {
            if ("admin".equals(node.getName()) && node.getPermissions() != null && node.getPermissions() == 15) {
                return true;
            }
            
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                if (hasAdminPermission(node.getChildren())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if action is allowed based on permission bits
     */
    private boolean checkActionPermission(RoleNode node, String action) {
        if (node.getPermissions() == null) {
            return false;
        }
        
        int permissions = node.getPermissions();
        
        return switch (action.toLowerCase()) {
            case "read", "view" -> (permissions & 1) != 0;
            case "create", "add" -> (permissions & 2) != 0;
            case "update", "edit" -> (permissions & 4) != 0;
            case "delete", "remove" -> (permissions & 8) != 0;
            default -> false;
        };
    }
}
