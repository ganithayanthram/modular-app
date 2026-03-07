package com.ganithyanthram.modularapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganithyanthram.modularapp.ModularAppApplication;
import com.ganithyanthram.modularapp.config.DockerEnvironmentDetector;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Individual;
import com.ganithyanthram.modularapp.db.jooq.tables.pojos.Organisation;
import com.ganithyanthram.modularapp.entitlement.individual.repository.IndividualRepository;
import com.ganithyanthram.modularapp.entitlement.organisation.repository.OrganisationRepository;
import com.ganithyanthram.modularapp.security.dto.AuthenticationResponse;
import com.ganithyanthram.modularapp.security.dto.LoginRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenRequest;
import com.ganithyanthram.modularapp.security.dto.RefreshTokenResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for security and authentication functionality.
 * Tests JWT authentication, authorization, and @CurrentUser annotation.
 */
@SpringBootTest(classes = ModularAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
class SecurityITest {
    
    static {
        DockerEnvironmentDetector.configureFromProfile("test");
    }
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jooq.sql-dialect", () -> "POSTGRES");
    }
    
    @Autowired
    private WebApplicationContext context;
    
    private MockMvc mockMvc;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private OrganisationRepository organisationRepository;
    
    @Autowired
    private IndividualRepository individualRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static UUID testOrgId;
    private static UUID testUserId;
    private static String testUserEmail;
    private static String testUserPassword;
    private static String accessToken;
    private static String refreshToken;
    
    @BeforeAll
    static void verifyContainer() {
        assertThat(postgres.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();
    }
    
    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders
                    .webAppContextSetup(context)
                    .apply(springSecurity())
                    .build();
        }
        
        if (testOrgId == null) {
            setupTestData();
        }
    }
    
    private void setupTestData() {
        Organisation org = new Organisation();
        org.setId(UUID.randomUUID());
        org.setName("Test Organisation");
        org.setCategory("Test");
        org.setIsActive(true);
        org.setCreatedBy(UUID.randomUUID());
        org.setCreatedOn(LocalDateTime.now());
        testOrgId = organisationRepository.create(org);
        
        testUserEmail = "security.test@example.com";
        testUserPassword = "SecurePassword123!";
        
        Individual individual = new Individual();
        individual.setId(UUID.randomUUID());
        individual.setName("Security Test User");
        individual.setEmail(testUserEmail);
        individual.setPassword(passwordEncoder.encode(testUserPassword));
        individual.setIsActive(true);
        individual.setCreatedBy(UUID.randomUUID());
        individual.setCreatedOn(LocalDateTime.now());
        testUserId = individualRepository.create(individual);
    }
    
    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest(testUserEmail, testUserPassword, testOrgId);
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value(testUserEmail))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(responseBody, AuthenticationResponse.class);
        
        accessToken = response.getAccessToken();
        refreshToken = response.getRefreshToken();
        
        assertThat(accessToken).isNotNull().isNotEmpty();
        assertThat(refreshToken).isNotNull().isNotEmpty();
    }
    
    @Test
    @Order(2)
    @DisplayName("Should fail login with invalid credentials")
    void testLoginFailure() throws Exception {
        LoginRequest request = new LoginRequest(testUserEmail, "WrongPassword", testOrgId);
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(3)
    @DisplayName("Should fail login with non-existent user")
    void testLoginNonExistentUser() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password", testOrgId);
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(4)
    @DisplayName("Should access protected endpoint with valid JWT token")
    void testAccessProtectedEndpointWithToken() throws Exception {
        if (accessToken == null) {
            testLoginSuccess();
        }
        
        mockMvc.perform(get("/api/v1/user/entitlements")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }
    
    @Test
    @Order(5)
    @DisplayName("Should deny access to protected endpoint without token")
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/entitlements"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(6)
    @DisplayName("Should deny access with invalid token")
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/user/entitlements")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(7)
    @DisplayName("Should refresh access token with valid refresh token")
    void testRefreshToken() throws Exception {
        if (refreshToken == null) {
            testLoginSuccess();
        }
        
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        RefreshTokenResponse response = objectMapper.readValue(responseBody, RefreshTokenResponse.class);
        
        assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        assertThat(response.getAccessToken()).isNotEqualTo(accessToken);
    }
    
    @Test
    @Order(8)
    @DisplayName("Should fail refresh with invalid refresh token")
    void testRefreshTokenFailure() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid.refresh.token");
        
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(9)
    @DisplayName("Should logout successfully")
    void testLogout() throws Exception {
        if (accessToken == null) {
            testLoginSuccess();
        }
        
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
    
    @Test
    @Order(10)
    @DisplayName("Should inject current user ID with @CurrentUser annotation")
    void testCurrentUserAnnotation() throws Exception {
        if (accessToken == null) {
            testLoginSuccess();
        }
        
        Organisation newOrg = new Organisation();
        newOrg.setName("Test Org for CurrentUser");
        newOrg.setCategory("Test");
        
        mockMvc.perform(post("/api/v1/admin/organisations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
    
    @Test
    @Order(11)
    @DisplayName("Should enforce authentication on admin endpoints")
    void testAdminEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/admin/organisations"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(12)
    @DisplayName("Should enforce authentication on user endpoints")
    void testUserEndpointRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/user/entitlements"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(13)
    @DisplayName("Should allow access to public auth endpoints")
    void testPublicAuthEndpoints() throws Exception {
        LoginRequest request = new LoginRequest(testUserEmail, testUserPassword, testOrgId);
        
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
