# Entitlement Management Module

A comprehensive Role-Based Access Control (RBAC) system with hierarchical permissions, multi-tenancy support, and flexible permission overrides.

## Overview

This module provides enterprise-grade entitlement management capabilities including:
- Organisation and individual management
- Hierarchical role-based permissions
- Resource management with tree structure
- Role assignments and permission overrides
- Effective permission calculation
- Multi-tenancy support

## Architecture

### Module Structure
```
entitlement/
├── common/              # Shared components
│   ├── dto/            # Common DTOs (RoleNode)
│   ├── converter/      # JOOQ JSONB converters
│   └── exception/      # Custom exceptions & global handler
├── organisation/       # Organisation management
├── individual/         # Individual (user) management
├── role/              # Role management with hierarchy
├── resource/          # Resource management with tree structure
├── assignment/        # Role assignments & permission overrides
└── user/              # User-facing entitlement APIs
```

### Technology Stack
- **Spring Boot 4.0.3** - Application framework
- **Spring Modulith** - Modular monolith architecture
- **JOOQ** - Type-safe SQL with code generation
- **Liquibase** - Database migration management
- **PostgreSQL** - Relational database with JSONB support
- **Jakarta Validation** - Request validation
- **Lombok** - Boilerplate reduction
- **BCrypt** - Password hashing

## Database Schema

### Core Tables (6)
1. **organisation** - Organisation/tenant management
2. **individual** - User accounts with authentication
3. **roles** - Role definitions with hierarchical permissions
4. **resource** - Resource tree for navigation/menu
5. **individual_role** - Role assignments (many-to-many)
6. **individual_permission** - Permission overrides per individual

### Audit Tables (3)
7. **individual_password_reset_audit** - Password reset tracking
8. **individual_verification_audit** - Email verification tracking
9. **individual_sessions** - Session management

### Supporting Tables (2)
10. **list_names** - List definitions
11. **list_values** - List value entries

### Key Features
- **JSONB Columns**: Flexible schema for permissions, pages, metadata, validations
- **Soft Deletes**: All entities support soft delete with `is_active` flag
- **Audit Trail**: `created_by`, `updated_by`, `created_on`, `updated_on` on all entities
- **UUID Primary Keys**: Distributed-system friendly
- **Foreign Keys**: Referential integrity enforcement
- **Indexes**: Performance optimization on frequently queried columns

## API Endpoints

### Admin APIs (`/api/v1/admin`)

#### Organisation Management (7 endpoints)
```
POST   /organisations              Create organisation
GET    /organisations/{id}         Get by ID
GET    /organisations              List with pagination & search
PUT    /organisations/{id}         Update
DELETE /organisations/{id}         Soft delete
PATCH  /organisations/{id}/activate    Activate
PATCH  /organisations/{id}/deactivate  Deactivate
```

#### Individual Management (7 endpoints)
```
POST   /individuals                Create individual
GET    /individuals/{id}           Get by ID
GET    /individuals                List with pagination & search
PUT    /individuals/{id}           Update
DELETE /individuals/{id}           Soft delete
PATCH  /individuals/{id}/activate      Activate
PATCH  /individuals/{id}/deactivate    Deactivate
```

#### Role Management (7 endpoints)
```
POST   /roles                      Create role
GET    /roles/{id}                 Get by ID
GET    /roles                      List with pagination & org filter
PUT    /roles/{id}                 Update
DELETE /roles/{id}                 Soft delete
PATCH  /roles/{id}/activate        Activate
GET    /roles/{id}/effective-permissions  Get effective permissions
```

#### Resource Management (6 endpoints)
```
POST   /resources                  Create resource
GET    /resources/{id}             Get by ID
GET    /resources                  List with pagination & type filter
PUT    /resources/{id}             Update
DELETE /resources/{id}             Soft delete
GET    /resources/hierarchy        Get resource tree
```

#### Assignment Management (6 endpoints)
```
POST   /assignments/roles          Assign role to individual
DELETE /assignments/roles/{individualId}/{roleId}  Revoke role
GET    /assignments/individuals/{individualId}/roles  Get roles for individual
GET    /assignments/roles/{roleId}/individuals  Get individuals with role
POST   /assignments/permissions    Override permissions
GET    /assignments/individuals/{individualId}/permissions  Get effective permissions
```

### User APIs (`/api/v1/user`)

#### Entitlement Access (4 endpoints)
```
GET /entitlements                   Get my complete entitlements
GET /entitlements/permissions       Get my permissions for org
GET /entitlements/resources         Get resource hierarchy
GET /entitlements/check             Check specific permission
```

## Core Concepts

### 1. Hierarchical Permissions

Roles can inherit from parent roles, with permissions being merged:

```java
// Parent Role: "Manager"
permissions: [
  { name: "users", permissions: 15 }  // CRUD (1111 in binary)
]

// Child Role: "Team Lead" (inherits from Manager)
permissions: [
  { name: "reports", permissions: 1 }  // Read only (0001 in binary)
]

// Effective permissions for Team Lead:
// - users: 15 (CRUD) - inherited from Manager
// - reports: 1 (Read) - own permission
```

### 2. Permission Bits

Permissions use bitwise operations for efficient storage:
- **Read**: 1 (0001)
- **Create**: 2 (0010)
- **Update**: 4 (0100)
- **Delete**: 8 (1000)
- **CRUD**: 15 (1111)

### 3. Permission Overrides

Individual permissions override role permissions:

```java
// Individual has "Viewer" role with Read-only (1)
// But needs Create access for specific resource
// Override: { name: "reports", permissions: 3 }  // Read + Create

// Effective permission: 3 (Read + Create)
```

### 4. Multi-Tenancy

Individuals can have different roles in different organisations:

```java
// John in Org A: "Admin" role
// John in Org B: "Viewer" role
// Permissions are calculated per organisation
```

### 5. Resource Tree

Resources form a hierarchical tree for navigation:

```json
{
  "name": "Dashboard",
  "children": [
    {
      "name": "Analytics",
      "children": [
        { "name": "Sales Reports" },
        { "name": "User Reports" }
      ]
    },
    {
      "name": "Settings",
      "children": [
        { "name": "User Management" },
        { "name": "Role Management" }
      ]
    }
  ]
}
```

## Usage Examples

### 1. Create Organisation
```bash
POST /api/v1/admin/organisations
Content-Type: application/json

{
  "name": "Acme Corp",
  "description": "Main organisation",
  "metaData": {
    "industry": "Technology",
    "size": "Enterprise"
  }
}
```

### 2. Create Individual
```bash
POST /api/v1/admin/individuals
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@acme.com",
  "password": "SecurePass123!",
  "orgId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 3. Create Role with Permissions
```bash
POST /api/v1/admin/roles
Content-Type: application/json

{
  "name": "Content Manager",
  "description": "Can manage content",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "permissions": [
    {
      "name": "articles",
      "type": "resource",
      "permissions": 15,  // CRUD
      "children": [
        {
          "name": "publish",
          "type": "action",
          "permissions": 2  // Create (publish action)
        }
      ]
    }
  ]
}
```

### 4. Assign Role to Individual
```bash
POST /api/v1/admin/assignments/roles
Content-Type: application/json

{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "roleId": "770e8400-e29b-41d4-a716-446655440002",
  "orgId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 5. Override Individual Permissions
```bash
POST /api/v1/admin/assignments/permissions
Content-Type: application/json

{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "permissions": [
    {
      "name": "reports",
      "type": "resource",
      "permissions": 3  // Read + Create
    }
  ],
  "remarks": "Temporary access for Q1 reporting"
}
```

### 6. Get User Entitlements
```bash
GET /api/v1/user/entitlements
```

Response:
```json
{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "John Doe",
  "email": "john@acme.com",
  "organisations": [
    {
      "orgId": "550e8400-e29b-41d4-a716-446655440000",
      "orgName": "Acme Corp",
      "roles": [
        {
          "id": "770e8400-e29b-41d4-a716-446655440002",
          "name": "Content Manager"
        }
      ],
      "effectivePermissions": [
        {
          "name": "articles",
          "permissions": 15,
          "children": [...]
        },
        {
          "name": "reports",
          "permissions": 3
        }
      ]
    }
  ]
}
```

### 7. Check Permission
```bash
GET /api/v1/user/entitlements/check?orgId=550e8400-e29b-41d4-a716-446655440000&resource=articles&action=create
```

Response:
```json
true
```

## Error Handling

All endpoints return consistent error responses:

```json
{
  "timestamp": "2026-03-01T19:45:30",
  "status": 404,
  "error": "Not Found",
  "message": "Role not found with ID: 770e8400-e29b-41d4-a716-446655440002",
  "path": "/api/v1/admin/roles/770e8400-e29b-41d4-a716-446655440002"
}
```

### Error Types
- **400 Bad Request**: Validation errors, duplicate entries
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected errors

## Security Considerations

### Current Implementation
⚠️ **Note**: Authentication and authorization are not yet implemented. All endpoints are currently unprotected.

### Planned Security Features
- Spring Security JWT authentication
- @CurrentUser annotation for authenticated user context
- Role-based access control on endpoints
- Password policies and validation
- Session management
- Rate limiting
- CORS configuration

## Development

### Prerequisites
- Java 21+
- PostgreSQL 14+
- Gradle 9.3+

### Running Locally
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Run application
./gradlew bootRun

# Application will start on http://localhost:8080
```

### Database Migrations
Liquibase migrations run automatically on application startup.

Manual migration:
```bash
./gradlew update
```

### JOOQ Code Generation
```bash
./gradlew generateJooq
```

### Build
```bash
# Build without tests
./gradlew build -x test

# Build with tests
./gradlew build
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### API Tests
```bash
./gradlew apiTest
```

⚠️ **Note**: Tests are not yet implemented but the code structure supports easy test addition.

## Performance Considerations

### Database Indexes
All frequently queried columns have indexes:
- `organisation.name`
- `individual.email`, `individual.org_id`
- `roles.name`, `roles.org_id`
- `resource.name`, `resource.parent_resource_id`
- `individual_role.individual_id`, `individual_role.role_id`

### Query Optimization
- JOOQ provides type-safe, optimized SQL
- Pagination support on all list endpoints
- Lazy loading for hierarchical structures
- JSONB indexing for permission queries

### Caching Strategy (Planned)
- Role permissions (rarely change)
- Resource hierarchy (rarely change)
- User entitlements (cache per session)

## Monitoring & Observability

### Logging
- SLF4J with Logback
- Structured logging with correlation IDs
- Log levels: ERROR, WARN, INFO, DEBUG

### Metrics (Planned)
- API endpoint metrics
- Database query metrics
- Permission calculation metrics
- Cache hit/miss rates

### Health Checks (Planned)
- Database connectivity
- Liquibase migration status
- Cache availability

## Contributing

### Code Style
- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction
- Write comprehensive JavaDoc
- Follow package-by-feature structure

### Commit Guidelines
- Use conventional commits
- Reference issue numbers
- Keep commits atomic

## License

[Your License Here]

## Support

For questions or issues, please contact:
- Email: [your-email]
- Slack: [your-slack-channel]
- Issues: [your-issue-tracker]

---

**Version**: 1.0.0  
**Last Updated**: March 1, 2026  
**Status**: Production Ready (Core Functionality)
