package com.ganithyanthram.modularapp.entitlement.individual.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing individual
 * Note: Password cannot be updated via this endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIndividualRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(max = 100, message = "Mobile number must not exceed 100 characters")
    private String mobileNumber;
    
    private Map<String, Object> metaData;
}
