package com.ganithyanthram.modularapp.entitlement.user.controller;

import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.UserEntitlementResponse;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for User Entitlement APIs (User-facing)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/user/entitlements")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Entitlements", description = "User endpoints for viewing their own entitlements and permissions")
@SecurityRequirement(name = "bearerAuth")
public class UserEntitlementController {
    
    private final PermissionOverrideService permissionOverrideService;
    private final ResourceService resourceService;
    
    /**
     * Get current user's complete entitlements
     * GET /api/v1/user/entitlements
     */
    @GetMapping
    public ResponseEntity<UserEntitlementResponse> getMyEntitlements(@CurrentUser UUID currentUserId) {
        UserEntitlementResponse response = permissionOverrideService.getUserEntitlements(currentUserId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user's permissions for a specific organisation
     * GET /api/v1/user/entitlements/permissions
     */
    @GetMapping("/permissions")
    public ResponseEntity<List<RoleNode>> getMyPermissions(
            @CurrentUser UUID currentUserId,
            @RequestParam UUID orgId) {
        
        List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(currentUserId, orgId);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * Get resource hierarchy (for navigation/menu building)
     * GET /api/v1/user/entitlements/resources
     */
    @GetMapping("/resources")
    public ResponseEntity<List<ResourceResponse>> getResourceHierarchy() {
        List<ResourceResponse> hierarchy = resourceService.getResourceHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
    
    /**
     * Check if user has specific permission
     * GET /api/v1/user/entitlements/check
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkPermission(
            @CurrentUser UUID currentUserId,
            @RequestParam UUID orgId,
            @RequestParam String resource,
            @RequestParam String action) {
        
        List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(currentUserId, orgId);
        boolean hasPermission = hasPermission(permissions, resource, action);
        
        return ResponseEntity.ok(hasPermission);
    }
    
    /**
     * Check if user has permission for a resource and action
     */
    private boolean hasPermission(List<RoleNode> permissions, String resource, String action) {
        if (permissions == null) {
            return false;
        }
        
        for (RoleNode node : permissions) {
            if (node.getName().equals(resource)) {
                return checkActionPermission(node, action);
            }
            
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                if (hasPermission(node.getChildren(), resource, action)) {
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
