package com.ganithyanthram.modularapp.entitlement.organisation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for organisation data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationResponse {
    
    private UUID id;
    private String name;
    private String category;
    private Map<String, Object> metaData;
    private Boolean isActive;
    private String status;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
