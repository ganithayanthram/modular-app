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

import java.time.OffsetDateTime;
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
        request.setName("Test Organisation");
        request.setCategory("Business");
        request.setStatus("ACTIVE");

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
                .name("Test Organisation")
                .category("Business")
                .isActive(true)
                .status("ACTIVE")
                .build();

        when(organisationService.getOrganisationById(orgId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/organisations/{id}", orgId)
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orgId.toString()))
                .andExpect(jsonPath("$.name").value("Test Organisation"));

        verify(organisationService).getOrganisationById(orgId);
    }

    @Test
    @DisplayName("Should get all organisations with pagination")
    void shouldGetAllOrganisationsWithPagination() throws Exception {
        OrganisationResponse org1 = OrganisationResponse.builder()
                .id(UUID.randomUUID())
                .name("Org 1")
                .category("Business")
                .isActive(true)
                .build();

        when(organisationService.getAllOrganisations(0, 20, null))
                .thenReturn(List.of(org1));

        mockMvc.perform(get("/api/v1/admin/organisations")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Org 1"));

        verify(organisationService).getAllOrganisations(0, 20, null);
    }

    @Test
    @DisplayName("Should update organisation successfully")
    void shouldUpdateOrganisationSuccessfully() throws Exception {
        UUID orgId = UUID.randomUUID();
        UpdateOrganisationRequest request = new UpdateOrganisationRequest();
        request.setName("Updated Organisation");
        request.setCategory("Enterprise");
        request.setStatus("ACTIVE");

        OrganisationResponse response = OrganisationResponse.builder()
                .id(orgId)
                .name("Updated Organisation")
                .category("Enterprise")
                .isActive(true)
                .status("ACTIVE")
                .build();

        when(organisationService.updateOrganisation(eq(orgId), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/organisations/{id}", orgId)
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Organisation"));

        verify(organisationService).updateOrganisation(eq(orgId), any(), any());
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
}
