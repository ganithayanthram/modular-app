package com.ganithyanthram.modularapp.entitlement.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for assigning a role to an individual
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {
    
    @NotNull(message = "Individual ID is required")
    private UUID individualId;
    
    @NotNull(message = "Role ID is required")
    private UUID roleId;
    
    @NotNull(message = "Organisation ID is required")
    private UUID orgId;
}
