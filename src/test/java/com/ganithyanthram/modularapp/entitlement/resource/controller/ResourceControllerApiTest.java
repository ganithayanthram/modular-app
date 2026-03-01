package com.ganithyanthram.modularapp.entitlement.resource.controller;

import com.ganithyanthram.modularapp.config.ApiTest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.UpdateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
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

@ApiTest(controllers = ResourceController.class)
@DisplayName("ResourceController API Tests")
class ResourceControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private ResourceService resourceService;

    @Test
    @DisplayName("Should create resource successfully")
    void shouldCreateResourceSuccessfully() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest();
        request.setName("Dashboard");
        request.setType("PAGE");
        request.setDescription("Main dashboard");

        UUID expectedId = UUID.randomUUID();
        when(resourceService.createResource(any(), any())).thenReturn(expectedId);

        mockMvc.perform(post("/api/v1/admin/resources")
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(expectedId.toString()));

        verify(resourceService).createResource(any(), any());
    }

    @Test
    @DisplayName("Should get resource by ID")
    void shouldGetResourceById() throws Exception {
        UUID resourceId = UUID.randomUUID();
        ResourceResponse response = ResourceResponse.builder()
                .id(resourceId)
                .name("Dashboard")
                .type("PAGE")
                .description("Main dashboard")
                .isActive(true)
                .build();

        when(resourceService.getResourceById(resourceId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/resources/{id}", resourceId)
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resourceId.toString()))
                .andExpect(jsonPath("$.name").value("Dashboard"));

        verify(resourceService).getResourceById(resourceId);
    }

    @Test
    @DisplayName("Should get all resources with pagination")
    void shouldGetAllResourcesWithPagination() throws Exception {
        ResourceResponse resource1 = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Dashboard")
                .type("PAGE")
                .description("Main dashboard")
                .isActive(true)
                .build();

        when(resourceService.getAllResources(0, 20, null))
                .thenReturn(List.of(resource1));

        mockMvc.perform(get("/api/v1/admin/resources")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dashboard"));

        verify(resourceService).getAllResources(0, 20, null);
    }

    @Test
    @DisplayName("Should get resources by type")
    void shouldGetResourcesByType() throws Exception {
        ResourceResponse resource1 = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Dashboard")
                .type("PAGE")
                .description("Main dashboard")
                .isActive(true)
                .build();

        when(resourceService.getAllResources(0, 20, "PAGE"))
                .thenReturn(List.of(resource1));

        mockMvc.perform(get("/api/v1/admin/resources")
                        .param("page", "0")
                        .param("size", "20")
                        .param("type", "PAGE")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PAGE"));

        verify(resourceService).getAllResources(0, 20, "PAGE");
    }

    @Test
    @DisplayName("Should update resource successfully")
    void shouldUpdateResourceSuccessfully() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UpdateResourceRequest request = new UpdateResourceRequest();
        request.setName("Updated Dashboard");
        request.setDescription("Updated description");
        request.setType("PAGE");

        ResourceResponse response = ResourceResponse.builder()
                .id(resourceId)
                .name("Updated Dashboard")
                .description("Updated description")
                .type("PAGE")
                .isActive(true)
                .build();

        when(resourceService.updateResource(eq(resourceId), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/resources/{id}", resourceId)
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Dashboard"));

        verify(resourceService).updateResource(eq(resourceId), any(), any());
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void shouldDeleteResourceSuccessfully() throws Exception {
        UUID resourceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/resources/{id}", resourceId)
                        .with(csrf())
                        .with(user("admin")))
                .andExpect(status().isNoContent());

        verify(resourceService).deleteResource(resourceId);
    }

    @Test
    @DisplayName("Should get resource hierarchy")
    void shouldGetResourceHierarchy() throws Exception {
        ResourceResponse parent = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Dashboard")
                .type("PAGE")
                .description("Main dashboard")
                .isActive(true)
                .build();

        when(resourceService.getResourceHierarchy()).thenReturn(List.of(parent));

        mockMvc.perform(get("/api/v1/admin/resources/hierarchy")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dashboard"));

        verify(resourceService).getResourceHierarchy();
    }
}
