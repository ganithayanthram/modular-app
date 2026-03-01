package com.ganithyanthram.modularapp.entitlement.individual.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for individual data
 * Note: Password is never returned in responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualResponse {
    
    private UUID id;
    private String name;
    private String email;
    private String mobileNumber;
    private Map<String, Object> metaData;
    private Boolean isActive;
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
