package com.ganithyanthram.modularapp.entitlement.role.controller;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Role Management", description = "Admin endpoints for managing roles and permissions")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * Create a new role
     * POST /api/v1/admin/roles
     */
    @Operation(
        summary = "Create Role",
        description = "Create a new role with permissions. Roles can inherit permissions from parent roles."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Role created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createRole(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody CreateRoleRequest request) {
        
        UUID id = roleService.createRole(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get role by ID
     * GET /api/v1/admin/roles/{id}
     */
    @Operation(
        summary = "Get Role by ID",
        description = "Retrieve detailed information about a specific role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role found",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(
            @Parameter(description = "Role ID") @PathVariable UUID id) {
        RoleResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List roles with pagination and optional organisation filter
     * GET /api/v1/admin/roles
     */
    @Operation(
        summary = "List Roles",
        description = "Retrieve a paginated list of roles. Can be filtered by organization ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getRoles(
            @Parameter(description = "Filter by organization ID (optional)") 
            @RequestParam(required = false) UUID orgId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
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
    @Operation(
        summary = "Update Role",
        description = "Update an existing role's details and permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Parameter(description = "Role ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        
        RoleResponse response = roleService.updateRole(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete role (soft delete)
     * DELETE /api/v1/admin/roles/{id}
     */
    @Operation(
        summary = "Delete Role",
        description = "Soft delete a role. The role will be marked as inactive but not removed from the database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "Role ID") @PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activate role
     * PATCH /api/v1/admin/roles/{id}/activate
     */
    @Operation(
        summary = "Activate Role",
        description = "Activate a role, allowing it to be assigned to users"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role activated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateRole(
            @Parameter(description = "Role ID") @PathVariable UUID id) {
        roleService.activateRole(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get effective permissions for a role (includes parent permissions)
     * GET /api/v1/admin/roles/{id}/effective-permissions
     */
    @Operation(
        summary = "Get Effective Permissions",
        description = "Retrieve the effective permissions for a role, including permissions inherited from parent roles"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/{id}/effective-permissions")
    public ResponseEntity<List<RoleNode>> getEffectivePermissions(
            @Parameter(description = "Role ID") @PathVariable UUID id) {
        List<RoleNode> permissions = roleService.getEffectivePermissions(id);
        return ResponseEntity.ok(permissions);
    }
}
