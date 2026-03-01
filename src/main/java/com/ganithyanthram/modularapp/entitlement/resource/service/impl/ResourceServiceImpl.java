package com.ganithyanthram.modularapp.entitlement.resource.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Resource;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateResourceException;
import com.ganithyanthram.modularapp.entitlement.common.exception.ResourceNotFoundException;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.mapper.ResourceMapper;
import com.ganithyanthram.modularapp.entitlement.resource.repository.ResourceRepository;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Resource management with hierarchical tree structure
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    
    @Override
    @Transactional
    public UUID createResource(CreateResourceRequest request, UUID userId) {
        log.info("Creating resource with name: {}", request.getName());
        
        // Validate unique name
        if (resourceRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Resource with name '" + request.getName() + "' already exists");
        }
        
        // Validate parent resource exists if specified
        if (request.getParentResourceId() != null) {
            resourceRepository.findByIdAndIsActiveTrue(request.getParentResourceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent resource not found with ID: " + request.getParentResourceId()));
        }
        
        // Create entity
        Resource resource = resourceMapper.toEntity(request, userId);
        
        // Save to database
        UUID id = resourceRepository.create(resource);
        
        log.info("Resource created successfully with ID: {}", id);
        return id;
    }
    
    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(UUID id) {
        log.info("Fetching resource with ID: {}", id);
        
        Resource resource = resourceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        
        ResourceResponse response = resourceMapper.toResponse(resource);
        
        // Load children
        List<ResourceResponse> children = getChildResources(id);
        response.setChildren(children);
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources(int page, int size, String type) {
        log.info("Fetching resources - page: {}, size: {}, type: {}", page, size, type);
        
        int offset = page * size;
        List<Resource> resources;
        
        if (type != null && !type.trim().isEmpty()) {
            resources = resourceRepository.findByTypeAndIsActiveTrue(type, offset, size);
        } else {
            resources = resourceRepository.findByIsActiveTrue(offset, size);
        }
        
        return resources.stream()
                .map(resourceMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ResourceResponse updateResource(UUID id, UpdateResourceRequest request, UUID userId) {
        log.info("Updating resource with ID: {}", id);
        
        // Fetch existing resource
        Resource resource = resourceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        
        // Validate unique name (if name is being changed)
        if (!resource.getName().equals(request.getName()) &&
                resourceRepository.existsByNameAndIsActiveTrue(request.getName())) {
            throw new DuplicateResourceException("Resource with name '" + request.getName() + "' already exists");
        }
        
        // Update entity
        resourceMapper.updateEntity(resource, request, userId);
        
        // Save to database
        resourceRepository.update(resource);
        
        log.info("Resource updated successfully with ID: {}", id);
        return resourceMapper.toResponse(resource);
    }
    
    @Override
    @Transactional
    public void deleteResource(UUID id) {
        log.info("Deleting resource with ID: {}", id);
        
        // Verify resource exists
        resourceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        
        // Soft delete
        resourceRepository.softDelete(id);
        
        log.info("Resource deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getResourceHierarchy() {
        log.info("Fetching resource hierarchy");
        
        // Get root resources (no parent)
        List<Resource> rootResources = resourceRepository.findRootResources();
        
        // Build tree structure
        return rootResources.stream()
                .map(resource -> buildResourceTree(resource))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> getChildResources(UUID parentResourceId) {
        log.info("Fetching child resources for parent: {}", parentResourceId);
        
        List<Resource> children = resourceRepository.findByParentResourceId(parentResourceId);
        
        return children.stream()
                .map(resource -> buildResourceTree(resource))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countResourcesByType(String type) {
        if (type != null && !type.trim().isEmpty()) {
            return resourceRepository.countByTypeAndIsActiveTrue(type);
        }
        return resourceRepository.countActive();
    }
    
    /**
     * Recursively build resource tree with all children
     */
    private ResourceResponse buildResourceTree(Resource resource) {
        ResourceResponse response = resourceMapper.toResponse(resource);
        
        // Recursively load children
        List<Resource> children = resourceRepository.findByParentResourceId(resource.getId());
        List<ResourceResponse> childResponses = children.stream()
                .map(this::buildResourceTree)
                .collect(Collectors.toList());
        
        response.setChildren(childResponses);
        return response;
    }
}
