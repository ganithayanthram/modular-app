package com.ganithyanthram.modularapp.entitlement.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in the hierarchical permission tree structure.
 * Used in roles.permissions and individual_permission.permissions JSONB columns.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleNode {
    
    /**
     * Name of the permission node (e.g., "Dashboard", "Donors", "Create_Donor")
     */
    private String name;
    
    /**
     * Permission bits: 1=Read, 2=Write, 4=Update, 8=Delete, 15=All
     */
    private Integer permissions;
    
    /**
     * Display order in UI
     */
    private Integer displayNumber;
    
    /**
     * Type of permission node: "MENU", "PAGE", "ACTION", etc.
     */
    private String type;
    
    /**
     * Child permission nodes (hierarchical structure)
     */
    @Builder.Default
    private List<RoleNode> children = new ArrayList<>();
}
