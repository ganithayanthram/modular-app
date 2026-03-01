package com.ganithyanthram.modularapp.entitlement.role.service;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Role management
 */
public interface RoleService {
    
    /**
     * Create a new role
     */
    UUID createRole(CreateRoleRequest request, UUID userId);
    
    /**
     * Get role by ID
     */
    RoleResponse getRoleById(UUID id);
    
    /**
     * Get roles by organisation with pagination
     */
    List<RoleResponse> getRolesByOrganisation(UUID orgId, int page, int size);
    
    /**
     * Get all roles with pagination
     */
    List<RoleResponse> getAllRoles(int page, int size);
    
    /**
     * Update role
     */
    RoleResponse updateRole(UUID id, UpdateRoleRequest request, UUID userId);
    
    /**
     * Delete role (soft delete)
     */
    void deleteRole(UUID id);
    
    /**
     * Activate role
     */
    void activateRole(UUID id);
    
    /**
     * Get effective permissions for a role (includes parent role permissions)
     */
    List<RoleNode> getEffectivePermissions(UUID roleId);
    
    /**
     * Count roles by organisation
     */
    long countRolesByOrganisation(UUID orgId);
}
