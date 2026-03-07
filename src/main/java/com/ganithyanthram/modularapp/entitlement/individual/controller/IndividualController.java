package com.ganithyanthram.modularapp.entitlement.individual.controller;

import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.service.IndividualService;
import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Individual management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/individuals")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class IndividualController {
    
    private final IndividualService individualService;
    
    /**
     * Create a new individual
     * POST /api/v1/admin/individuals
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createIndividual(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateIndividualRequest request) {
        
        UUID id = individualService.createIndividual(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get individual by ID
     * GET /api/v1/admin/individuals/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<IndividualResponse> getIndividualById(@PathVariable UUID id) {
        IndividualResponse response = individualService.getIndividualById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List all individuals with pagination and search
     * GET /api/v1/admin/individuals
     */
    @GetMapping
    public ResponseEntity<List<IndividualResponse>> getAllIndividuals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        List<IndividualResponse> individuals = individualService.getAllIndividuals(page, size, search);
        return ResponseEntity.ok(individuals);
    }
    
    /**
     * Update individual
     * PUT /api/v1/admin/individuals/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<IndividualResponse> updateIndividual(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIndividualRequest request) {
        
        IndividualResponse response = individualService.updateIndividual(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete individual (soft delete)
     * DELETE /api/v1/admin/individuals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndividual(@PathVariable UUID id) {
        individualService.deleteIndividual(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activate individual
     * PATCH /api/v1/admin/individuals/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateIndividual(@PathVariable UUID id) {
        individualService.activateIndividual(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Deactivate individual
     * PATCH /api/v1/admin/individuals/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateIndividual(@PathVariable UUID id) {
        individualService.deactivateIndividual(id);
        return ResponseEntity.ok().build();
    }
}
