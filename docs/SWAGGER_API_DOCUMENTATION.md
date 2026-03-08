# Swagger/OpenAPI Documentation

## Overview

The Modular App now includes comprehensive API documentation using **Swagger/OpenAPI 3.0** via SpringDoc OpenAPI.

## Accessing the Documentation

Once the application is running, you can access the API documentation at:

### Swagger UI (Interactive Documentation)
```
http://localhost:8080/swagger-ui.html
```

The Swagger UI provides:
- **Interactive API testing** - Try out API endpoints directly from the browser
- **Request/Response examples** - See sample payloads for each endpoint
- **Authentication testing** - Test JWT authentication flows
- **Schema definitions** - View all DTOs and their properties

### OpenAPI JSON Specification
```
http://localhost:8080/v3/api-docs
```

This endpoint returns the raw OpenAPI 3.0 specification in JSON format, useful for:
- Generating client SDKs
- Importing into API testing tools (Postman, Insomnia)
- CI/CD pipeline integration

### OpenAPI YAML Specification
```
http://localhost:8080/v3/api-docs.yaml
```

## Authentication in Swagger UI

Most API endpoints require JWT authentication. To test authenticated endpoints:

### Step 1: Login
1. Expand the **Authentication** section
2. Find the `POST /api/v1/auth/login` endpoint
3. Click **"Try it out"**
4. Enter valid credentials in the request body:
   ```json
   {
     "email": "user@example.com",
     "password": "your-password",
     "orgId": "your-org-uuid"
   }
   ```
5. Click **"Execute"**
6. Copy the `accessToken` from the response

### Step 2: Authorize
1. Click the **"Authorize"** button at the top of the page (lock icon)
2. In the dialog, enter: `Bearer <your-access-token>`
   - Example: `Bearer eyJhbGciOiJIUzUxMiJ9...`
3. Click **"Authorize"**
4. Click **"Close"**

### Step 3: Test Endpoints
All subsequent API calls will now include the JWT token automatically.

## API Organization

The API is organized into the following sections:

### 1. Authentication
- `POST /api/v1/auth/login` - Login with email/password
- `POST /api/v1/auth/refresh` - Refresh access token
- `POST /api/v1/auth/logout` - Logout (requires authentication)

### 2. Individual Management (Admin)
- `POST /api/v1/admin/individuals` - Create individual
- `GET /api/v1/admin/individuals` - List individuals
- `GET /api/v1/admin/individuals/{id}` - Get individual by ID
- `PUT /api/v1/admin/individuals/{id}` - Update individual
- `DELETE /api/v1/admin/individuals/{id}` - Delete individual
- `PATCH /api/v1/admin/individuals/{id}/activate` - Activate individual
- `PATCH /api/v1/admin/individuals/{id}/deactivate` - Deactivate individual

### 3. Organization Management (Admin)
- `POST /api/v1/admin/organisations` - Create organization
- `GET /api/v1/admin/organisations` - List organizations
- `GET /api/v1/admin/organisations/{id}` - Get organization by ID
- `PUT /api/v1/admin/organisations/{id}` - Update organization
- `DELETE /api/v1/admin/organisations/{id}` - Delete organization

### 4. Role Management (Admin)
- `POST /api/v1/admin/roles` - Create role
- `GET /api/v1/admin/roles` - List roles
- `GET /api/v1/admin/roles/{id}` - Get role by ID
- `PUT /api/v1/admin/roles/{id}` - Update role
- `DELETE /api/v1/admin/roles/{id}` - Delete role
- `GET /api/v1/admin/roles/{id}/permissions` - Get effective permissions

### 5. Resource Management (Admin)
- `POST /api/v1/admin/resources` - Create resource
- `GET /api/v1/admin/resources` - List resources
- `GET /api/v1/admin/resources/{id}` - Get resource by ID
- `PUT /api/v1/admin/resources/{id}` - Update resource
- `DELETE /api/v1/admin/resources/{id}` - Delete resource
- `GET /api/v1/admin/resources/hierarchy` - Get resource hierarchy

### 6. Entitlement Assignment (Admin)
- `POST /api/v1/admin/entitlements/roles/assign` - Assign role to individual
- `DELETE /api/v1/admin/entitlements/roles/revoke` - Revoke role from individual
- `GET /api/v1/admin/entitlements/individuals/{id}/roles` - Get individual's roles
- `GET /api/v1/admin/entitlements/roles/{id}/individuals` - Get individuals with role
- `POST /api/v1/admin/entitlements/permissions/override` - Override permissions
- `GET /api/v1/admin/entitlements/individuals/{id}/permissions` - Get effective permissions

### 7. User Entitlements
- `GET /api/v1/user/entitlements` - Get my entitlements
- `GET /api/v1/user/entitlements/permissions` - Get my permissions for organization
- `GET /api/v1/user/entitlements/resources` - Get resource hierarchy
- `GET /api/v1/user/entitlements/check` - Check specific permission

## Configuration

### Dependency
The Swagger/OpenAPI documentation is provided by SpringDoc OpenAPI:

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'
```

### Configuration Class
Location: `src/main/java/com/ganithyanthram/modularapp/config/OpenApiConfig.java`

Key configurations:
- **API Info**: Title, version, description, contact, license
- **Servers**: Local development and production URLs
- **Security Scheme**: JWT Bearer authentication
- **Global Security Requirement**: Applied to all endpoints (except public ones)

### Security Configuration
The Swagger UI endpoints are publicly accessible (no authentication required):
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

This is configured in `SecurityConfig.java`.

## Adding Documentation to New Controllers

### 1. Add Controller-Level Annotations

```java
@RestController
@RequestMapping("/api/v1/your-endpoint")
@Tag(name = "Your Feature", description = "Description of your feature")
@SecurityRequirement(name = "bearerAuth")  // If authentication required
public class YourController {
    // ...
}
```

### 2. Add Method-Level Annotations

```java
@Operation(
    summary = "Short summary",
    description = "Detailed description of what this endpoint does"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200",
        description = "Success description",
        content = @Content(schema = @Schema(implementation = YourResponse.class))
    ),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = @Content
    ),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content
    )
})
@GetMapping("/{id}")
public ResponseEntity<YourResponse> getById(
        @Parameter(description = "The ID") @PathVariable UUID id) {
    // ...
}
```

### 3. Hide Internal Parameters

Use `@Parameter(hidden = true)` for parameters that are injected by the framework (like `@CurrentUser`):

```java
@PostMapping
public ResponseEntity<YourResponse> create(
        @Parameter(hidden = true) @CurrentUser UUID userId,
        @Valid @RequestBody YourRequest request) {
    // ...
}
```

### 4. Document DTOs

Add annotations to your DTO classes:

```java
@Schema(description = "Request to create a new resource")
public class CreateResourceRequest {
    
    @Schema(description = "Resource name", example = "Dashboard", required = true)
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "Resource type", example = "menu", allowableValues = {"menu", "page", "action"})
    private String type;
    
    // ...
}
```

## Best Practices

1. **Be Descriptive**: Write clear, concise descriptions for all endpoints and parameters
2. **Include Examples**: Provide example values in `@Schema` annotations
3. **Document All Responses**: Include all possible HTTP status codes
4. **Use Proper HTTP Status Codes**: 
   - `200` for successful GET/PUT
   - `201` for successful POST (creation)
   - `204` for successful DELETE
   - `400` for validation errors
   - `401` for authentication failures
   - `403` for authorization failures
   - `404` for not found
5. **Group Related Endpoints**: Use `@Tag` to organize endpoints logically
6. **Hide Implementation Details**: Use `@Parameter(hidden = true)` for framework-injected parameters

## Testing with Swagger UI

### Example: Create an Individual

1. **Login** (get JWT token)
   - Navigate to **Authentication > POST /api/v1/auth/login**
   - Click "Try it out"
   - Enter credentials and execute
   - Copy the `accessToken`

2. **Authorize**
   - Click "Authorize" button
   - Enter: `Bearer <token>`
   - Click "Authorize" and "Close"

3. **Create Individual**
   - Navigate to **Individual Management > POST /api/v1/admin/individuals**
   - Click "Try it out"
   - Modify the request body:
     ```json
     {
       "name": "John Doe",
       "email": "john.doe@example.com",
       "password": "SecurePassword123!",
       "isActive": true
     }
     ```
   - Click "Execute"
   - View the response

## Customization

### Change Server URLs
Edit `OpenApiConfig.java`:

```java
.servers(List.of(
    new Server()
        .url("http://localhost:8080")
        .description("Local Development"),
    new Server()
        .url("https://your-production-url.com")
        .description("Production")
))
```

### Change API Info
Edit `OpenApiConfig.java`:

```java
.info(new Info()
    .title("Your API Title")
    .version("2.0.0")
    .description("Your API description")
    .contact(new Contact()
        .name("Your Team")
        .email("support@yourcompany.com"))
)
```

### Disable Swagger in Production
Add to `application-prod.properties`:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

## Troubleshooting

### Swagger UI Not Loading
- Ensure the application is running
- Check that security configuration allows access to `/swagger-ui/**`
- Clear browser cache

### Endpoints Not Showing
- Verify controller has `@RestController` annotation
- Check that controller is in a scanned package
- Ensure methods have proper HTTP method annotations (`@GetMapping`, etc.)

### Authentication Not Working
- Verify JWT token is valid and not expired
- Check that token is prefixed with `Bearer `
- Ensure security configuration is correct

## Additional Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
