package com.ganithyanthram.modularapp.entitlement.role.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.config.JwtTestUtil;
import com.ganithyanthram.modularapp.config.SecurityTestConfig;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import com.ganithyanthram.modularapp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = RoleController.class)
@Import(SecurityTestConfig.class)
@DisplayName("RoleController API Tests")
class RoleControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private String validToken;
    private UUID testUserId;
    private UUID testOrgId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testOrgId = UUID.randomUUID();
        validToken = jwtTestUtil.generateAccessToken(testUserId, testOrgId, "test@example.com");
        
        org.springframework.security.core.userdetails.User mockUser = 
            new org.springframework.security.core.userdetails.User(
                "test@example.com",
                "password",
                java.util.Collections.emptyList()
            );
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(mockUser);
    }

    @Test
    @DisplayName("Should create role successfully")
    void shouldCreateRoleSuccessfully() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setName("Test Role");
        request.setOrgId(UUID.randomUUID());

        UUID expectedId = UUID.randomUUID();
        when(roleService.createRole(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(roleService).createRole(any(), any());
    }

    @Test
    @DisplayName("Should get role by ID")
    void shouldGetRoleById() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleResponse response = RoleResponse.builder()
                .id(roleId)
                .name("Test Role")
                .isActive(true)
                .build();

        when(roleService.getRoleById(roleId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId.toString()))
                .andExpect(jsonPath("$.name").value("Test Role"));

        verify(roleService).getRoleById(roleId);
    }

    @Test
    @DisplayName("Should list roles with pagination")
    void shouldListRolesWithPagination() throws Exception {
        RoleResponse response = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Role")
                .isActive(true)
                .build();

        when(roleService.getAllRoles(0, 20)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/roles")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Role"));

        verify(roleService).getAllRoles(0, 20);
    }

    @Test
    @DisplayName("Should list roles by organisation")
    void shouldListRolesByOrganisation() throws Exception {
        UUID orgId = UUID.randomUUID();
        RoleResponse response = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("Org Role")
                .orgId(orgId)
                .isActive(true)
                .build();

        when(roleService.getRolesByOrganisation(orgId, 0, 20)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/roles")
                        .param("orgId", orgId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Org Role"));

        verify(roleService).getRolesByOrganisation(orgId, 0, 20);
    }

    @Test
    @DisplayName("Should update role successfully")
    void shouldUpdateRoleSuccessfully() throws Exception {
        UUID roleId = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Updated Role");

        RoleResponse response = RoleResponse.builder()
                .id(roleId)
                .name("Updated Role")
                .isActive(true)
                .build();

        when(roleService.updateRole(any(), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Role"));

        verify(roleService).updateRole(any(), any(), any());
    }

    @Test
    @DisplayName("Should delete role successfully")
    void shouldDeleteRoleSuccessfully() throws Exception {
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/roles/{id}", roleId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(roleService).deleteRole(roleId);
    }

    @Test
    @DisplayName("Should activate role successfully")
    void shouldActivateRoleSuccessfully() throws Exception {
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/roles/{id}/activate", roleId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        verify(roleService).activateRole(roleId);
    }

    @Test
    @DisplayName("Should get effective permissions for role")
    void shouldGetEffectivePermissionsForRole() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleNode permission = RoleNode.builder()
                .name("users")
                .permissions(15)
                .build();

        when(roleService.getEffectivePermissions(roleId)).thenReturn(List.of(permission));

        mockMvc.perform(get("/api/v1/admin/roles/{id}/effective-permissions", roleId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("users"))
                .andExpect(jsonPath("$[0].permissions").value(15));

        verify(roleService).getEffectivePermissions(roleId);
    }
}
