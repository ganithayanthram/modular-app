package com.ganithyanthram.modularapp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sample Unit Test
 * 
 * Unit tests should:
 * - Test individual components in isolation
 * - Use mocks for dependencies
 * - Be fast and not require external resources
 * - Be tagged with @Tag("unit")
 * - Follow naming convention: *UTest.java
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Sample Unit Tests")
class SampleUTest {

    @Test
    @DisplayName("Should pass basic assertion test")
    void shouldPassBasicAssertionTest() {
        // Given
        String expected = "Hello, World!";
        
        // When
        String actual = "Hello, World!";
        
        // Then
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Should demonstrate unit test structure")
    void shouldDemonstrateUnitTestStructure() {
        // Given - Setup test data and mocks
        int a = 5;
        int b = 3;
        
        // When - Execute the method under test
        int result = a + b;
        
        // Then - Assert the results
        assertEquals(8, result);
        assertTrue(result > 0);
    }
}