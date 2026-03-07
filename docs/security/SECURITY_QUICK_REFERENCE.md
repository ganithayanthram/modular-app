# Security - Quick Reference Guide

## Authentication Flow

### 1. Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password",
  "orgId": "org-uuid"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "userId": "user-uuid",
  "email": "user@example.com",
  "name": "User Name"
}
```

### 2. Use Token
```bash
GET /api/v1/admin/organisations
Authorization: Bearer eyJhbGc...
```

### 3. Refresh Token
```bash
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGc..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

### 4. Logout
```bash
POST /api/v1/auth/logout
Authorization: Bearer eyJhbGc...
```

---

## Token Details

### Access Token
- **Lifetime**: 15 minutes
- **Purpose**: API authentication
- **Claims**: userId, orgId, email, type=access
- **Storage**: sessionStorage (client-side)

### Refresh Token
- **Lifetime**: 7 days
- **Purpose**: Get new access tokens
- **Claims**: userId, email, type=refresh
- **Storage**: httpOnly cookie or localStorage

---

## Using @CurrentUser

### In Controllers
```java
@PostMapping
public ResponseEntity<?> create(
        @CurrentUser UUID userId,  // Injected from JWT
        @RequestBody Request request) {
    service.create(request, userId);
}
```

### What It Does
1. Extracts JWT from Authorization header
2. Validates token signature and expiration
3. Extracts userId claim
4. Injects UUID into method parameter

---

## Authorization

### Class-Level Security
```java
@RestController
@PreAuthorize("isAuthenticated()")  // All methods require auth
public class MyController {
    // All endpoints require authentication
}
```

### Method-Level Security
```java
@PostMapping
@PreAuthorize("@permissionEvaluator.hasPermission(#userId, #orgId, 'resource', 'create')")
public ResponseEntity<?> create(@CurrentUser UUID userId, @RequestParam UUID orgId) {
    // Only users with 'create' permission on 'resource' can access
}
```

---

## Common Scenarios

### Scenario 1: User Login
```bash
# Step 1: Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "SecurePass123!",
    "orgId": "org-uuid"
  }'

# Step 2: Save tokens
# accessToken -> sessionStorage
# refreshToken -> localStorage or httpOnly cookie
```

### Scenario 2: Make API Request
```bash
# Use access token
curl -X GET http://localhost:8080/api/v1/admin/organisations \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Scenario 3: Token Expired
```bash
# Step 1: Try request (fails with 401)
curl -X GET http://localhost:8080/api/v1/admin/organisations \
  -H "Authorization: Bearer $EXPIRED_TOKEN"
# Response: 401 Unauthorized

# Step 2: Refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "$REFRESH_TOKEN"}'

# Step 3: Use new access token
curl -X GET http://localhost:8080/api/v1/admin/organisations \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN"
```

### Scenario 4: User Logout
```bash
# Step 1: Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"

# Step 2: Clear client-side tokens
# Remove from sessionStorage/localStorage
```

---

## Error Responses

### 401 Unauthorized
```json
{
  "timestamp": "2026-03-01T12:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

### 400 Bad Request (Validation)
```json
{
  "timestamp": "2026-03-01T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  },
  "path": "/api/v1/auth/login"
}
```

---

## Client Implementation Examples

### JavaScript/TypeScript
```typescript
// Login
async function login(email: string, password: string, orgId: string) {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, orgId })
  });
  
  const data = await response.json();
  
  // Store tokens
  sessionStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  
  return data;
}

// Make authenticated request
async function fetchOrganisations() {
  const token = sessionStorage.getItem('accessToken');
  
  const response = await fetch('/api/v1/admin/organisations', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.status === 401) {
    // Token expired, refresh it
    await refreshToken();
    return fetchOrganisations(); // Retry
  }
  
  return response.json();
}

// Refresh token
async function refreshToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch('/api/v1/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });
  
  const data = await response.json();
  sessionStorage.setItem('accessToken', data.accessToken);
  
  return data;
}

// Logout
async function logout() {
  const token = sessionStorage.getItem('accessToken');
  
  await fetch('/api/v1/auth/logout', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  // Clear tokens
  sessionStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
}
```

### Java (RestTemplate)
```java
// Login
RestTemplate restTemplate = new RestTemplate();
LoginRequest request = new LoginRequest("user@example.com", "password", orgId);

ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
    "http://localhost:8080/api/v1/auth/login",
    request,
    AuthenticationResponse.class
);

String accessToken = response.getBody().getAccessToken();

// Make authenticated request
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + accessToken);
HttpEntity<String> entity = new HttpEntity<>(headers);

ResponseEntity<String> orgsResponse = restTemplate.exchange(
    "http://localhost:8080/api/v1/admin/organisations",
    HttpMethod.GET,
    entity,
    String.class
);
```

---

## Configuration Reference

### Required Properties
```properties
jwt.secret=<256-bit-secret-key>
jwt.access-token-expiration-ms=900000
jwt.refresh-token-expiration-ms=604800000
jwt.issuer=modular-app
jwt.audience=modular-app-users
```

### Optional Properties
```properties
# Logging
logging.level.com.ganithyanthram.modularapp.security=DEBUG

# CORS (if needed)
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH
spring.web.cors.allowed-headers=Authorization,Content-Type
```

---

## Quick Commands

### Generate Strong Secret Key
```bash
# Generate 256-bit random key (base64)
openssl rand -base64 32

# Generate 512-bit random key (base64)
openssl rand -base64 64
```

### Test Authentication
```bash
# Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"pass","orgId":"uuid"}' \
  | jq -r '.accessToken')

# Test protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/admin/organisations

# Decode JWT (without verification)
echo $TOKEN | cut -d. -f2 | base64 -d | jq
```

---

**Quick Reference Version**: 1.0.0  
**Last Updated**: March 1, 2026
