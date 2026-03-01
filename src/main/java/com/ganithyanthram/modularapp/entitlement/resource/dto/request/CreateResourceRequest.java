package com.ganithyanthram.modularapp.entitlement.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for creating a new resource
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {
    
    @NotBlank(message = "Resource name is required")
    @Size(min = 3, max = 100, message = "Resource name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
    
    @NotBlank(message = "Resource type is required")
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;
    
    private UUID parentResourceId;
    
    private Map<String, Object> validations;
}
