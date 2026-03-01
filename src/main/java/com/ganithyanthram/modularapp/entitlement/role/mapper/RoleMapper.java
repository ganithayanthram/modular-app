package com.ganithyanthram.modularapp.entitlement.role.mapper;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Roles;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper for converting between Roles entity and DTOs
 */
@Component
public class RoleMapper {
    
    /**
     * Convert CreateRoleRequest to Roles entity
     */
    public Roles toEntity(CreateRoleRequest request, UUID createdBy) {
        Roles role = new Roles();
        role.setId(UUID.randomUUID());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setOrgId(request.getOrgId());
        role.setParentRoleId(request.getParentRoleId());
        role.setPermissions(request.getPermissions());
        role.setPages(request.getPages());
        role.setIsActive(true);
        role.setCreatedBy(createdBy);
        role.setUpdatedBy(createdBy);
        role.setCreatedOn(LocalDateTime.now());
        role.setUpdatedOn(LocalDateTime.now());
        return role;
    }
    
    /**
     * Update Roles entity from UpdateRoleRequest
     */
    public void updateEntity(Roles role, UpdateRoleRequest request, UUID updatedBy) {
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(request.getPermissions());
        role.setPages(request.getPages());
        role.setUpdatedBy(updatedBy);
        role.setUpdatedOn(LocalDateTime.now());
    }
    
    /**
     * Convert Roles entity to RoleResponse
     */
    public RoleResponse toResponse(Roles role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .orgId(role.getOrgId())
                .parentRoleId(role.getParentRoleId())
                .permissions(role.getPermissions())
                .pages(role.getPages())
                .isActive(role.getIsActive())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .createdOn(role.getCreatedOn())
                .updatedOn(role.getUpdatedOn())
                .build();
    }
}
