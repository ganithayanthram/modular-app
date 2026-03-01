package com.ganithyanthram.modularapp.entitlement.organisation.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Organisation;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateOrganisationException;
import com.ganithyanthram.modularapp.entitlement.common.exception.OrganisationNotFoundException;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import com.ganithyanthram.modularapp.entitlement.organisation.mapper.OrganisationMapper;
import com.ganithyanthram.modularapp.entitlement.organisation.repository.OrganisationRepository;
import com.ganithyanthram.modularapp.entitlement.organisation.service.OrganisationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Organisation management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganisationServiceImpl implements OrganisationService {
    
    private final OrganisationRepository organisationRepository;
    private final OrganisationMapper organisationMapper;
    
    @Override
    @Transactional
    public UUID createOrganisation(CreateOrganisationRequest request, UUID userId) {
        log.info("Creating organisation with name: {}", request.getName());
        
        // Validate unique name
        if (organisationRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new DuplicateOrganisationException("Organisation with name '" + request.getName() + "' already exists");
        }
        
        // Create entity
        Organisation organisation = organisationMapper.toEntity(request, userId);
        
        // Save to database
        UUID id = organisationRepository.create(organisation);
        
        log.info("Organisation created successfully with ID: {}", id);
        return id;
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationById(UUID id) {
        log.info("Fetching organisation with ID: {}", id);
        
        Organisation organisation = organisationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found with ID: " + id));
        
        return organisationMapper.toResponse(organisation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrganisationResponse> getAllOrganisations(int page, int size, String search) {
        log.info("Fetching organisations - page: {}, size: {}, search: {}", page, size, search);
        
        int offset = page * size;
        List<Organisation> organisations;
        
        if (search != null && !search.trim().isEmpty()) {
            organisations = organisationRepository.searchByName(search, offset, size);
        } else {
            organisations = organisationRepository.findByIsActiveTrue(offset, size);
        }
        
        return organisations.stream()
                .map(organisationMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrganisationResponse updateOrganisation(UUID id, UpdateOrganisationRequest request, UUID userId) {
        log.info("Updating organisation with ID: {}", id);
        
        // Fetch existing organisation
        Organisation organisation = organisationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found with ID: " + id));
        
        // Validate unique name (if name is being changed)
        if (!organisation.getName().equals(request.getName()) &&
                organisationRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new DuplicateOrganisationException("Organisation with name '" + request.getName() + "' already exists");
        }
        
        // Update entity
        organisationMapper.updateEntity(organisation, request, userId);
        
        // Save to database
        organisationRepository.update(organisation);
        
        log.info("Organisation updated successfully with ID: {}", id);
        return organisationMapper.toResponse(organisation);
    }
    
    @Override
    @Transactional
    public void deleteOrganisation(UUID id) {
        log.info("Deleting organisation with ID: {}", id);
        
        // Verify organisation exists
        organisationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found with ID: " + id));
        
        // Soft delete
        organisationRepository.softDelete(id);
        
        log.info("Organisation deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void activateOrganisation(UUID id) {
        log.info("Activating organisation with ID: {}", id);
        
        // Verify organisation exists
        organisationRepository.findById(id)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found with ID: " + id));
        
        organisationRepository.activate(id);
        
        log.info("Organisation activated successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void deactivateOrganisation(UUID id) {
        log.info("Deactivating organisation with ID: {}", id);
        
        // Verify organisation exists
        organisationRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found with ID: " + id));
        
        organisationRepository.deactivate(id);
        
        log.info("Organisation deactivated successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countActiveOrganisations() {
        return organisationRepository.countActive();
    }
}
