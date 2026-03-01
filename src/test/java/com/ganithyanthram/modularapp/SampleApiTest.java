package com.ganithyanthram.modularapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sample API Test - Documentation and Pattern Reference
 *
 * This is a documentation-only test class that demonstrates the correct pattern
 * for writing API tests. It does NOT load a Spring context to avoid dependency issues.
 *
 * API tests should:
 * - Test the web layer in isolation using @ApiTest (not @SpringBootTest)
 * - Mock service dependencies with @MockBean (Spring Boot 4.x)
 * - Use MockMvc to make HTTP requests and assert responses
 * - NOT use a real database, Testcontainers, or PostgreSQLContainer
 * - Be tagged with @Tag("api") (automatically applied by @ApiTest)
 * - Follow naming convention: *ApiTest.java
 *
 * IMPORTANT: Always specify the exact controller class in @ApiTest to avoid loading
 * all controllers and their dependencies.
 *
 * Example with a real controller:
 * <pre>
 * {@code
 * @ApiTest(controllers = OrganisationController.class)
 * @DisplayName("OrganisationController API Tests")
 * class OrganisationControllerApiTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @MockBean
 *     private OrganisationService organisationService;
 *
 *     @Test
 *     void shouldCreateOrganisationSuccessfully() throws Exception {
 *         // Given
 *         CreateOrganisationRequest request = new CreateOrganisationRequest();
 *         request.setName("Test Org");
 *         UUID expectedId = UUID.randomUUID();
 *
 *         when(organisationService.createOrganisation(any(), any()))
 *             .thenReturn(expectedId);
 *
 *         // When & Then
 *         mockMvc.perform(post("/api/v1/admin/organisations")
 *                 .contentType(MediaType.APPLICATION_JSON)
 *                 .content(objectMapper.writeValueAsString(request)))
 *             .andExpect(status().isCreated())
 *             .andExpect(jsonPath("$.id").value(expectedId.toString()));
 *
 *         verify(organisationService).createOrganisation(any(), any());
 *     }
 * }
 * }
 * </pre>
 *
 * See the @ApiTest annotation in com.ganithyanthram.modularapp.config.ApiTest for
 * the centralized configuration that handles:
 * - Excluding GlobalExceptionHandler (to avoid loading service dependencies)
 * - Setting test profile
 * - Adding @Tag("api")
 * - Excluding unnecessary auto-configurations
 */
@Tag("api")
@DisplayName("Sample API Tests - Pattern Documentation")
class SampleApiTest {

    @Test
    @DisplayName("Should demonstrate API test pattern")
    void shouldDemonstrateApiTestPattern() {
        // This is a documentation-only test
        // Real API tests should use @ApiTest(controllers = YourController.class)
        // and mock all service dependencies with @MockBean
        assertTrue(true, "See class-level JavaDoc for API test pattern examples");
    }
}