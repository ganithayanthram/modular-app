package com.ganithyanthram.modularapp.entitlement.individual.service;

import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Individual management
 */
public interface IndividualService {
    
    /**
     * Create a new individual
     */
    UUID createIndividual(CreateIndividualRequest request, UUID userId);
    
    /**
     * Get individual by ID
     */
    IndividualResponse getIndividualById(UUID id);
    
    /**
     * Get all individuals with pagination and optional search
     */
    List<IndividualResponse> getAllIndividuals(int page, int size, String search);
    
    /**
     * Update individual
     */
    IndividualResponse updateIndividual(UUID id, UpdateIndividualRequest request, UUID userId);
    
    /**
     * Delete individual (soft delete)
     */
    void deleteIndividual(UUID id);
    
    /**
     * Activate individual
     */
    void activateIndividual(UUID id);
    
    /**
     * Deactivate individual
     */
    void deactivateIndividual(UUID id);
    
    /**
     * Count active individuals
     */
    long countActiveIndividuals();
}
