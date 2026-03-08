# Swagger/OpenAPI Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Modular App                              │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Spring Boot Application                │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │         SpringDoc OpenAPI                     │  │    │
│  │  │  - Auto-scans @RestController                 │  │    │
│  │  │  - Generates OpenAPI 3.0 spec                 │  │    │
│  │  │  - Serves Swagger UI                          │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │         OpenApiConfig                         │  │    │
│  │  │  - API metadata                               │  │    │
│  │  │  - Server definitions                         │  │    │
│  │  │  - JWT security scheme                        │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │         Controllers                           │  │    │
│  │  │  @Tag, @Operation, @ApiResponses              │  │    │
│  │  │  - AuthenticationController                   │  │    │
│  │  │  - IndividualController                       │  │    │
│  │  │  - OrganisationController                     │  │    │
│  │  │  - RoleController                             │  │    │
│  │  │  - ResourceController                         │  │    │
│  │  │  - UserEntitlementController                  │  │    │
│  │  │  - EntitlementAssignmentController            │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  │                                                      │    │
│  │  ┌──────────────────────────────────────────────┐  │    │
│  │  │         SecurityConfig                        │  │    │
│  │  │  - Permits /swagger-ui/**                     │  │    │
│  │  │  - Permits /v3/api-docs/**                    │  │    │
│  │  │  - JWT filter for other endpoints             │  │    │
│  │  └──────────────────────────────────────────────┘  │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Request Flow

### 1. Accessing Swagger UI

```
User Browser
     │
     │ GET /swagger-ui.html
     ├──────────────────────────────────────────┐
     │                                           │
     ▼                                           ▼
SecurityFilterChain                    Swagger UI (HTML/JS/CSS)
     │                                           │
     │ ✓ Public endpoint                        │
     │   (no auth required)                     │
     │                                           │
     └───────────────────────────────────────────┘
                          │
                          ▼
                  Swagger UI loads in browser
```

### 2. Loading API Specification

```
Swagger UI
     │
     │ GET /v3/api-docs
     ├──────────────────────────────────────────┐
     │                                           │
     ▼                                           ▼
SecurityFilterChain                    SpringDoc OpenAPI
     │                                           │
     │ ✓ Public endpoint                        │
     │   (no auth required)                     │
     │                                           │
     │                                           │ Scans controllers
     │                                           │ Reads annotations
     │                                           │ Generates OpenAPI JSON
     │                                           │
     └───────────────────────────────────────────┘
                          │
                          ▼
              OpenAPI 3.0 JSON specification
              (includes all endpoints, schemas, security)
```

### 3. Testing Authenticated Endpoint

```
User in Swagger UI
     │
     │ 1. POST /api/v1/auth/login
     ├──────────────────────────────────────────┐
     │                                           │
     ▼                                           ▼
SecurityFilterChain                    AuthenticationController
     │                                           │
     │ ✓ Public endpoint                        │
     │   (no auth required)                     │
     │                                           │
     │                                           │ Validate credentials
     │                                           │ Generate JWT token
     │                                           │
     └───────────────────────────────────────────┘
                          │
                          ▼
              Response: { "accessToken": "eyJ..." }
                          │
                          │ User copies token
                          │ Clicks "Authorize"
                          │ Enters: Bearer eyJ...
                          ▼
              Token stored in Swagger UI
                          │
                          │
     ┌────────────────────┴────────────────────┐
     │                                          │
     │ 2. GET /api/v1/admin/individuals         │
     │    Header: Authorization: Bearer eyJ...  │
     │                                          │
     ├──────────────────────────────────────────┤
     │                                          │
     ▼                                          ▼
SecurityFilterChain                    IndividualController
     │                                          │
     │ JwtAuthenticationFilter                 │
     │ ├─ Extract token                        │
     │ ├─ Validate signature                   │
     │ ├─ Check expiration                     │
     │ ├─ Load user details                    │
     │ └─ Set SecurityContext                  │
     │                                          │
     │ ✓ Authenticated                         │
     │                                          │
     │                                          │ @CurrentUser injected
     │                                          │ Business logic executes
     │                                          │
     └──────────────────────────────────────────┘
                          │
                          ▼
              Response: [ { "id": "...", ... } ]
```

## Component Interactions

```
┌─────────────────────────────────────────────────────────────┐
│                    Browser (Swagger UI)                      │
└─────────────────────────────────────────────────────────────┘
                          │
                          │ HTTP Requests
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Security Filter Chain                │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  1. Check if public endpoint                         │  │
│  │     - /swagger-ui/** → Allow                         │  │
│  │     - /v3/api-docs/** → Allow                        │  │
│  │     - /api/v1/auth/** → Allow                        │  │
│  │                                                       │  │
│  │  2. If not public:                                   │  │
│  │     - Extract JWT from Authorization header          │  │
│  │     - Validate token                                 │  │
│  │     - Set SecurityContext                            │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      Controllers                             │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  @Tag("Authentication")                              │  │
│  │  @SecurityRequirement(name = "bearerAuth")           │  │
│  │  @Operation(summary = "Login")                       │  │
│  │  @ApiResponses(...)                                  │  │
│  │                                                       │  │
│  │  public ResponseEntity<...> login(...) { ... }       │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   SpringDoc OpenAPI                          │
│                                                              │
│  - Scans @RestController classes                            │
│  - Reads Swagger annotations                                │
│  - Generates OpenAPI 3.0 specification                      │
│  - Serves at /v3/api-docs                                   │
│  - Serves Swagger UI at /swagger-ui.html                    │
└─────────────────────────────────────────────────────────────┘
```

## Security Integration

```
┌──────────────────────────────────────────────────────────────┐
│                     OpenApiConfig                             │
│                                                               │
│  SecurityScheme:                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  name: "bearerAuth"                                    │  │
│  │  type: HTTP                                            │  │
│  │  scheme: "bearer"                                      │  │
│  │  bearerFormat: "JWT"                                   │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                               │
│  Global Security Requirement:                                 │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  All endpoints require "bearerAuth"                    │  │
│  │  (except those marked as public)                       │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
                          │
                          │ Applied to
                          ▼
┌──────────────────────────────────────────────────────────────┐
│                      Controllers                              │
│                                                               │
│  Public Endpoints (no @SecurityRequirement):                  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  POST /api/v1/auth/login                               │  │
│  │  POST /api/v1/auth/refresh                             │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                               │
│  Protected Endpoints (@SecurityRequirement):                  │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  @SecurityRequirement(name = "bearerAuth")             │  │
│  │  GET /api/v1/admin/individuals                         │  │
│  │  POST /api/v1/admin/individuals                        │  │
│  │  ... (all other endpoints)                             │  │
│  └────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

## Annotation Hierarchy

```
Controller Level
├── @RestController
├── @RequestMapping("/api/v1/...")
├── @Tag(name = "...", description = "...")
├── @SecurityRequirement(name = "bearerAuth")  ← Applied to all methods
└── @PreAuthorize("isAuthenticated()")

Method Level
├── @GetMapping / @PostMapping / @PutMapping / @DeleteMapping
├── @Operation(summary = "...", description = "...")
├── @ApiResponses(value = {
│       @ApiResponse(responseCode = "200", ...),
│       @ApiResponse(responseCode = "400", ...),
│       @ApiResponse(responseCode = "401", ...)
│   })
└── Parameters
    ├── @Parameter(description = "...")
    ├── @Parameter(hidden = true)  ← For @CurrentUser
    └── @Valid @RequestBody
```

## Data Flow

```
1. Application Startup
   ├── Spring Boot initializes
   ├── SpringDoc scans @RestController classes
   ├── Reads Swagger annotations
   ├── Generates OpenAPI specification
   └── Registers endpoints:
       ├── /swagger-ui.html
       ├── /swagger-ui/**
       └── /v3/api-docs

2. User Accesses Swagger UI
   ├── Browser requests /swagger-ui.html
   ├── SecurityFilterChain permits (public endpoint)
   ├── Swagger UI HTML/JS/CSS served
   └── Swagger UI loads in browser

3. Swagger UI Loads API Spec
   ├── Swagger UI requests /v3/api-docs
   ├── SecurityFilterChain permits (public endpoint)
   ├── SpringDoc returns OpenAPI JSON
   └── Swagger UI renders API documentation

4. User Tests Endpoint
   ├── User clicks "Try it out"
   ├── Enters request parameters
   ├── Clicks "Execute"
   ├── Swagger UI sends HTTP request
   │   └── Includes Authorization header if authenticated
   ├── SecurityFilterChain processes request
   │   ├── Public endpoint? → Allow
   │   └── Protected endpoint? → Validate JWT
   ├── Controller handles request
   └── Response displayed in Swagger UI
```

## Benefits

### For Development
- ✅ Auto-generated documentation
- ✅ Always up-to-date with code
- ✅ Interactive testing
- ✅ No separate documentation maintenance

### For Testing
- ✅ Test authentication flows
- ✅ Try different request payloads
- ✅ See all possible responses
- ✅ Validate API behavior

### For Integration
- ✅ Download OpenAPI spec
- ✅ Generate client SDKs
- ✅ Import into Postman/Insomnia
- ✅ CI/CD integration

### For Teams
- ✅ Self-service API exploration
- ✅ Reduced onboarding time
- ✅ Clear API contracts
- ✅ Better collaboration

---

**Architecture Version**: 1.0  
**Last Updated**: March 2026
