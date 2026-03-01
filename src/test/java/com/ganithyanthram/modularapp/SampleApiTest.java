package com.ganithyanthram.modularapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Sample API Test
 *
 * API tests should:
 * - Test the web layer in isolation using @WebMvcTest (not @SpringBootTest)
 * - Mock service dependencies with @MockitoBean (Spring Boot 4.x)
 * - Use MockMvc to make HTTP requests and assert responses
 * - NOT use a real database, Testcontainers, or PostgreSQLContainer
 * - Be tagged with @Tag("api")
 * - Follow naming convention: *ApiTest.java
 *
 * Example with a real controller:
 * <pre>
 * {@code
 * @WebMvcTest(controllers = SampleController.class)
 * class SampleControllerApiTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @MockitoBean
 *     private SampleService sampleService;
 *
 *     @Test
 *     void shouldReturnOkForValidRequest() throws Exception {
 *         when(sampleService.getData()).thenReturn("data");
 *         mockMvc.perform(get("/sample"))
 *                .andExpect(status().isOk())
 *                .andExpect(content().string("data"));
 *     }
 * }
 * }
 * </pre>
 */
@WebMvcTest
@ActiveProfiles("test")
@Tag("api")
@DisplayName("Sample API Tests")
class SampleApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should load web layer context")
    void shouldLoadWebLayerContext() {
        // Given & When & Then
        // @WebMvcTest loads only the web layer — no database, no Testcontainers required.
        assertNotNull(mockMvc, "MockMvc should be available in the web layer context");
    }

    @Test
    @DisplayName("Should demonstrate API test structure")
    void shouldDemonstrateApiTestStructure() {
        // Given - Setup test data and mock service responses (using @MockitoBean fields)
        // When  - Execute HTTP request via MockMvc: mockMvc.perform(get("/endpoint"))
        // Then  - Assert HTTP response: .andExpect(status().isOk())
        assertNotNull(mockMvc, "MockMvc is ready to test controller endpoints");
    }
}