package com.ganithyanthram.modularapp.entitlement.assignment.service;

import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.UserEntitlementResponse;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for permission override management
 */
public interface PermissionOverrideService {
    
    /**
     * Override permissions for an individual in an organisation
     */
    UUID overridePermissions(OverridePermissionRequest request, UUID userId);
    
    /**
     * Get effective permissions for an individual in an organisation
     * (combines role permissions + individual overrides)
     */
    List<RoleNode> getEffectivePermissions(UUID individualId, UUID orgId);
    
    /**
     * Get complete user entitlements (roles + permissions + resources)
     */
    UserEntitlementResponse getUserEntitlements(UUID individualId);
    
    /**
     * Delete permission overrides for an individual in an organisation
     */
    void deletePermissionOverride(UUID individualId, UUID orgId);
}
