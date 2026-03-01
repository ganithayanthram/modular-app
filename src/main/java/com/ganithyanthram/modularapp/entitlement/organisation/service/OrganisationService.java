package com.ganithyanthram.modularapp.entitlement.organisation.service;

import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Organisation management
 */
public interface OrganisationService {
    
    /**
     * Create a new organisation
     */
    UUID createOrganisation(CreateOrganisationRequest request, UUID userId);
    
    /**
     * Get organisation by ID
     */
    OrganisationResponse getOrganisationById(UUID id);
    
    /**
     * Get all organisations with pagination
     */
    List<OrganisationResponse> getAllOrganisations(int page, int size, String search);
    
    /**
     * Update organisation
     */
    OrganisationResponse updateOrganisation(UUID id, UpdateOrganisationRequest request, UUID userId);
    
    /**
     * Delete organisation (soft delete)
     */
    void deleteOrganisation(UUID id);
    
    /**
     * Activate organisation
     */
    void activateOrganisation(UUID id);
    
    /**
     * Deactivate organisation
     */
    void deactivateOrganisation(UUID id);
    
    /**
     * Count active organisations
     */
    long countActiveOrganisations();
}
