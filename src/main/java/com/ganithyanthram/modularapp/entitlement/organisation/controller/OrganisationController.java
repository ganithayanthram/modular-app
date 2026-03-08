package com.ganithyanthram.modularapp.entitlement.organisation.controller;

import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import com.ganithyanthram.modularapp.entitlement.organisation.service.OrganisationService;
import com.ganithyanthram.modularapp.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for Organisation management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/organisations")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Organization Management", description = "Admin endpoints for managing organizations")
@SecurityRequirement(name = "bearerAuth")
public class OrganisationController {
    
    private final OrganisationService organisationService;
    
    /**
     * Create a new organisation
     * POST /api/v1/admin/organisations
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createOrganisation(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateOrganisationRequest request) {
        
        UUID id = organisationService.createOrganisation(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get organisation by ID
     * GET /api/v1/admin/organisations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganisationResponse> getOrganisationById(@PathVariable UUID id) {
        OrganisationResponse response = organisationService.getOrganisationById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List all organisations with pagination and search
     * GET /api/v1/admin/organisations
     */
    @GetMapping
    public ResponseEntity<List<OrganisationResponse>> getAllOrganisations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        List<OrganisationResponse> organisations = organisationService.getAllOrganisations(page, size, search);
        return ResponseEntity.ok(organisations);
    }
    
    /**
     * Update organisation
     * PUT /api/v1/admin/organisations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganisationResponse> updateOrganisation(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganisationRequest request) {
        
        OrganisationResponse response = organisationService.updateOrganisation(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete organisation (soft delete)
     * DELETE /api/v1/admin/organisations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable UUID id) {
        organisationService.deleteOrganisation(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activate organisation
     * PATCH /api/v1/admin/organisations/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateOrganisation(@PathVariable UUID id) {
        organisationService.activateOrganisation(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Deactivate organisation
     * PATCH /api/v1/admin/organisations/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateOrganisation(@PathVariable UUID id) {
        organisationService.deactivateOrganisation(id);
        return ResponseEntity.ok().build();
    }
}
