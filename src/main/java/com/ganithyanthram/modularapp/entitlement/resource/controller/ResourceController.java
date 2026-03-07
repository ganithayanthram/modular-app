package com.ganithyanthram.modularapp.entitlement.resource.controller;

import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
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
 * REST controller for Resource management (Admin APIs)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/resources")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ResourceController {
    
    private final ResourceService resourceService;
    
    /**
     * Create a new resource
     * POST /api/v1/admin/resources
     */
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createResource(
            @CurrentUser UUID userId,
            @Valid @RequestBody CreateResourceRequest request) {
        
        UUID id = resourceService.createResource(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
    
    /**
     * Get resource by ID
     * GET /api/v1/admin/resources/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResourceById(@PathVariable UUID id) {
        ResourceResponse response = resourceService.getResourceById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * List all resources with pagination and optional type filter
     * GET /api/v1/admin/resources
     */
    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        
        List<ResourceResponse> resources = resourceService.getAllResources(page, size, type);
        return ResponseEntity.ok(resources);
    }
    
    /**
     * Update resource
     * PUT /api/v1/admin/resources/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(
            @CurrentUser UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceRequest request) {
        
        ResourceResponse response = resourceService.updateResource(id, request, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete resource (soft delete)
     * DELETE /api/v1/admin/resources/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get resource hierarchy (tree structure)
     * GET /api/v1/admin/resources/hierarchy
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<List<ResourceResponse>> getResourceHierarchy() {
        List<ResourceResponse> hierarchy = resourceService.getResourceHierarchy();
        return ResponseEntity.ok(hierarchy);
    }
}
