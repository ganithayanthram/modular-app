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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
        request.setName("Test Resource");
        request.setType("menu");

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
                .name("Test Resource")
                .type("menu")
                .isActive(true)
                .children(new ArrayList<>())
                .build();

        when(resourceService.getResourceById(resourceId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/resources/{id}", resourceId)
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resourceId.toString()))
                .andExpect(jsonPath("$.name").value("Test Resource"));

        verify(resourceService).getResourceById(resourceId);
    }

    @Test
    @DisplayName("Should list resources with pagination")
    void shouldListResourcesWithPagination() throws Exception {
        ResourceResponse response = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Test Resource")
                .type("menu")
                .isActive(true)
                .children(new ArrayList<>())
                .build();

        when(resourceService.getAllResources(0, 20, null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/admin/resources")
                        .param("page", "0")
                        .param("size", "20")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Resource"));

        verify(resourceService).getAllResources(0, 20, null);
    }

    @Test
    @DisplayName("Should get resource hierarchy")
    void shouldGetResourceHierarchy() throws Exception {
        ResourceResponse root = ResourceResponse.builder()
                .id(UUID.randomUUID())
                .name("Dashboard")
                .type("menu")
                .children(new ArrayList<>())
                .build();

        when(resourceService.getResourceHierarchy()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/v1/admin/resources/hierarchy")
                        .with(user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dashboard"));

        verify(resourceService).getResourceHierarchy();
    }

    @Test
    @DisplayName("Should update resource successfully")
    void shouldUpdateResourceSuccessfully() throws Exception {
        UUID resourceId = UUID.randomUUID();
        UpdateResourceRequest request = new UpdateResourceRequest();
        request.setName("Updated Resource");
        request.setType("menu");

        ResourceResponse response = ResourceResponse.builder()
                .id(resourceId)
                .name("Updated Resource")
                .type("menu")
                .isActive(true)
                .children(new ArrayList<>())
                .build();

        when(resourceService.updateResource(any(), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/resources/{id}", resourceId)
                        .with(csrf())
                        .with(user("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Resource"));

        verify(resourceService).updateResource(any(), any(), any());
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
}
