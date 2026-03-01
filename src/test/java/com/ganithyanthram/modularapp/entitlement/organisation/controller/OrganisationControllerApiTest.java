package com.ganithyanthram.modularapp.entitlement.organisation.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import com.ganithyanthram.modularapp.entitlement.organisation.service.OrganisationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ApiTest(controllers = OrganisationController.class)
@DisplayName("OrganisationController API Tests")
class OrganisationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private OrganisationService organisationService;

    @Test
    @DisplayName("Should create organisation successfully")
    void shouldCreateOrganisationSuccessfully() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();
        request.setName("Test Org");
        request.setCategory("Technology");

        UUID expectedId = UUID.randomUUID();
        when(organisationService.createOrganisation(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(organisationService).createOrganisation(any(), any());
    }

    @Test
    @DisplayName("Should get organisation by ID")
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
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value("Test Org"));

        verify(organisationService).getOrganisationById(orgId);
    }

    @Test
    @DisplayName("Should list organisations with pagination")
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
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Org"));

        verify(organisationService).getAllOrganisations(0, 20, null);
    }

    @Test
    @DisplayName("Should update organisation successfully")
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
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Org"));

        verify(organisationService).updateOrganisation(any(), any(), any());
    }

    @Test
    @DisplayName("Should delete organisation successfully")
    void shouldDeleteOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/organisations/{id}", orgId)
                        .with(csrf())
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        verify(organisationService).deleteOrganisation(orgId);
    }

    @Test
    @DisplayName("Should activate organisation successfully")
    void shouldActivateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/organisations/{id}/activate", orgId)
                        .with(csrf())
                        .with(user("admin")))
                .andExpect(status().isOk());

        verify(organisationService).activateOrganisation(orgId);
    }

    @Test
    @DisplayName("Should deactivate organisation successfully")
    void shouldDeactivateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/organisations/{id}/deactivate", orgId)
                        .with(csrf())
                        .with(user("admin")))
                .andExpect(status().isOk());

        verify(organisationService).deactivateOrganisation(orgId);
    }

    @Test
    @DisplayName("Should return 400 for invalid create request")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        CreateOrganisationRequest request = new CreateOrganisationRequest();

        mockMvc.perform(post("/api/v1/admin/organisations")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
