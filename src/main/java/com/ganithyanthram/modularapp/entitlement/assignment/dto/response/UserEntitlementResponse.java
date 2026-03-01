package com.ganithyanthram.modularapp.entitlement.assignment.dto.response;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for user entitlements (roles + permissions + resources)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntitlementResponse {
    
    private UUID individualId;
    private String name;
    private String email;
    private List<OrganisationEntitlement> organisations;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganisationEntitlement {
        private UUID orgId;
        private String orgName;
        private List<RoleResponse> roles;
        private List<RoleNode> effectivePermissions;
        private List<ResourceResponse> accessibleResources;
    }
}
