package com.ganithyanthram.modularapp.entitlement.resource.service;

import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Resource management
 */
public interface ResourceService {
    
    /**
     * Create a new resource
     */
    UUID createResource(CreateResourceRequest request, UUID userId);
    
    /**
     * Get resource by ID
     */
    ResourceResponse getResourceById(UUID id);
    
    /**
     * Get all resources with pagination
     */
    List<ResourceResponse> getAllResources(int page, int size, String type);
    
    /**
     * Update resource
     */
    ResourceResponse updateResource(UUID id, UpdateResourceRequest request, UUID userId);
    
    /**
     * Delete resource (soft delete)
     */
    void deleteResource(UUID id);
    
    /**
     * Get resource hierarchy (tree structure with all children)
     */
    List<ResourceResponse> getResourceHierarchy();
    
    /**
     * Get child resources for a parent
     */
    List<ResourceResponse> getChildResources(UUID parentResourceId);
    
    /**
     * Count resources by type
     */
    long countResourcesByType(String type);
}
