package com.ganithyanthram.modularapp.entitlement.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for resource data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponse {
    
    private UUID id;
    private String name;
    private String description;
    private String type;
    private UUID parentResourceId;
    private Map<String, Object> validations;
    private Boolean isActive;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private List<ResourceResponse> children;
}
