# Security Module

## Overview
Spring Security implementation with JWT authentication for the modular-app. Provides stateless authentication, token-based authorization, and the @CurrentUser annotation for accessing the authenticated user context.

## Components

### Configuration
- **JwtProperties** - JWT configuration properties
- **SecurityConfig** - Spring Security configuration
- **WebMvcConfig** - Web MVC configuration for argument resolvers

### JWT
- **JwtUtil** - JWT token generation and validation
- **JwtAuthenticationFilter** - Filter for JWT authentication

### Services
- **CustomUserDetailsService** - Load user details from database
- **AuthenticationService** - Handle login, logout, token refresh
- **PermissionEvaluationService** - Runtime permission evaluation

### Controllers
- **AuthenticationController** - Authentication endpoints (login, refresh, logout)

### Annotations
- **@CurrentUser** - Inject current user ID from JWT token

### Resolvers
- **CurrentUserArgumentResolver** - Resolve @CurrentUser parameters

### DTOs
- **LoginRequest** - Login credentials
- **RefreshTokenRequest** - Refresh token request
- **AuthenticationResponse** - Login response with tokens
- **RefreshTokenResponse** - Refresh response with new access token

## Usage

### Login
```java
POST /api/v1/auth/login
{
  "email": "user@example.com",
  "password": "password",
  "orgId": "org-uuid"
}
```

### Use @CurrentUser
```java
@PostMapping
public ResponseEntity<?> create(
        @CurrentUser UUID userId,
        @RequestBody Request request) {
    // userId is automatically injected from JWT
}
```

### Protect Endpoints
```java
@RestController
@PreAuthorize("isAuthenticated()")
public class MyController {
    // All endpoints require authentication
}
```

## Documentation
See `docs/security/` for detailed documentation:
- `SECURITY_IMPLEMENTATION.md` - Complete implementation details
- `SECURITY_QUICK_REFERENCE.md` - Quick reference guide

## Testing
- **SecurityIntegrationTest** - 13 integration tests
- **AuthenticationControllerApiTest** - 6 API tests

Run tests:
```bash
./gradlew test --tests "*Security*"
```
