package com.ganithyanthram.modularapp.entitlement.assignment.controller;

import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.assignment.service.RoleAssignmentService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
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
 * REST controller for Role & Permission Assignment (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/assignments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Entitlement Assignment", description = "Admin endpoints for assigning roles and overriding permissions")
@SecurityRequirement(name = "bearerAuth")
public class EntitlementAssignmentController {
    
    private final RoleAssignmentService roleAssignmentService;
    private final PermissionOverrideService permissionOverrideService;
    
    /**
     * Assign role to individual
     * POST /api/v1/admin/assignments/roles
     */
    @Operation(
        summary = "Assign Role",
        description = "Assign a role to an individual in a specific organization"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Role assigned successfully"
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual or role not found",
            content = @Content
        )
    })
    @PostMapping("/roles")
    public ResponseEntity<Map<String, UUID>> assignRole(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        
        UUID id = roleAssignmentService.assignRole(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Revoke role from individual
     * DELETE /api/v1/admin/assignments/roles/{individualId}/{roleId}
     */
    @Operation(
        summary = "Revoke Role",
        description = "Revoke a role from an individual"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role revoked successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual or role assignment not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @DeleteMapping("/roles/{individualId}/{roleId}")
    public ResponseEntity<Void> revokeRole(
            @Parameter(description = "Individual ID") @PathVariable UUID individualId,
            @Parameter(description = "Role ID") @PathVariable UUID roleId) {
        
        roleAssignmentService.revokeRole(individualId, roleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get roles for individual
     * GET /api/v1/admin/assignments/individuals/{individualId}/roles
     */
    @Operation(
        summary = "Get Roles for Individual",
        description = "Retrieve all roles assigned to an individual, optionally filtered by organization"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/individuals/{individualId}/roles")
    public ResponseEntity<List<RoleResponse>> getRolesForIndividual(
            @Parameter(description = "Individual ID") @PathVariable UUID individualId,
            @Parameter(description = "Filter by organization ID (optional)") @RequestParam(required = false) UUID orgId) {
        
        List<RoleResponse> roles;
        if (orgId != null) {
            roles = roleAssignmentService.getRolesByIndividualAndOrganisation(individualId, orgId);
        } else {
            roles = roleAssignmentService.getRolesByIndividual(individualId);
        }
        
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Get individuals with role
     * GET /api/v1/admin/assignments/roles/{roleId}/individuals
     */
    @Operation(
        summary = "Get Individuals with Role",
        description = "Retrieve all individuals who have been assigned a specific role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Individuals retrieved successfully"
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
    @GetMapping("/roles/{roleId}/individuals")
    public ResponseEntity<List<IndividualResponse>> getIndividualsWithRole(
            @Parameter(description = "Role ID") @PathVariable UUID roleId) {
        List<IndividualResponse> individuals = roleAssignmentService.getIndividualsByRole(roleId);
        return ResponseEntity.ok(individuals);
    }
    
    /**
     * Override permissions for individual
     * POST /api/v1/admin/assignments/permissions
     */
    @Operation(
        summary = "Override Permissions",
        description = "Override specific permissions for an individual on a resource, superseding role-based permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Permission override created successfully"
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual or resource not found",
            content = @Content
        )
    })
    @PostMapping("/permissions")
    public ResponseEntity<Map<String, UUID>> overridePermissions(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody OverridePermissionRequest request) {
        
        UUID id = permissionOverrideService.overridePermissions(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get effective permissions for individual
     * GET /api/v1/admin/assignments/individuals/{individualId}/permissions
     */
    @Operation(
        summary = "Get Effective Permissions",
        description = "Retrieve the effective permissions for an individual in an organization, including role-based and overridden permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/individuals/{individualId}/permissions")
    public ResponseEntity<List<RoleNode>> getEffectivePermissions(
            @Parameter(description = "Individual ID") @PathVariable UUID individualId,
            @Parameter(description = "Organization ID", required = true) @RequestParam UUID orgId) {
        
        List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(individualId, orgId);
        return ResponseEntity.ok(permissions);
    }
}
