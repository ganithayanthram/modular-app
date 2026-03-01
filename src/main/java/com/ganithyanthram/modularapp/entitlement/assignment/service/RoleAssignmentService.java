package com.ganithyanthram.modularapp.entitlement.assignment.service;

import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.RoleAssignmentResponse;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for role assignment management
 */
public interface RoleAssignmentService {
    
    /**
     * Assign a role to an individual
     */
    UUID assignRole(AssignRoleRequest request, UUID userId);
    
    /**
     * Revoke a role from an individual
     */
    void revokeRole(UUID individualId, UUID roleId);
    
    /**
     * Get all roles assigned to an individual
     */
    List<RoleResponse> getRolesByIndividual(UUID individualId);
    
    /**
     * Get all roles assigned to an individual in a specific organisation
     */
    List<RoleResponse> getRolesByIndividualAndOrganisation(UUID individualId, UUID orgId);
    
    /**
     * Get all individuals with a specific role
     */
    List<IndividualResponse> getIndividualsByRole(UUID roleId);
    
    /**
     * Get role assignment details
     */
    RoleAssignmentResponse getAssignment(UUID individualId, UUID roleId);
}
