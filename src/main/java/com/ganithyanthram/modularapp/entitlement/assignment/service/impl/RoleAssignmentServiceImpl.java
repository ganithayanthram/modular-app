package com.ganithyanthram.modularapp.entitlement.assignment.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.IndividualRole;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Roles;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.RoleAssignmentResponse;
import com.ganithyanthram.modularapp.entitlement.assignment.repository.IndividualRoleRepository;
import com.ganithyanthram.modularapp.entitlement.assignment.service.RoleAssignmentService;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateAssignmentException;
import com.ganithyanthram.modularapp.entitlement.common.exception.IndividualNotFoundException;
import com.ganithyanthram.modularapp.entitlement.common.exception.RoleNotFoundException;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.mapper.IndividualMapper;
import com.ganithyanthram.modularapp.entitlement.individual.repository.IndividualRepository;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.mapper.RoleMapper;
import com.ganithyanthram.modularapp.entitlement.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for role assignment management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {
    
    private final IndividualRoleRepository individualRoleRepository;
    private final IndividualRepository individualRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final IndividualMapper individualMapper;
    
    @Override
    @Transactional
    public UUID assignRole(AssignRoleRequest request, UUID userId) {
        log.info("Assigning role {} to individual {} in org {}", 
                request.getRoleId(), request.getIndividualId(), request.getOrgId());
        
        // Validate individual exists
        individualRepository.findByIdAndIsActiveTrue(request.getIndividualId())
                .orElseThrow(() -> new IndividualNotFoundException("Individual not found with ID: " + request.getIndividualId()));
        
        // Validate role exists
        roleRepository.findByIdAndIsActiveTrue(request.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + request.getRoleId()));
        
        // Check if already assigned
        if (individualRoleRepository.existsByIndividualIdAndRoleId(request.getIndividualId(), request.getRoleId())) {
            throw new DuplicateAssignmentException("Role already assigned to individual");
        }
        
        // Create assignment
        IndividualRole assignment = new IndividualRole();
        assignment.setId(UUID.randomUUID());
        assignment.setIndividualId(request.getIndividualId());
        assignment.setRoleId(request.getRoleId());
        assignment.setOrgId(request.getOrgId());
        assignment.setCreatedBy(userId);
        assignment.setUpdatedBy(userId);
        assignment.setCreatedOn(LocalDateTime.now());
        assignment.setUpdatedOn(LocalDateTime.now());
        
        UUID id = individualRoleRepository.create(assignment);
        
        log.info("Role assigned successfully with ID: {}", id);
        return id;
    }
    
    @Override
    @Transactional
    public void revokeRole(UUID individualId, UUID roleId) {
        log.info("Revoking role {} from individual {}", roleId, individualId);
        
        // Verify assignment exists
        individualRoleRepository.findByIndividualIdAndRoleId(individualId, roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role assignment not found"));
        
        // Delete assignment
        individualRoleRepository.deleteByIndividualIdAndRoleId(individualId, roleId);
        
        log.info("Role revoked successfully");
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByIndividual(UUID individualId) {
        log.info("Fetching roles for individual: {}", individualId);
        
        List<IndividualRole> assignments = individualRoleRepository.findByIndividualId(individualId);
        
        return assignments.stream()
                .map(assignment -> roleRepository.findById(assignment.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByIndividualAndOrganisation(UUID individualId, UUID orgId) {
        log.info("Fetching roles for individual: {} in org: {}", individualId, orgId);
        
        List<IndividualRole> assignments = individualRoleRepository.findByIndividualIdAndOrgId(individualId, orgId);
        
        return assignments.stream()
                .map(assignment -> roleRepository.findById(assignment.getRoleId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<IndividualResponse> getIndividualsByRole(UUID roleId) {
        log.info("Fetching individuals with role: {}", roleId);
        
        List<IndividualRole> assignments = individualRoleRepository.findByRoleId(roleId);
        
        return assignments.stream()
                .map(assignment -> individualRepository.findById(assignment.getIndividualId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(individualMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoleAssignmentResponse getAssignment(UUID individualId, UUID roleId) {
        log.info("Fetching assignment for individual: {} and role: {}", individualId, roleId);
        
        IndividualRole assignment = individualRoleRepository.findByIndividualIdAndRoleId(individualId, roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role assignment not found"));
        
        return RoleAssignmentResponse.builder()
                .id(assignment.getId())
                .individualId(assignment.getIndividualId())
                .roleId(assignment.getRoleId())
                .orgId(assignment.getOrgId())
                .createdBy(assignment.getCreatedBy())
                .createdOn(assignment.getCreatedOn())
                .build();
    }
}
