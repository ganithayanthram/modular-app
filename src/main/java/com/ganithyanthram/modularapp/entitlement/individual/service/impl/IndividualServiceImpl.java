package com.ganithyanthram.modularapp.entitlement.individual.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateIndividualException;
import com.ganithyanthram.modularapp.entitlement.common.exception.IndividualNotFoundException;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.mapper.IndividualMapper;
import com.ganithyanthram.modularapp.entitlement.individual.repository.IndividualRepository;
import com.ganithyanthram.modularapp.entitlement.individual.service.IndividualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Individual management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualServiceImpl implements IndividualService {
    
    private final IndividualRepository individualRepository;
    private final IndividualMapper individualMapper;
    
    @Override
    @Transactional
    public UUID createIndividual(CreateIndividualRequest request, UUID userId) {
        log.info("Creating individual with email: {}", request.getEmail());
        
        // Validate unique email
        if (request.getEmail() != null && individualRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new DuplicateIndividualException("Individual with email '" + request.getEmail() + "' already exists");
        }
        
        // Create entity (password will be hashed in mapper)
        Individual individual = individualMapper.toEntity(request, userId);
        
        // Save to database
        UUID id = individualRepository.create(individual);
        
        log.info("Individual created successfully with ID: {}", id);
        return id;
    }
    
    @Override
    @Transactional(readOnly = true)
    public IndividualResponse getIndividualById(UUID id) {
        log.info("Fetching individual with ID: {}", id);
        
        Individual individual = individualRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + id));
        
        return individualMapper.toResponse(individual);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<IndividualResponse> getAllIndividuals(int page, int size, String search) {
        log.info("Fetching individuals - page: {}, size: {}, search: {}", page, size, search);
        
        int offset = page * size;
        List<Individual> individuals;
        
        if (search != null && !search.trim().isEmpty()) {
            individuals = individualRepository.searchByNameOrEmail(search, offset, size);
        } else {
            individuals = individualRepository.findByIsActiveTrue(offset, size);
        }
        
        return individuals.stream()
                .map(individualMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public IndividualResponse updateIndividual(UUID id, UpdateIndividualRequest request, UUID userId) {
        log.info("Updating individual with ID: {}", id);
        
        // Fetch existing individual
        Individual individual = individualRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + id));
        
        // Validate unique email (if email is being changed)
        if (request.getEmail() != null && !request.getEmail().equals(individual.getEmail()) &&
                individualRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new DuplicateIndividualException("Individual with email '" + request.getEmail() + "' already exists");
        }
        
        // Update entity (password is not updated via this method)
        individualMapper.updateEntity(individual, request, userId);
        
        // Save to database
        individualRepository.update(individual);
        
        log.info("Individual updated successfully with ID: {}", id);
        return individualMapper.toResponse(individual);
    }
    
    @Override
    @Transactional
    public void deleteIndividual(UUID id) {
        log.info("Deleting individual with ID: {}", id);
        
        // Verify individual exists
        individualRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + id));
        
        // Soft delete
        individualRepository.softDelete(id);
        
        log.info("Individual deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void activateIndividual(UUID id) {
        log.info("Activating individual with ID: {}", id);
        
        // Verify individual exists
        individualRepository.findById(id)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + id));
        
        individualRepository.activate(id);
        
        log.info("Individual activated successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void deactivateIndividual(UUID id) {
        log.info("Deactivating individual with ID: {}", id);
        
        // Verify individual exists
        individualRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + id));
        
        individualRepository.deactivate(id);
        
        log.info("Individual deactivated successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countActiveIndividuals() {
        return individualRepository.countActive();
    }
}
