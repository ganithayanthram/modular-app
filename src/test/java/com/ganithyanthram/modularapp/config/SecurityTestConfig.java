package com.ganithyanthram.modularapp.config;

import com.ganithyanthram.modularapp.security.config.JwtProperties;
import com.ganithyanthram.modularapp.security.jwt.JwtAuthenticationFilter;
import com.ganithyanthram.modularapp.security.jwt.JwtUtil;
import com.ganithyanthram.modularapp.security.resolver.CurrentUserArgumentResolver;
import com.ganithyanthram.modularapp.security.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Reusable test configuration for API tests with JWT security.
 * Provides all necessary beans for testing controllers with JWT authentication.
 * 
 * Usage:
 * <pre>
 * {@code
 * @ApiTest(controllers = YourController.class)
 * @Import(SecurityTestConfig.class)
 * class YourControllerApiTest {
 *     @Autowired
 *     private JwtTestUtil jwtTestUtil;
 *     
 *     @MockitoBean
 *     private CustomUserDetailsService customUserDetailsService;
 *     
 *     // ... tests
 * }
 * }
 * </pre>
 */
@TestConfiguration
public class SecurityTestConfig {
    
    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setSecret("TestSecretKeyForJWTTokenGeneration_MustBeAtLeast256BitsLong_ThisIsForTestingOnly");
        props.setAccessTokenExpirationMs(900000L);
        props.setRefreshTokenExpirationMs(604800000L);
        props.setIssuer("modular-app-test");
        props.setAudience("modular-app-test-users");
        return props;
    }

    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }

    @Bean
    public JwtTestUtil jwtTestUtil(JwtProperties jwtProperties) {
        return new JwtTestUtil(jwtProperties);
    }

    @Bean
    public CurrentUserArgumentResolver currentUserArgumentResolver(JwtUtil jwtUtil) {
        return new CurrentUserArgumentResolver(jwtUtil);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil, 
            CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + 
                            authException.getMessage() + "\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
