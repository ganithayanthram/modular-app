package com.ganithyanthram.modularapp.entitlement.assignment.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.assignment.service.RoleAssignmentService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = EntitlementAssignmentController.class)
@DisplayName("EntitlementAssignmentController API Tests")
class EntitlementAssignmentControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private RoleAssignmentService roleAssignmentService;

    @MockitoBean
    private PermissionOverrideService permissionOverrideService;

    @Test
    @DisplayName("Should assign role to individual successfully")
    void shouldAssignRoleToIndividualSuccessfully() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setIndividualId(UUID.randomUUID());
        request.setRoleId(UUID.randomUUID());
        request.setOrgId(UUID.randomUUID());

        UUID expectedId = UUID.randomUUID();
        when(roleAssignmentService.assignRole(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/assignments/roles")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(roleAssignmentService).assignRole(any(), any());
    }

    @Test
    @DisplayName("Should revoke role from individual successfully")
    void shouldRevokeRoleFromIndividualSuccessfully() throws Exception {
        UUID individualId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/assignments/roles/{individualId}/{roleId}", 
                        individualId, roleId)
                        .with(csrf())
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        verify(roleAssignmentService).revokeRole(individualId, roleId);
    }

    @Test
    @DisplayName("Should get roles for individual")
    void shouldGetRolesForIndividual() throws Exception {
        UUID individualId = UUID.randomUUID();
        RoleResponse role = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("Admin Role")
                .orgId(UUID.randomUUID())
                .isActive(true)
                .build();

        when(roleAssignmentService.getRolesByIndividual(individualId))
                .thenReturn(List.of(role));

        mockMvc.perform(get("/api/v1/admin/assignments/individuals/{individualId}/roles", individualId)
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Admin Role"));

        verify(roleAssignmentService).getRolesByIndividual(individualId);
    }

    @Test
    @DisplayName("Should get roles for individual by organisation")
    void shouldGetRolesForIndividualByOrganisation() throws Exception {
        UUID individualId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        RoleResponse role = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("Org Admin Role")
                .orgId(orgId)
                .isActive(true)
                .build();

        when(roleAssignmentService.getRolesByIndividualAndOrganisation(individualId, orgId))
                .thenReturn(List.of(role));

        mockMvc.perform(get("/api/v1/admin/assignments/individuals/{individualId}/roles", individualId)
                        .param("orgId", orgId.toString())
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Org Admin Role"));

        verify(roleAssignmentService).getRolesByIndividualAndOrganisation(individualId, orgId);
    }

    @Test
    @DisplayName("Should get individuals with role")
    void shouldGetIndividualsWithRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        IndividualResponse individual = IndividualResponse.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@example.com")
                .isActive(true)
                .build();

        when(roleAssignmentService.getIndividualsByRole(roleId))
                .thenReturn(List.of(individual));

        mockMvc.perform(get("/api/v1/admin/assignments/roles/{roleId}/individuals", roleId)
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(roleAssignmentService).getIndividualsByRole(roleId);
    }

    @Test
    @DisplayName("Should override permissions for individual successfully")
    void shouldOverridePermissionsForIndividualSuccessfully() throws Exception {
        OverridePermissionRequest request = new OverridePermissionRequest();
        request.setIndividualId(UUID.randomUUID());
        request.setOrgId(UUID.randomUUID());
        request.setPermissions(List.of());

        UUID expectedId = UUID.randomUUID();
        when(permissionOverrideService.overridePermissions(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/assignments/permissions")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(permissionOverrideService).overridePermissions(any(), any());
    }

    @Test
    @DisplayName("Should get effective permissions for individual")
    void shouldGetEffectivePermissionsForIndividual() throws Exception {
        UUID individualId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        RoleNode permission = RoleNode.builder()
                .name("dashboard")
                .permissions(15)
                .build();

        when(permissionOverrideService.getEffectivePermissions(individualId, orgId))
                .thenReturn(List.of(permission));

        mockMvc.perform(get("/api/v1/admin/assignments/individuals/{individualId}/permissions", individualId)
                        .param("orgId", orgId.toString())
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("dashboard"))
                .andExpect(jsonPath("$[0].permissions").value(15));

        verify(permissionOverrideService).getEffectivePermissions(individualId, orgId);
    }
}
