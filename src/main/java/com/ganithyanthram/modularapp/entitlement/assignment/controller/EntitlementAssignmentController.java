package com.ganithyanthram.modularapp.entitlement.assignment.controller;

import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.assignment.service.RoleAssignmentService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class EntitlementAssignmentController {
    
    private final RoleAssignmentService roleAssignmentService;
    private final PermissionOverrideService permissionOverrideService;
    
    /**
     * Assign role to individual
     * POST /api/v1/admin/assignments/roles
     */
    @PostMapping("/roles")
    public ResponseEntity<Map<String, UUID>> assignRole(
            @Valid @RequestBody AssignRoleRequest request) {
        
        UUID userId = UUID.randomUUID(); // TODO: Get from @CurrentUser
        UUID id = roleAssignmentService.assignRole(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Revoke role from individual
     * DELETE /api/v1/admin/assignments/roles/{individualId}/{roleId}
     */
    @DeleteMapping("/roles/{individualId}/{roleId}")
    public ResponseEntity<Void> revokeRole(
            @PathVariable UUID individualId,
            @PathVariable UUID roleId) {
        
        roleAssignmentService.revokeRole(individualId, roleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get roles for individual
     * GET /api/v1/admin/assignments/individuals/{individualId}/roles
     */
    @GetMapping("/individuals/{individualId}/roles")
    public ResponseEntity<List<RoleResponse>> getRolesForIndividual(
            @PathVariable UUID individualId,
            @RequestParam(required = false) UUID orgId) {
        
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
    @GetMapping("/roles/{roleId}/individuals")
    public ResponseEntity<List<IndividualResponse>> getIndividualsWithRole(@PathVariable UUID roleId) {
        List<IndividualResponse> individuals = roleAssignmentService.getIndividualsByRole(roleId);
        return ResponseEntity.ok(individuals);
    }
    
    /**
     * Override permissions for individual
     * POST /api/v1/admin/assignments/permissions
     */
    @PostMapping("/permissions")
    public ResponseEntity<Map<String, UUID>> overridePermissions(
            @Valid @RequestBody OverridePermissionRequest request) {
        
        UUID userId = UUID.randomUUID(); // TODO: Get from @CurrentUser
        UUID id = permissionOverrideService.overridePermissions(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get effective permissions for individual
     * GET /api/v1/admin/assignments/individuals/{individualId}/permissions
     */
    @GetMapping("/individuals/{individualId}/permissions")
    public ResponseEntity<List<RoleNode>> getEffectivePermissions(
            @PathVariable UUID individualId,
            @RequestParam UUID orgId) {
        
        List<RoleNode> permissions = permissionOverrideService.getEffectivePermissions(individualId, orgId);
        return ResponseEntity.ok(permissions);
    }
}
