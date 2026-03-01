# User Entitlement Management Module

## Overview

This module implements a comprehensive **User Entitlement Management** system using **Spring Modulith** architecture. It manages relationships between Organizations, Individuals (users), Roles, and Resources with full RBAC (Role-Based Access Control) support.

---

## Key Features

### 1. **Multi-Tenant Organization Management**
- Create and manage multiple organizations
- Soft delete and activation/deactivation
- Flexible metadata storage (JSONB)
- Full audit trail (created/updated by/on)

### 2. **Individual (User) Management**
- User profiles with secure password hashing
- Email and mobile number validation
- Flexible metadata for custom attributes
- Multi-organization support

### 3. **Hierarchical Role Management**
- Role inheritance via `parent_role_id`
- Organization-scoped roles
- Granular permission trees (JSONB)
- Permission bits: Read (1), Write (2), Update (4), Delete (8)

### 4. **Resource Management**
- Hierarchical resource structure
- Resource types: MODULE, PAGE, ACTION, etc.
- Custom validation rules (JSONB)
- Parent-child relationships

### 5. **Role & Permission Assignment**
- Assign multiple roles to individuals
- Individual-specific permission overrides
- Effective permission calculation (role + overrides)
- Organization-scoped assignments

### 6. **User-Facing APIs**
- View my entitlements
- View my roles and permissions
- View accessible resources
- Read-only access for normal users

---

## Architecture

### Module Structure

```
entitlement/
├── organisation/          # Organisation management
├── individual/            # Individual (user) management
├── role/                  # Role management
├── resource/              # Resource management
├── assignment/            # Role & permission assignments
└── common/                # Shared exceptions and utilities
```

### Technology Stack

- **Framework**: Spring Boot 3.x
- **Architecture**: Spring Modulith (modular monolith with future microservices capability)
- **Database**: PostgreSQL 15+
- **Data Access**: JOOQ (type-safe SQL queries with code generation)
- **Migration**: Liquibase
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Security**: Spring Security with JWT
- **Validation**: Jakarta Validation (Bean Validation)
- **Mapping**: MapStruct (or manual mappers)

---

## Architecture Strategy

### Modular Monolith Foundation

This module is built using **Spring Modulith** as a **modular monolith** with strong module boundaries and loose coupling. The architecture is designed to enable future extraction into microservices without major refactoring.

#### Key Principles

1. **Module Independence**: Each submodule (Organisation, Individual, Role, Resource, Assignment) is designed as if it were a separate service
2. **Event-Driven Communication**: Modules communicate via Spring Modulith events, not direct method calls
3. **Package-Private Visibility**: Internal implementation details are hidden; only public APIs are exposed
4. **Database Schema Separation**: Each module owns its tables (though currently in a shared database)
5. **No Shared Entities**: Modules use DTOs for inter-module communication, not shared domain entities

#### Current Phase: Modular Monolith

- **Deployment**: Single deployable JAR/WAR
- **Database**: Shared PostgreSQL database with logical schema separation
- **Communication**: In-process Spring Modulith events
- **Module Boundaries**: Enforced by Spring Modulith at compile-time and runtime
- **Benefits**:
  - Simplified deployment and operations
  - Lower infrastructure costs
  - Easier debugging and testing
  - Transactional consistency within modules

#### Future Phase: Microservices Transition

When scale or team structure demands it, modules can be extracted into separate microservices:

- **Deployment**: Each module becomes an independent service
- **Database**: Separate database per service (database-per-service pattern)
- **Communication**: Replace in-process events with message broker (Kafka/RabbitMQ)
- **API Gateway**: Route external requests to appropriate services
- **Service Discovery**: Use Eureka, Consul, or Kubernetes service discovery
- **Distributed Tracing**: Implement with Zipkin/Jaeger for observability

#### Design for Easy Transition

To ensure smooth microservices extraction, we follow these rules:

- ✅ **No Direct Method Calls**: Modules communicate only via events or public APIs
- ✅ **Eventual Consistency**: Design for asynchronous workflows where possible
- ✅ **Idempotent Operations**: All event handlers are idempotent
- ✅ **Bounded Contexts**: Each module has clear domain boundaries (DDD principles)
- ✅ **API Contracts**: All inter-module communication uses versioned DTOs
- ✅ **Independent Data**: No foreign keys across module boundaries

---

## Data Model

### Core Entities

| Entity | Description | Key Relationships |
|--------|-------------|-------------------|
| **Organisation** | Multi-tenant organizations | Has many Roles, IndividualRoles |
| **Individual** | Users/people | Has many IndividualRoles, IndividualPermissions |
| **Role** | Named permission sets | Belongs to Organisation, self-referential (parent) |
| **Resource** | Protected resources | Self-referential (parent) |
| **IndividualRole** | Role assignments | Links Individual + Role + Organisation |
| **IndividualPermission** | Permission overrides | Links Individual + Permissions + Organisation |

### Permission Model

**RoleNode Structure** (stored as JSONB):
```json
{
  "name": "Dashboard",
  "type": "MENU",
  "permissions": 1,
  "displayNumber": 1,
  "children": [
    {
      "name": "View_Dashboard",
      "type": "PAGE",
      "permissions": 1,
      "displayNumber": 1,
      "children": []
    }
  ]
}
```

**Permission Bits**:
- `1` = Read
- `2` = Write/Create
- `4` = Update
- `8` = Delete
- `15` = All (1 + 2 + 4 + 8)

---

## API Endpoints

### Admin APIs (ROLE_ADMIN)

| Resource | Endpoints | Methods |
|----------|-----------|---------|
| **Organisations** | `/api/v1/admin/organisations` | POST, GET, PUT, DELETE, PATCH |
| **Individuals** | `/api/v1/admin/individuals` | POST, GET, PUT, DELETE, PATCH |
| **Roles** | `/api/v1/admin/roles` | POST, GET, PUT, DELETE, PATCH |
| **Resources** | `/api/v1/admin/resources` | POST, GET, PUT, DELETE |
| **Assignments** | `/api/v1/admin/assignments/roles` | POST, GET, DELETE |
| **Permissions** | `/api/v1/admin/assignments/permissions` | POST, GET |

### User APIs (ROLE_USER)

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/users/me/entitlements` | Get my entitlements (roles + permissions + resources) |
| `GET /api/v1/users/me/roles` | Get my roles |
| `GET /api/v1/users/me/permissions` | Get my effective permissions |
| `GET /api/v1/users/me/resources` | Get my accessible resources |

**See `API_SPECIFICATION.md` for detailed API documentation.**

---

## Development Plan

### Iteration Breakdown

| Iteration | Focus | Duration | Test Types |
|-----------|-------|----------|------------|
| **1** | Database schema & entities | 2-3 days | Integration |
| **2** | Service: Org & Individual | 2 days | Unit |
| **3** | Service: Role & Resource | 2 days | Unit |
| **4** | Service: Assignments | 2 days | Unit |
| **5** | Controller: Admin APIs | 3 days | API |
| **6** | Controller: User APIs & Security | 2 days | API + Integration |

**Total Estimated Effort**: 13-14 days

**See `ITERATIONS.md` for detailed iteration breakdown.**

---

## Testing Strategy

### Test Types

1. **Unit Tests** (`*UTest.java`)
   - Test business logic in isolation
   - Mock all dependencies
   - Fast execution (no database)
   - Target: 90% coverage for services

2. **API Tests** (`*ApiTest.java`)
   - Test controller endpoints with MockMvc
   - Mock service layer
   - Validate request/response formats
   - Target: 80% coverage for controllers

3. **Integration Tests** (`*ITest.java`)
   - Test full stack with PostgreSQL (Testcontainers)
   - Validate database operations
   - Test end-to-end flows
   - Target: 85% coverage for repositories

### Test Naming Convention

- Unit: `OrganisationServiceUTest.java`
- API: `OrganisationControllerApiTest.java`
- Integration: `OrganisationRepositoryITest.java`

---

## Security

### Authentication
- JWT-based authentication
- `@CurrentUser UserPrincipal` for authenticated user context

### Authorization
- `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints
- `@PreAuthorize("hasRole('USER')")` for user endpoints
- Custom permission evaluator for resource-level access

### Audit Trail
All entities track:
- `createdBy` (UUID)
- `updatedBy` (UUID)
- `createdOn` (LocalDateTime)
- `updatedOn` (LocalDateTime)

---

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Docker (for Testcontainers)
- Gradle 9+

### Setup

1. **Database Setup**
   ```bash
   # Create database
   createdb entitlement_db

   # Run Liquibase migrations
   ./gradlew update

   # Generate JOOQ classes
   ./gradlew generateJooq
   ```

2. **Run Tests**
   ```bash
   # Unit tests
   ./gradlew unitTest
   
   # API tests
   ./gradlew apiTest
   
   # Integration tests
   ./gradlew integrationTest
   
   # All tests
   ./gradlew test
   ```

3. **Run Application**
   ```bash
   ./gradlew bootRun
   ```

---

## Documentation

- **[PLANNING.md](PLANNING.md)**: Detailed planning and design document
- **[ITERATIONS.md](ITERATIONS.md)**: Development iteration breakdown
- **[API_SPECIFICATION.md](API_SPECIFICATION.md)**: Complete API documentation
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)**: Testing patterns and examples (TBD)

---

## Next Steps

1. ✅ **Planning Phase Complete**
   - Data model analyzed
   - Module structure defined
   - API endpoints specified
   - Development iterations planned

2. **Ready to Start Implementation**
   - Begin Iteration 1: Database schema & entities
   - Follow TDD approach: write tests first
   - Implement features incrementally

---

## Contributing

### Development Workflow
1. Create feature branch from `main`
2. Write tests first (TDD)
3. Implement feature
4. Ensure all tests pass
5. Create pull request
6. Code review
7. Merge to `main`

### Code Standards
- Follow existing patterns from `pure-heart-backend`
- Maintain 85%+ test coverage
- Use meaningful variable/method names
- Add JavaDoc for public APIs
- Follow Spring Boot best practices

---

## License

[Your License Here]

---

## Contact

For questions or support, contact: [Your Contact Info]

