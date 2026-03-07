package com.ganithyanthram.modularapp.entitlement.user.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.config.JwtTestUtil;
import com.ganithyanthram.modularapp.config.SecurityTestConfig;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.UserEntitlementResponse;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
import com.ganithyanthram.modularapp.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = UserEntitlementController.class)
@Import(SecurityTestConfig.class)
@DisplayName("UserEntitlementController API Tests")
class UserEntitlementControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @MockitoBean
    private PermissionOverrideService permissionOverrideService;

    @MockitoBean
    private ResourceService resourceService;

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
    @DisplayName("Should get my entitlements")
    void shouldGetMyEntitlements() throws Exception {
        UserEntitlementResponse response = UserEntitlementResponse.builder()
                .individualId(UUID.randomUUID())
                .name("John Doe")
                .email("john@test.com")
                .organisations(new ArrayList<>())
                .build();

        when(permissionOverrideService.getUserEntitlements(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/user/entitlements")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));

        verify(permissionOverrideService).getUserEntitlements(any());
    }

    @Test
    @DisplayName("Should get my permissions for organisation")
    void shouldGetMyPermissionsForOrganisation() throws Exception {
        UUID orgId = UUID.randomUUID();
        RoleNode permission = RoleNode.builder()
                .name("dashboard")
                .permissions(15)
                .build();

        when(permissionOverrideService.getEffectivePermissions(any(), any()))
                .thenReturn(List.of(permission));

        mockMvc.perform(get("/api/v1/user/entitlements/permissions")
                        .param("orgId", orgId.toString())
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("dashboard"))
                .andExpect(jsonPath("$[0].permissions").value(15));

        verify(permissionOverrideService).getEffectivePermissions(any(), any());
    }

    @Test
    @DisplayName("Should get resource hierarchy")
    void shouldGetResourceHierarchy() throws Exception {
        ResourceResponse resource = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Dashboard")
                .type("menu")
                .children(new ArrayList<>())
                .build();

        when(resourceService.getResourceHierarchy()).thenReturn(List.of(resource));

        mockMvc.perform(get("/api/v1/user/entitlements/resources")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dashboard"));

        verify(resourceService).getResourceHierarchy();
    }

    @Test
    @DisplayName("Should check permission")
    void shouldCheckPermission() throws Exception {
        UUID orgId = UUID.randomUUID();
        RoleNode permission = RoleNode.builder()
                .name("users")
                .permissions(15)
                .build();

        when(permissionOverrideService.getEffectivePermissions(any(), any()))
                .thenReturn(List.of(permission));

        mockMvc.perform(get("/api/v1/user/entitlements/check")
                        .param("orgId", orgId.toString())
                        .param("resource", "users")
                        .param("action", "create")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(permissionOverrideService).getEffectivePermissions(any(), any());
    }
}
