package com.ganithyanthram.modularapp.entitlement.role.dto.request;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating an existing role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 100, message = "Role name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    private List<RoleNode> permissions;
    
    private List<RoleNode> pages;
}
