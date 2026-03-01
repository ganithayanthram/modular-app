package com.ganithyanthram.modularapp.entitlement.role.service.impl;

import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Roles;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateRoleException;
import com.ganithyanthram.modularapp.entitlement.common.exception.RoleNotFoundException;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.mapper.RoleMapper;
import com.ganithyanthram.modularapp.entitlement.role.repository.RoleRepository;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Role management with hierarchical permission support
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    
    @Override
    @Transactional
    public UUID createRole(CreateRoleRequest request, UUID userId) {
        log.info("Creating role with name: {} for org: {}", request.getName(), request.getOrgId());
        
        // Validate unique name within organisation
        if (roleRepository.existsByNameAndOrgIdAndIsActiveTrue(request.getName(), request.getOrgId())) {
            throw new DuplicateRoleException("Role with name '" + request.getName() + 
                    "' already exists in organisation: " + request.getOrgId());
        }
        
        // Validate parent role exists if specified
        if (request.getParentRoleId() != null) {
            roleRepository.findByIdAndIsActiveTrue(request.getParentRoleId())
                    .orElseThrow(() -> new RoleNotFoundException("Parent role not found with ID: " + request.getParentRoleId()));
        }
        
        // Create entity
        Roles role = roleMapper.toEntity(request, userId);
        
        // Save to database
        UUID id = roleRepository.create(role);
        
        log.info("Role created successfully with ID: {}", id);
        return id;
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        log.info("Fetching role with ID: {}", id);
        
        Roles role = roleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));
        
        return roleMapper.toResponse(role);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByOrganisation(UUID orgId, int page, int size) {
        log.info("Fetching roles for organisation: {} - page: {}, size: {}", orgId, page, size);
        
        int offset = page * size;
        List<Roles> roles = roleRepository.findByOrgIdAndIsActiveTrue(orgId, offset, size);
        
        return roles.stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles(int page, int size) {
        log.info("Fetching all roles - page: {}, size: {}", page, size);
        
        int offset = page * size;
        List<Roles> roles = roleRepository.findByIsActiveTrue(offset, size);
        
        return roles.stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, UpdateRoleRequest request, UUID userId) {
        log.info("Updating role with ID: {}", id);
        
        // Fetch existing role
        Roles role = roleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));
        
        // Validate unique name within organisation (if name is being changed)
        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByNameAndOrgIdAndIsActiveTrue(request.getName(), role.getOrgId())) {
            throw new DuplicateRoleException("Role with name '" + request.getName() + 
                    "' already exists in organisation: " + role.getOrgId());
        }
        
        // Update entity
        roleMapper.updateEntity(role, request, userId);
        
        // Save to database
        roleRepository.update(role);
        
        log.info("Role updated successfully with ID: {}", id);
        return roleMapper.toResponse(role);
    }
    
    @Override
    @Transactional
    public void deleteRole(UUID id) {
        log.info("Deleting role with ID: {}", id);
        
        // Verify role exists
        roleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));
        
        // Soft delete
        roleRepository.softDelete(id);
        
        log.info("Role deleted successfully with ID: {}", id);
    }
    
    @Override
    @Transactional
    public void activateRole(UUID id) {
        log.info("Activating role with ID: {}", id);
        
        // Verify role exists
        roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + id));
        
        roleRepository.activate(id);
        
        log.info("Role activated successfully with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RoleNode> getEffectivePermissions(UUID roleId) {
        log.info("Calculating effective permissions for role: {}", roleId);
        
        Roles role = roleRepository.findByIdAndIsActiveTrue(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with ID: " + roleId));
        
        // Start with role's own permissions
        List<RoleNode> effectivePermissions = new ArrayList<>();
        if (role.getPermissions() != null) {
            effectivePermissions.addAll(role.getPermissions());
        }
        
        // If role has a parent, merge parent permissions
        if (role.getParentRoleId() != null) {
            List<RoleNode> parentPermissions = getEffectivePermissions(role.getParentRoleId());
            effectivePermissions = mergePermissions(parentPermissions, effectivePermissions);
        }
        
        log.info("Effective permissions calculated for role: {}", roleId);
        return effectivePermissions;
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countRolesByOrganisation(UUID orgId) {
        return roleRepository.countByOrgIdAndIsActiveTrue(orgId);
    }
    
    /**
     * Merge parent and child permissions.
     * Child permissions override parent permissions for the same resource.
     */
    private List<RoleNode> mergePermissions(List<RoleNode> parentPermissions, List<RoleNode> childPermissions) {
        Map<String, RoleNode> mergedMap = new HashMap<>();
        
        // Add parent permissions first
        if (parentPermissions != null) {
            for (RoleNode node : parentPermissions) {
                mergedMap.put(node.getName(), node);
            }
        }
        
        // Override with child permissions
        if (childPermissions != null) {
            for (RoleNode node : childPermissions) {
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
     * Merge two role nodes (child overrides parent)
     */
    private RoleNode mergeRoleNodes(RoleNode parent, RoleNode child) {
        RoleNode merged = new RoleNode();
        merged.setName(child.getName());
        merged.setType(child.getType() != null ? child.getType() : parent.getType());
        merged.setDisplayNumber(child.getDisplayNumber() != null ? child.getDisplayNumber() : parent.getDisplayNumber());
        
        // Use bitwise OR to combine permissions
        Integer parentPerms = parent.getPermissions() != null ? parent.getPermissions() : 0;
        Integer childPerms = child.getPermissions() != null ? child.getPermissions() : 0;
        merged.setPermissions(parentPerms | childPerms);
        
        // Merge children recursively
        List<RoleNode> mergedChildren = mergePermissions(parent.getChildren(), child.getChildren());
        merged.setChildren(mergedChildren);
        
        return merged;
    }
}
