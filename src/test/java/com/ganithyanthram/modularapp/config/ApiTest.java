package com.ganithyanthram.modularapp.config;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom meta-annotation that encapsulates all common @WebMvcTest configuration
 * for API test classes. This eliminates code duplication across controller test classes.
 * 
 * Usage:
 * <pre>
 * {@code
 * @ApiTest(controllers = YourController.class)
 * @DisplayName("YourController API Tests")
 * class YourControllerApiTest {
 * 
 *     @Autowired
 *     private MockMvc mockMvc;
 * 
 *     @MockBean
 *     private YourService yourService;
 * 
 *     @Test
 *     void shouldTestEndpoint() throws Exception {
 *         when(yourService.getData()).thenReturn("data");
 *         mockMvc.perform(get("/api/endpoint"))
 *                .andExpect(status().isOk());
 *     }
 * }
 * }
 * </pre>
 * 
 * This annotation automatically provides:
 * - Standard @WebMvcTest configuration with common exclusions
 * - Test profile activation
 * - API test tagging
 * - Exclusion of common infrastructure components (JOOQ, DataSource, Liquibase, etc.)
 * - Exclusion of GlobalExceptionHandler (to avoid loading service dependencies)
 * 
 * Benefits:
 * - Reduces boilerplate code in each test class
 * - Centralizes test configuration management
 * - Ensures consistency across all API test classes
 * - Simplifies maintenance and updates
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WebMvcTest(
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                com.ganithyanthram.modularapp.entitlement.common.exception.GlobalExceptionHandler.class
            }
        )
    }
)
@ActiveProfiles("test")
@Tag("api")
public @interface ApiTest {
    
    /**
     * The controller classes to test. This is the only parameter that needs
     * to be specified per test class.
     * 
     * Example:
     * <pre>
     * {@code
     * @ApiTest(controllers = OrganisationController.class)
     * class OrganisationControllerApiTest {
     *     // tests...
     * }
     * }
     * </pre>
     * 
     * @return array of controller classes to include in the test context
     */
    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};
}

