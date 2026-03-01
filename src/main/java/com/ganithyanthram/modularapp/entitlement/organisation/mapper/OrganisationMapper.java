package com.ganithyanthram.modularapp.entitlement.organisation.mapper;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Organisation;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Organisation entity and DTOs
 */
@Component
public class OrganisationMapper {
    
    /**
     * Convert CreateOrganisationRequest to Organisation entity
     */
    public Organisation toEntity(CreateOrganisationRequest request, UUID createdBy) {
        Organisation organisation = new Organisation();
        organisation.setId(UUID.randomUUID());
        organisation.setName(request.getName());
        organisation.setCategory(request.getCategory());
        organisation.setMetaData(request.getMetaData());
        organisation.setStatus(request.getStatus());
        organisation.setIsActive(true);
        organisation.setCreatedBy(createdBy);
        organisation.setUpdatedBy(createdBy);
        organisation.setCreatedOn(LocalDateTime.now());
        organisation.setUpdatedOn(LocalDateTime.now());
        return organisation;
    }
    
    /**
     * Update Organisation entity from UpdateOrganisationRequest
     */
    public void updateEntity(Organisation organisation, UpdateOrganisationRequest request, UUID updatedBy) {
        organisation.setName(request.getName());
        organisation.setCategory(request.getCategory());
        organisation.setMetaData(request.getMetaData());
        organisation.setStatus(request.getStatus());
        organisation.setUpdatedBy(updatedBy);
        organisation.setUpdatedOn(LocalDateTime.now());
    }
    
    /**
     * Convert Organisation entity to OrganisationResponse
     */
    public OrganisationResponse toResponse(Organisation organisation) {
        return OrganisationResponse.builder()
                .id(organisation.getId())
                .name(organisation.getName())
                .category(organisation.getCategory())
                .metaData(organisation.getMetaData())
                .isActive(organisation.getIsActive())
                .status(organisation.getStatus())
                .createdBy(organisation.getCreatedBy())
                .updatedBy(organisation.getUpdatedBy())
                .createdOn(organisation.getCreatedOn())
                .updatedOn(organisation.getUpdatedOn())
                .build();
    }
}
