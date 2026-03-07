package com.ganithyanthram.modularapp.entitlement.individual.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.config.JwtTestUtil;
import com.ganithyanthram.modularapp.config.SecurityTestConfig;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.service.IndividualService;
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

@ApiTest(controllers = IndividualController.class)
@Import(SecurityTestConfig.class)
@DisplayName("IndividualController API Tests")
class IndividualControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @MockitoBean
    private IndividualService individualService;

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
    @DisplayName("Should create individual successfully")
    void shouldCreateIndividualSuccessfully() throws Exception {
        CreateIndividualRequest request = new CreateIndividualRequest();
        request.setName("John Doe");
        request.setEmail("john@test.com");
        request.setPassword("SecurePass123!");

        UUID expectedId = UUID.randomUUID();
        when(individualService.createIndividual(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/individuals")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(individualService).createIndividual(any(), any());
    }

    @Test
    @DisplayName("Should get individual by ID")
    void shouldGetIndividualById() throws Exception {
        UUID individualId = UUID.randomUUID();
        IndividualResponse response = IndividualResponse.builder()
                .id(individualId)
                .name("John Doe")
                .email("john@test.com")
                .isActive(true)
                .build();

        when(individualService.getIndividualById(individualId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/individuals/{id}", individualId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(individualId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(individualService).getIndividualById(individualId);
    }

    @Test
    @DisplayName("Should list individuals with pagination")
    void shouldListIndividualsWithPagination() throws Exception {
        IndividualResponse response = IndividualResponse.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@test.com")
                .isActive(true)
                .build();

        when(individualService.getAllIndividuals(0, 20, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/individuals")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(individualService).getAllIndividuals(0, 20, null);
    }

    @Test
    @DisplayName("Should update individual successfully")
    void shouldUpdateIndividualSuccessfully() throws Exception {
        UUID individualId = UUID.randomUUID();
        UpdateIndividualRequest request = new UpdateIndividualRequest();
        request.setName("John Updated");
        request.setEmail("john@test.com");

        IndividualResponse response = IndividualResponse.builder()
                .id(individualId)
                .name("John Updated")
                .email("john@test.com")
                .isActive(true)
                .build();

        when(individualService.updateIndividual(any(), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/individuals/{id}", individualId)
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));

        verify(individualService).updateIndividual(any(), any(), any());
    }

    @Test
    @DisplayName("Should delete individual successfully")
    void shouldDeleteIndividualSuccessfully() throws Exception {
        UUID individualId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/individuals/{id}", individualId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());

        verify(individualService).deleteIndividual(individualId);
    }

    @Test
    @DisplayName("Should activate individual successfully")
    void shouldActivateIndividualSuccessfully() throws Exception {
        UUID individualId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/individuals/{id}/activate", individualId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        verify(individualService).activateIndividual(individualId);
    }

    @Test
    @DisplayName("Should deactivate individual successfully")
    void shouldDeactivateIndividualSuccessfully() throws Exception {
        UUID individualId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/individuals/{id}/deactivate", individualId)
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());

        verify(individualService).deactivateIndividual(individualId);
    }

    @Test
    @DisplayName("Should return 400 for invalid create request")
    void shouldReturn400ForInvalidCreateRequest() throws Exception {
        CreateIndividualRequest request = new CreateIndividualRequest();

        mockMvc.perform(post("/api/v1/admin/individuals")
                        .header("Authorization", "Bearer " + validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
