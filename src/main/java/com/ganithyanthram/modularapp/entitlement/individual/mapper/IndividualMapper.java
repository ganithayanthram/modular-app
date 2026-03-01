package com.ganithyanthram.modularapp.entitlement.individual.mapper;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Individual entity and DTOs
 */
@Component
public class IndividualMapper {
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Convert CreateIndividualRequest to Individual entity
     */
    public Individual toEntity(CreateIndividualRequest request, UUID createdBy) {
        Individual individual = new Individual();
        individual.setId(UUID.randomUUID());
        individual.setName(request.getName());
        individual.setEmail(request.getEmail());
        individual.setMobileNumber(request.getMobileNumber());
        // Hash password before storing
        individual.setPassword(passwordEncoder.encode(request.getPassword()));
        individual.setMetaData(request.getMetaData());
        individual.setIsActive(true);
        individual.setCreatedBy(createdBy);
        individual.setUpdatedBy(createdBy);
        individual.setCreatedOn(LocalDateTime.now());
        individual.setUpdatedOn(LocalDateTime.now());
        return individual;
    }
    
    /**
     * Update Individual entity from UpdateIndividualRequest
     * Note: Password is not updated via this method
     */
    public void updateEntity(Individual individual, UpdateIndividualRequest request, UUID updatedBy) {
        individual.setName(request.getName());
        individual.setEmail(request.getEmail());
        individual.setMobileNumber(request.getMobileNumber());
        individual.setMetaData(request.getMetaData());
        individual.setUpdatedBy(updatedBy);
        individual.setUpdatedOn(LocalDateTime.now());
    }
    
    /**
     * Convert Individual entity to IndividualResponse
     * Note: Password is never included in response
     */
    public IndividualResponse toResponse(Individual individual) {
        return IndividualResponse.builder()
                .id(individual.getId())
                .name(individual.getName())
                .email(individual.getEmail())
                .mobileNumber(individual.getMobileNumber())
                .metaData(individual.getMetaData())
                .isActive(individual.getIsActive())
                .createdBy(individual.getCreatedBy())
                .updatedBy(individual.getUpdatedBy())
                .createdOn(individual.getCreatedOn())
                .updatedOn(individual.getUpdatedOn())
                .build();
    }
}
