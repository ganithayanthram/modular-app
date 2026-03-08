package com.ganithyanthram.modularapp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Modular App}")
    private String applicationName;

    @Value("${spring.application.version:1.0.0}")
    private String applicationVersion;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(applicationVersion)
                        .description("""
                                RESTful API for the Modular Application with Spring Modulith architecture.
                                
                                ## Features
                                - JWT-based authentication and authorization
                                - Role-based access control (RBAC)
                                - Permission management with hierarchical resources
                                - Organization and individual management
                                - Comprehensive entitlement system
                                
                                ## Authentication
                                Most endpoints require authentication. To authenticate:
                                1. Use the `/api/v1/auth/login` endpoint to obtain an access token
                                2. Click the "Authorize" button above and enter: `Bearer <your-token>`
                                3. All subsequent requests will include the authentication token
                                
                                ## Authorization
                                Different endpoints require different roles and permissions:
                                - **Admin endpoints** (`/api/v1/admin/**`): Require admin privileges
                                - **User endpoints** (`/api/v1/user/**`): Require authenticated user
                                - **Auth endpoints** (`/api/v1/auth/**`): Public access for login/register
                                """)
                        .contact(new Contact()
                                .name("Ganithyanthram Team")
                                .email("support@ganithyanthram.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.ganithyanthram.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/v1/auth/login")));
    }
}
