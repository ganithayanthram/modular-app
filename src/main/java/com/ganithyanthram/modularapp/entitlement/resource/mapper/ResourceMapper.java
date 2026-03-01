package com.ganithyanthram.modularapp.entitlement.resource.mapper;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Resource;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Mapper for converting between Resource entity and DTOs
 */
@Component
public class ResourceMapper {
    
    /**
     * Convert CreateResourceRequest to Resource entity
     */
    public Resource toEntity(CreateResourceRequest request, UUID createdBy) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID());
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setParentResourceId(request.getParentResourceId());
        resource.setValidations(request.getValidations());
        resource.setIsActive(true);
        resource.setCreatedBy(createdBy);
        resource.setUpdatedBy(createdBy);
        resource.setCreatedOn(LocalDateTime.now());
        resource.setUpdatedOn(LocalDateTime.now());
        return resource;
    }
    
    /**
     * Update Resource entity from UpdateResourceRequest
     */
    public void updateEntity(Resource resource, UpdateResourceRequest request, UUID updatedBy) {
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setValidations(request.getValidations());
        resource.setUpdatedBy(updatedBy);
        resource.setUpdatedOn(LocalDateTime.now());
    }
    
    /**
     * Convert Resource entity to ResourceResponse
     */
    public ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .parentResourceId(resource.getParentResourceId())
                .validations(resource.getValidations())
                .isActive(resource.getIsActive())
                .createdBy(resource.getCreatedBy())
                .updatedBy(resource.getUpdatedBy())
                .createdOn(resource.getCreatedOn())
                .updatedOn(resource.getUpdatedOn())
                .children(new ArrayList<>())
                .build();
    }
}
