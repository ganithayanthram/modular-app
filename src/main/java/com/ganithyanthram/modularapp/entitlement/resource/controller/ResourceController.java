package com.ganithyanthram.modularapp.entitlement.resource.controller;

import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
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
 * REST controller for Resource management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/resources")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Resource Management", description = "Admin endpoints for managing hierarchical resources (menus, pages, actions)")
@SecurityRequirement(name = "bearerAuth")
public class ResourceController {
    
    private final ResourceService resourceService;
    
    /**
     * Create a new resource
     * POST /api/v1/admin/resources
     */
    @Operation(
        summary = "Create Resource",
        description = "Create a new hierarchical resource (menu, page, or action). Resources can have parent-child relationships."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Resource created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createResource(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Valid @RequestBody CreateResourceRequest request) {
        
        UUID id = resourceService.createResource(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get resource by ID
     * GET /api/v1/admin/resources/{id}
     */
    @Operation(
        summary = "Get Resource by ID",
        description = "Retrieve detailed information about a specific resource"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource found",
            content = @Content(schema = @Schema(implementation = ResourceResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(
            @Parameter(description = "Resource ID") @PathVariable UUID id) {
        ResourceResponse response = resourceService.getResourceById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List all resources with pagination and optional type filter
     * GET /api/v1/admin/resources
     */
    @Operation(
        summary = "List Resources",
        description = "Retrieve a paginated list of resources. Can be filtered by resource type (MENU, PAGE, ACTION)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resources retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by resource type (MENU, PAGE, ACTION)")
            @RequestParam(required = false) String type) {
        
        List<ResourceResponse> resources = resourceService.getAllResources(page, size, type);
        return ResponseEntity.ok(resources);
    }
    
    /**
     * Update resource
     * PUT /api/v1/admin/resources/{id}
     */
    @Operation(
        summary = "Update Resource",
        description = "Update an existing resource's details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource updated successfully",
            content = @Content(schema = @Schema(implementation = ResourceResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation error",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @Parameter(hidden = true) @CurrentUser UUID userId,
            @Parameter(description = "Resource ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceRequest request) {
        
        ResourceResponse response = resourceService.updateResource(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete resource (soft delete)
     * DELETE /api/v1/admin/resources/{id}
     */
    @Operation(
        summary = "Delete Resource",
        description = "Soft delete a resource. The resource will be marked as inactive but not removed from the database."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Resource deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Resource not found",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(
            @Parameter(description = "Resource ID") @PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get resource hierarchy (tree structure)
     * GET /api/v1/admin/resources/hierarchy
     */
    @Operation(
        summary = "Get Resource Hierarchy",
        description = "Retrieve the complete hierarchical tree structure of all resources (menus, pages, and actions)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resource hierarchy retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/hierarchy")
    public ResponseEntity<List<ResourceResponse>> getResourceHierarchy() {
        List<ResourceResponse> hierarchy = resourceService.getResourceHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
}
