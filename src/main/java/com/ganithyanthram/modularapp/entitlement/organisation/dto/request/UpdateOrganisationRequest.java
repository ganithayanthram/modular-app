package com.ganithyanthram.modularapp.entitlement.organisation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing organisation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganisationRequest {
    
    @NotBlank(message = "Organisation name is required")
    @Size(min = 3, max = 255, message = "Organisation name must be between 3 and 255 characters")
    private String name;
    
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    private Map<String, Object> metaData;
    
    @Size(max = 50, message = "Status must not exceed 50 characters")
    private String status;
}
