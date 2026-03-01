package com.ganithyanthram.modularapp.entitlement.assignment.dto.request;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for overriding permissions for an individual
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverridePermissionRequest {
    
    @NotNull(message = "Individual ID is required")
    private UUID individualId;
    
    @NotNull(message = "Organisation ID is required")
    private UUID orgId;
    
    @NotNull(message = "Permissions are required")
    private List<RoleNode> permissions;
    
    private List<RoleNode> pages;
    
    @Size(max = 512, message = "Remarks must not exceed 512 characters")
    private String remarks;
}
