package com.ganithyanthram.modularapp.entitlement.assignment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for role assignment data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {
    
    private UUID id;
    private UUID individualId;
    private UUID roleId;
    private UUID orgId;
    private UUID createdBy;
    private LocalDateTime createdOn;
}
