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
    @Operation(
        summary = "Create Organization",
        description = "Create a new organization in the system. Organizations are used for tenant isolation and grouping users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Organization created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token missing or invalid",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createOrganisation(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody CreateOrganisationRequest request) {
        
        UUID id = organisationService.createOrganisation(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get organisation by ID
     * GET /api/v1/admin/organisations/{id}
     */
    @Operation(
        summary = "Get Organization by ID",
        description = "Retrieve detailed information about a specific organization"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization found",
            content = @Content(schema = @Schema(implementation = OrganisationResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrganisationResponse> getOrganisationById(
            @Parameter(description = "Organization ID", example = "123e4567-e89b-12d3-a456-426614174000") 
            @PathVariable UUID id) {
        OrganisationResponse response = organisationService.getOrganisationById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List all organisations with pagination and search
     * GET /api/v1/admin/organisations
     */
    @Operation(
        summary = "List Organizations",
        description = "Retrieve a paginated list of all organizations. Supports optional search filtering."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organizations retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<List<OrganisationResponse>> getAllOrganisations(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Search term to filter organizations by name")
            @RequestParam(required = false) String search) {
        
        List<OrganisationResponse> organisations = organisationService.getAllOrganisations(page, size, search);
        return ResponseEntity.ok(organisations);
    }
    
    /**
     * Update organisation
     * PUT /api/v1/admin/organisations/{id}
     */
    @Operation(
        summary = "Update Organization",
        description = "Update an existing organization's details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization updated successfully",
            content = @Content(schema = @Schema(implementation = OrganisationResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrganisationResponse> updateOrganisation(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Parameter(description = "Organization ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateOrganisationRequest request) {
        
        OrganisationResponse response = organisationService.updateOrganisation(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete organisation (soft delete)
     * DELETE /api/v1/admin/organisations/{id}
     */
    @Operation(
        summary = "Delete Organization",
        description = "Soft delete an organization. The organization will be marked as inactive but not removed from the database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Organization deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(
            @Parameter(description = "Organization ID") @PathVariable UUID id) {
        organisationService.deleteOrganisation(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activate organisation
     * PATCH /api/v1/admin/organisations/{id}/activate
     */
    @Operation(
        summary = "Activate Organization",
        description = "Activate an organization, allowing its users to access the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization activated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateOrganisation(
            @Parameter(description = "Organization ID") @PathVariable UUID id) {
        organisationService.activateOrganisation(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Deactivate organisation
     * PATCH /api/v1/admin/organisations/{id}/deactivate
     */
    @Operation(
        summary = "Deactivate Organization",
        description = "Deactivate an organization, preventing its users from accessing the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Organization deactivated successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Organization not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateOrganisation(
            @Parameter(description = "Organization ID") @PathVariable UUID id) {
        organisationService.deactivateOrganisation(id);
        return ResponseEntity.ok().build();
    }
}
