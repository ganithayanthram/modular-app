package com.ganithyanthram.modularapp.entitlement.individual.controller;

import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.service.IndividualService;
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
 * REST controller for Individual management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/individuals")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Individual Management", description = "Admin endpoints for managing individuals (users)")
@SecurityRequirement(name = "bearerAuth")
public class IndividualController {
    
    private final IndividualService individualService;
    
    /**
     * Create a new individual
     * POST /api/v1/admin/individuals
     */
    @Operation(
        summary = "Create Individual",
        description = "Create a new individual (user) in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Individual created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createIndividual(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody CreateIndividualRequest request) {
        
        UUID id = individualService.createIndividual(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get individual by ID
     * GET /api/v1/admin/individuals/{id}
     */
    @Operation(
        summary = "Get Individual by ID",
        description = "Retrieve detailed information about a specific individual"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Individual found",
            content = @Content(schema = @Schema(implementation = IndividualResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Individual not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<IndividualResponse> getIndividualById(
            @Parameter(description = "Individual ID") @PathVariable UUID id) {
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
