package com.ganithyanthram.modularapp.entitlement.organisation.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.config.JwtTestUtil;
import com.ganithyanthram.modularapp.config.SecurityTestConfig;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import com.ganithyanthram.modularapp.entitlement.organisation.service.OrganisationService;
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

@ApiTest(controllers = OrganisationController.class)
@Import(SecurityTestConfig.class)
@DisplayName("OrganisationController API Tests")
class OrganisationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @MockitoBean
    private OrganisationService organisationService;

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
        
        // Mock UserDetailsService to return a valid user when JWT is validated
        org.springframework.security.core.userdetails.User mockUser = 
            new org.springframework.security.core.userdetails.User(
                "test@example.com",
                "password",
                java.util.Collections.emptyList()
            );
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(mockUser);
    }

    @Test
    @DisplayName("Should create organisation successfully with valid JWT")
    void shouldCreateOrganisationSuccessfully() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();
        request.setName("Test Org");
        request.setCategory("Technology");

        UUID expectedId = UUID.randomUUID();
        when(organisationService.createOrganisation(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(organisationService).createOrganisation(any(), any());
    }

    @Test
    @DisplayName("Should reject request without JWT token")
    void shouldRejectRequestWithoutToken() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();
        request.setName("Test Org");
        request.setCategory("Technology");

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with invalid JWT token")
    void shouldRejectRequestWithInvalidToken() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();
        request.setName("Test Org");
        request.setCategory("Technology");

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with expired JWT token")
    void shouldRejectRequestWithExpiredToken() throws Exception {
        String expiredToken = jwtTestUtil.generateExpiredToken(testUserId, testOrgId, "test@example.com");
        
        CreateOrganisationRequest request = new CreateOrganisationRequest();
        request.setName("Test Org");
        request.setCategory("Technology");

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get organisation by ID with valid JWT")
    void shouldGetOrganisationById() throws Exception {
        UUID orgId = UUID.randomUUID();
        OrganisationResponse response = OrganisationResponse.builder()
                .id(orgId)
                .name("Test Org")
                .category("Technology")
                .isActive(true)
                .build();

        when(organisationService.getOrganisationById(orgId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/organisations/{id}", orgId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value("Test Org"));

        verify(organisationService).getOrganisationById(orgId);
    }

    @Test
    @DisplayName("Should list organisations with pagination and valid JWT")
    void shouldListOrganisationsWithPagination() throws Exception {
        OrganisationResponse response = OrganisationResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Org")
                .isActive(true)
                .build();

        when(organisationService.getAllOrganisations(0, 20, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/organisations")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Org"));

        verify(organisationService).getAllOrganisations(0, 20, null);
    }

    @Test
    @DisplayName("Should update organisation successfully with valid JWT")
    void shouldUpdateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();
        UpdateOrganisationRequest request = new UpdateOrganisationRequest();
        request.setName("Updated Org");
        request.setCategory("Updated Category");

        OrganisationResponse response = OrganisationResponse.builder()
                .id(orgId)
                .name("Updated Org")
                .category("Updated Category")
                .isActive(true)
                .build();

        when(organisationService.updateOrganisation(any(), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/organisations/{id}", orgId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Org"));

        verify(organisationService).updateOrganisation(any(), any(), any());
    }

    @Test
    @DisplayName("Should delete organisation successfully with valid JWT")
    void shouldDeleteOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/organisations/{id}", orgId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(organisationService).deleteOrganisation(orgId);
    }

    @Test
    @DisplayName("Should activate organisation successfully with valid JWT")
    void shouldActivateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/organisations/{id}/activate", orgId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        verify(organisationService).activateOrganisation(orgId);
    }

    @Test
    @DisplayName("Should deactivate organisation successfully with valid JWT")
    void shouldDeactivateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/organisations/{id}/deactivate", orgId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        verify(organisationService).deactivateOrganisation(orgId);
    }

    @Test
    @DisplayName("Should return 400 for invalid create request even with valid JWT")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
