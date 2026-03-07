package com.ganithyanthram.modularapp.entitlement.role.controller;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Role management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Create a new role
     * POST /api/v1/admin/roles
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createRole(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateRoleRequest request) {
        
        UUID id = roleService.createRole(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get role by ID
     * GET /api/v1/admin/roles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable UUID id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List roles with pagination and optional organisation filter
     * GET /api/v1/admin/roles
     */
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles(
            @RequestParam(required = false) UUID orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<RoleResponse> roles;
        if (orgId != null) {
            roles = roleService.getRolesByOrganisation(orgId, page, size);
        } else {
            roles = roleService.getAllRoles(page, size);
        }
        
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Update role
     * PUT /api/v1/admin/roles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        
        RoleResponse response = roleService.updateRole(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete role (soft delete)
     * DELETE /api/v1/admin/roles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activate role
     * PATCH /api/v1/admin/roles/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateRole(@PathVariable UUID id) {
        roleService.activateRole(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get effective permissions for a role (includes parent permissions)
     * GET /api/v1/admin/roles/{id}/effective-permissions
     */
    @GetMapping("/{id}/effective-permissions")
    public ResponseEntity<List<RoleNode>> getEffectivePermissions(@PathVariable UUID id) {
        List<RoleNode> permissions = roleService.getEffectivePermissions(id);
        return ResponseEntity.ok(permissions);
    }
}
