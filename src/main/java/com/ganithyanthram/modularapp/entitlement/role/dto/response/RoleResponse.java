package com.ganithyanthram.modularapp.entitlement.role.dto.response;

import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for role data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    
    private UUID id;
    private String name;
    private String description;
    private UUID orgId;
    private UUID parentRoleId;
    private List<RoleNode> permissions;
    private List<RoleNode> pages;
    private Boolean isActive;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
