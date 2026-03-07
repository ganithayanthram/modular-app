# API Test Security Guide

## Overview

This guide explains how to write API tests (`@WebMvcTest`) with proper JWT security validation.

## Problem

When we added Spring Security with JWT authentication, the existing `@WebMvcTest` API tests failed because:
1. `@WebMvcTest` doesn't load the full security context by default
2. Security components like `JwtAuthenticationFilter`, `JwtUtil`, and `CurrentUserArgumentResolver` weren't available
3. Tests were using mocked security (`.with(user("admin"))`) instead of real JWT tokens

## Solution

We created a reusable `SecurityTestConfig` that provides all necessary security beans for API tests.

## How to Write API Tests with JWT Security

### 1. Import SecurityTestConfig

```java
@ApiTest(controllers = YourController.class)
@Import(SecurityTestConfig.class)
@DisplayName("YourController API Tests")
class YourControllerApiTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTestUtil jwtTestUtil;
    
    @MockitoBean
    private YourService yourService;
    
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    
    private String validToken;
    private UUID testUserId;
    private UUID testOrgId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testOrgId = UUID.randomUUID();
        validToken = jwtTestUtil.generateAccessToken(testUserId, testOrgId, "test@example.com");
        
        // Mock UserDetailsService to return a valid user when JWT is validated
        org.springframework.security.core.userdetails.User mockUser = 
            new org.springframework.security.core.userdetails.User(
                "test@example.com",
                "password",
                java.util.Collections.emptyList()
            );
        when(customUserDetailsService.loadUserByUsername("test@example.com")).thenReturn(mockUser);
    }
}
```

### 2. Test with Valid JWT Token

```java
@Test
@DisplayName("Should create resource successfully with valid JWT")
void shouldCreateResourceSuccessfully() throws Exception {
    CreateResourceRequest request = new CreateResourceRequest();
    request.setName("Test Resource");
    
    UUID expectedId = UUID.randomUUID();
    when(yourService.createResource(any(), any())).thenReturn(expectedId);
    
    mockMvc.perform(post("/api/v1/admin/resources")
                    .header("Authorization", "Bearer " + validToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(expectedId.toString()));
    
    verify(yourService).createResource(any(), any());
}
```

### 3. Test Security Rejection Scenarios

```java
@Test
@DisplayName("Should reject request without JWT token")
void shouldRejectRequestWithoutToken() throws Exception {
    CreateResourceRequest request = new CreateResourceRequest();
    request.setName("Test Resource");
    
    mockMvc.perform(post("/api/v1/admin/resources")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("Should reject request with invalid JWT token")
void shouldRejectRequestWithInvalidToken() throws Exception {
    CreateResourceRequest request = new CreateResourceRequest();
    request.setName("Test Resource");
    
    mockMvc.perform(post("/api/v1/admin/resources")
                    .header("Authorization", "Bearer invalid.token.here")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("Should reject request with expired JWT token")
void shouldRejectRequestWithExpiredToken() throws Exception {
    String expiredToken = jwtTestUtil.generateExpiredToken(testUserId, testOrgId, "test@example.com");
    
    CreateResourceRequest request = new CreateResourceRequest();
    request.setName("Test Resource");
    
    mockMvc.perform(post("/api/v1/admin/resources")
                    .header("Authorization", "Bearer " + expiredToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
}
```

## JwtTestUtil Methods

The `JwtTestUtil` provides several methods for generating test tokens:

```java
// Generate token with default user
String token = jwtTestUtil.generateAccessToken();

// Generate token with specific user details
String token = jwtTestUtil.generateAccessToken(userId, orgId, "user@example.com");

// Generate token with roles (for authorization testing)
String token = jwtTestUtil.generateAccessTokenWithRoles(userId, orgId, "user@example.com", "ADMIN", "USER");

// Generate expired token (for testing token expiration)
String expiredToken = jwtTestUtil.generateExpiredToken(userId, orgId, "user@example.com");
```

## What SecurityTestConfig Provides

The `SecurityTestConfig` class provides:

1. **JwtProperties** - JWT configuration for tests
2. **JwtUtil** - Token generation and validation
3. **JwtTestUtil** - Test-specific token generation utilities
4. **CurrentUserArgumentResolver** - Resolves `@CurrentUser` annotation in controllers
5. **JwtAuthenticationFilter** - Validates JWT tokens in requests
6. **SecurityFilterChain** - Complete security configuration with:
   - CSRF disabled
   - Stateless session management
   - JWT authentication filter
   - Custom authentication entry point (returns 401 instead of 403)

## Key Differences from Old Approach

### ❌ Old Approach (Mocked Security)
```java
mockMvc.perform(post("/api/v1/admin/resources")
        .with(csrf())
        .with(user("admin"))  // Mocked security
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
    .andExpect(status().isCreated());
```

### ✅ New Approach (Real JWT Security)
```java
mockMvc.perform(post("/api/v1/admin/resources")
        .header("Authorization", "Bearer " + validToken)  // Real JWT token
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonMapper.writeValueAsString(request)))
    .andExpect(status().isCreated());
```

## Benefits

1. **Real Security Testing** - Tests validate actual JWT authentication, not mocked security
2. **Comprehensive Coverage** - Can test valid tokens, invalid tokens, expired tokens, missing tokens
3. **Reusable Configuration** - `SecurityTestConfig` can be imported by any API test
4. **Consistent with Production** - Security behavior in tests matches production
5. **Easy to Use** - Simple pattern to follow for all API tests

## Example: Complete Test Class

See `OrganisationControllerApiTest` for a complete working example with:
- Valid JWT token tests
- Security rejection tests (no token, invalid token, expired token)
- Validation error tests
- Full CRUD operation tests

## Next Steps

To update an existing API test:

1. Add `@Import(SecurityTestConfig.class)` to the test class
2. Inject `JwtTestUtil` and `CustomUserDetailsService` (as `@MockitoBean`)
3. In `@BeforeEach`, generate a valid token and mock the UserDetailsService
4. Replace `.with(user("admin"))` with `.header("Authorization", "Bearer " + validToken)`
5. Remove `.with(csrf())` (not needed with stateless JWT)
6. Add security rejection tests (no token, invalid token, expired token)

## Troubleshooting

### Test gets 403 instead of 401
- Make sure `SecurityTestConfig` is imported
- Check that the custom authentication entry point is configured

### Test gets "No qualifying bean of type JwtUtil"
- Verify `@Import(SecurityTestConfig.class)` is present
- Check that `CustomUserDetailsService` is mocked with `@MockitoBean`

### JWT validation fails
- Ensure `CustomUserDetailsService.loadUserByUsername()` is mocked in `@BeforeEach`
- Verify the email in the token matches the mocked user

## Summary

This approach gives us **real security testing** in API tests while keeping them fast and focused on controller logic. The service layer is still mocked, but security validation is real.
