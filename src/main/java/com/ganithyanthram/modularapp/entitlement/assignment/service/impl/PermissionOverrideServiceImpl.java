package com.ganithyanthram.modularapp.entitlement.assignment.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.IndividualPermission;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.IndividualRole;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Organisation;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.UserEntitlementResponse;
import com.ganithyanthram.modularapp.entitlement.assignment.repository.IndividualPermissionRepository;
import com.ganithyanthram.modularapp.entitlement.assignment.repository.IndividualRoleRepository;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.common.exception.IndividualNotFoundException;
import com.ganithyanthram.modularapp.entitlement.individual.repository.IndividualRepository;
import com.ganithyanthram.modularapp.entitlement.organisation.repository.OrganisationRepository;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.mapper.RoleMapper;
import com.ganithyanthram.modularapp.entitlement.role.repository.RoleRepository;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for permission override management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionOverrideServiceImpl implements PermissionOverrideService {
    
    private final IndividualPermissionRepository individualPermissionRepository;
    private final IndividualRoleRepository individualRoleRepository;
    private final IndividualRepository individualRepository;
    private final RoleRepository roleRepository;
    private final OrganisationRepository organisationRepository;
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    
    @Override
    @Transactional
    public UUID overridePermissions(OverridePermissionRequest request, UUID userId) {
        log.info("Overriding permissions for individual {} in org {}", 
                request.getIndividualId(), request.getOrgId());
        
        // Validate individual exists
        individualRepository.findByIdAndIsActiveTrue(request.getIndividualId())
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + request.getIndividualId()));
        
        // Check if override already exists
        Optional<IndividualPermission> existingOverride = 
                individualPermissionRepository.findByIndividualIdAndOrgId(request.getIndividualId(), request.getOrgId());
        
        if (existingOverride.isPresent()) {
            // Update existing override
            IndividualPermission permission = existingOverride.get();
            permission.setPermissions(request.getPermissions());
            permission.setPages(request.getPages());
            permission.setRemarks(request.getRemarks());
            permission.setUpdatedBy(userId);
            permission.setUpdatedOn(LocalDateTime.now());
            individualPermissionRepository.update(permission);
            log.info("Permission override updated for individual: {}", request.getIndividualId());
            return permission.getId();
        } else {
            // Create new override
            IndividualPermission permission = new IndividualPermission();
            permission.setId(UUID.randomUUID());
            permission.setIndividualId(request.getIndividualId());
            permission.setOrgId(request.getOrgId());
            permission.setPermissions(request.getPermissions());
            permission.setPages(request.getPages());
            permission.setRemarks(request.getRemarks());
            permission.setCreatedBy(userId);
            permission.setUpdatedBy(userId);
            permission.setCreatedOn(LocalDateTime.now());
            permission.setUpdatedOn(LocalDateTime.now());
            
            UUID id = individualPermissionRepository.create(permission);
            log.info("Permission override created with ID: {}", id);
            return id;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleNode> getEffectivePermissions(UUID individualId, UUID orgId) {
        log.info("Calculating effective permissions for individual {} in org {}", individualId, orgId);
        
        // Get all roles assigned to individual in organisation
        List<IndividualRole> assignments = individualRoleRepository.findByIndividualIdAndOrgId(individualId, orgId);
        
        // Collect permissions from all roles
        List<RoleNode> rolePermissions = new ArrayList<>();
        for (IndividualRole assignment : assignments) {
            List<RoleNode> effectiveRolePerms = roleService.getEffectivePermissions(assignment.getRoleId());
            rolePermissions = mergePermissions(rolePermissions, effectiveRolePerms);
        }
        
        // Get individual permission overrides
        Optional<IndividualPermission> override = 
                individualPermissionRepository.findByIndividualIdAndOrgId(individualId, orgId);
        
        if (override.isPresent() && override.get().getPermissions() != null) {
            // Merge overrides with role permissions
            rolePermissions = mergePermissions(rolePermissions, override.get().getPermissions());
        }
        
        log.info("Effective permissions calculated for individual: {}", individualId);
        return rolePermissions;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserEntitlementResponse getUserEntitlements(UUID individualId) {
        log.info("Fetching complete entitlements for individual: {}", individualId);
        
        // Get individual
        Individual individual = individualRepository.findByIdAndIsActiveTrue(individualId)
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + individualId));
        
        // Get all role assignments grouped by organisation
        List<IndividualRole> allAssignments = individualRoleRepository.findByIndividualId(individualId);
        
        Map<UUID, List<IndividualRole>> assignmentsByOrg = allAssignments.stream()
                .collect(Collectors.groupingBy(IndividualRole::getOrgId));
        
        // Build organisation entitlements
        List<UserEntitlementResponse.OrganisationEntitlement> orgEntitlements = new ArrayList<>();
        
        for (Map.Entry<UUID, List<IndividualRole>> entry : assignmentsByOrg.entrySet()) {
            UUID orgId = entry.getKey();
            
            // Get organisation details
            Optional<Organisation> orgOpt = organisationRepository.findById(orgId);
            if (orgOpt.isEmpty()) {
                continue;
            }
            
            // Get roles
            List<RoleResponse> roles = entry.getValue().stream()
                    .map(assignment -> roleRepository.findById(assignment.getRoleId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(roleMapper::toResponse)
                    .collect(Collectors.toList());
            
            // Get effective permissions
            List<RoleNode> effectivePermissions = getEffectivePermissions(individualId, orgId);
            
            UserEntitlementResponse.OrganisationEntitlement orgEntitlement = 
                    UserEntitlementResponse.OrganisationEntitlement.builder()
                    .orgId(orgId)
                    .orgName(orgOpt.get().getName())
                    .roles(roles)
                    .effectivePermissions(effectivePermissions)
                    .accessibleResources(new ArrayList<>())
                    .build();
            
            orgEntitlements.add(orgEntitlement);
        }
        
        return UserEntitlementResponse.builder()
                .individualId(individualId)
                .name(individual.getName())
                .email(individual.getEmail())
                .organisations(orgEntitlements)
                .build();
    }
    
    @Override
    @Transactional
    public void deletePermissionOverride(UUID individualId, UUID orgId) {
        log.info("Deleting permission override for individual {} in org {}", individualId, orgId);
        
        individualPermissionRepository.deleteByIndividualIdAndOrgId(individualId, orgId);
        
        log.info("Permission override deleted successfully");
    }
    
    /**
     * Merge two permission lists (second list overrides first)
     */
    private List<RoleNode> mergePermissions(List<RoleNode> base, List<RoleNode> override) {
        Map<String, RoleNode> mergedMap = new HashMap<>();
        
        // Add base permissions first
        if (base != null) {
            for (RoleNode node : base) {
                mergedMap.put(node.getName(), node);
            }
        }
        
        // Override with new permissions
        if (override != null) {
            for (RoleNode node : override) {
                if (mergedMap.containsKey(node.getName())) {
                    // Merge children recursively
                    RoleNode existing = mergedMap.get(node.getName());
                    RoleNode merged = mergeRoleNodes(existing, node);
                    mergedMap.put(node.getName(), merged);
                } else {
                    mergedMap.put(node.getName(), node);
                }
            }
        }
        
        return new ArrayList<>(mergedMap.values());
    }
    
    /**
     * Merge two role nodes (override takes precedence)
     */
    private RoleNode mergeRoleNodes(RoleNode base, RoleNode override) {
        RoleNode merged = new RoleNode();
        merged.setName(override.getName());
        merged.setType(override.getType() != null ? override.getType() : base.getType());
        merged.setDisplayNumber(override.getDisplayNumber() != null ? override.getDisplayNumber() : base.getDisplayNumber());
        
        // Use bitwise OR to combine permissions
        Integer basePerms = base.getPermissions() != null ? base.getPermissions() : 0;
        Integer overridePerms = override.getPermissions() != null ? override.getPermissions() : 0;
        merged.setPermissions(basePerms | overridePerms);
        
        // Merge children recursively
        List<RoleNode> mergedChildren = mergePermissions(base.getChildren(), override.getChildren());
        merged.setChildren(mergedChildren);
        
        return merged;
    }
}
