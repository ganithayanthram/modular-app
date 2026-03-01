package com.ganithyanthram.modularapp;

import com.ganithyanthram.modularapp.config.DockerEnvironmentDetector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample API Test
 * 
 * API tests should:
 * - Test application context loading
 * - Test with PostgreSQL TestContainer
 * - Be tagged with @Tag("api")
 * - Follow naming convention: *ApiTest.java
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Tag("api")
@DisplayName("Sample API Tests")
class SampleApiTest {

    static {
        // Configure TestContainers BEFORE container initialization
        DockerEnvironmentDetector.configureForMultipass();
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

    @Test
    @DisplayName("Should load Spring context with PostgreSQL database")
    void shouldLoadSpringContextWithPostgreSQLDatabase() {
        // Given & When & Then
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        assertNotNull(postgres.getJdbcUrl(), "Database URL should be available");
    }

    @Test
    @DisplayName("Should demonstrate API test structure")
    void shouldDemonstrateApiTestStructure() {
        // Given - Setup test data and prepare request
        // When - Execute API calls or business logic
        // Then - Assert the results
        
        assertTrue(postgres.isRunning(), "PostgreSQL should be available for API tests");
    }
}