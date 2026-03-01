package com.ganithyanthram.modularapp.entitlement.role.dto.request;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 100, message = "Role name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotNull(message = "Organisation ID is required")
    private UUID orgId;
    
    private UUID parentRoleId;
    
    private List<RoleNode> permissions;
    
    private List<RoleNode> pages;
}
