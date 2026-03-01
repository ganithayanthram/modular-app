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
 * Sample Integration Test
 * 
 * Integration tests should:
 * - Test component interactions
 * - Use PostgreSQL TestContainer
 * - Test Spring context loading
 * - Be tagged with @Tag("integration")
 * - Follow naming convention: *ITest.java
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Sample Integration Tests")
class SampleITest {

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
    @DisplayName("Should load Spring context with PostgreSQL")
    void shouldLoadSpringContextWithPostgreSQL() {
        // This test verifies that the Spring context loads with PostgreSQL
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        assertNotNull(postgres.getJdbcUrl(), "JDBC URL should be available");
    }

    @Test
    @DisplayName("Should demonstrate integration test structure")
    void shouldDemonstrateIntegrationTestStructure() {
        // Given - Setup test data in PostgreSQL database
        // When - Execute business logic that involves multiple components
        // Then - Assert the integrated behavior
        
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running for integration tests");
    }
}