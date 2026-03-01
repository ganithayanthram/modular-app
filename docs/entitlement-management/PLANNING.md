# User Entitlement Management Module - Planning Document

## 1. Executive Summary

This document outlines the design and implementation plan for a **User Entitlement Management** module using Spring Modulith architecture. The module will manage relationships between Organizations, Individuals (users), Roles, and Resources with comprehensive RBAC (Role-Based Access Control).

---

## 2. Reference Codebase Analysis

### 2.1 Existing Data Model (from `pure-heart-backend`)

#### **Core Entities**

| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| **Organisation** | `id`, `name`, `category`, `metaData` (JSONB), `isActive`, `status`, `documents` (JSONB) | Has many Roles, IndividualRoles, IndividualPermissions |
| **Individual** | `id`, `name`, `email`, `mobileNumber`, `password`, `metaData` (JSONB), `isActive` | Has many IndividualRoles, IndividualPermissions |
| **Roles** | `id`, `name`, `description`, `parentRoleId`, `permissions` (JSONB List<RoleNode>), `orgId`, `isActive` | Belongs to Organisation, self-referential (parent_role_id) |
| **Resource** | `id`, `name`, `description`, `type`, `parentResourceId`, `validations` (JSONB), `isActive` | Self-referential (parent_resource_id) |

#### **Junction Tables**

| Table | Purpose | Key Fields |
|-------|---------|-----------|
| **individual_role** | Links individuals to roles within an organization | `id`, `individual_id`, `role_id`, `org_id` |
| **individual_permission** | Stores individual-specific permission overrides | `id`, `individual_id`, `permissions` (JSONB List<RoleNode>), `org_id`, `remarks` |

#### **Permission Model**

- **RoleNode Structure**: Hierarchical tree structure stored as JSONB
  ```java
  class RoleNode {
      String name;           // e.g., "Dashboard", "Donors", "Create_Donor"
      Integer permissions;   // Bitwise: 1=Read, 2=Write, 4=Update, 8=Delete, 15=All
      Integer displayNumber; // UI ordering
      String type;           // "MENU", "PAGE", "ACTION"
      List<RoleNode> children; // Nested permissions
  }
  ```
- **Permission Inheritance**: Roles can have `parent_role_id` for hierarchical inheritance
- **Individual Overrides**: `individual_permission` table allows per-user permission customization

### 2.2 Architecture Patterns

#### **Package Structure** (from `docs/backend-code-guidelines.md`)
```
src/main/java/com/company/[domain]/
├── controller/          # REST endpoints
├── service/             # Business logic
├── repository/          # Data access (JOOQ-based)
├── dto/
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── mapper/              # Object mapping
└── exception/           # Domain exceptions
```

#### **Testing Patterns**
- **Unit Tests** (`*UTest.java`): `@ExtendWith(MockitoExtension.class)`, `@Tag("unit")`
- **API Tests** (`*ApiTest.java`): `@BaseApiTest(controllers = XController.class)`, `@Tag("api")`, uses `MockMvc` and `@MockBean`
- **Integration Tests** (`*ITest.java`): `@SpringBootTest`, `@Tag("integration")`, uses Testcontainers

#### **Repository Pattern**
- JOOQ-based with type-safe queries
- Separate DAOs for basic CRUD
- Custom query methods in Repository classes
- Example: `DonorsRepository` extends functionality of `DonorsDao`

#### **Service Pattern**
- Service interfaces with implementation classes
- Handler pattern for complex operations (e.g., `ProgramCreateHandler`, `ProgramUpdateHandler`)
- Cache management with `@Cacheable`, `@CacheEvict`

#### **Controller Pattern**
- RESTful endpoints with `@RestController`
- `@CurrentUser UserPrincipal` for authenticated user context
- Request/Response DTOs with validation (`@Valid`)
- Standard HTTP status codes

---

## 3. Module Design

### 3.1 Architecture Strategy: Modular Monolith with Microservices Future

#### Current Phase: Modular Monolith

This module is built as a **modular monolith** using Spring Modulith, providing:

- **Single Deployment Unit**: One JAR/WAR file for simplified operations
- **Shared Database**: PostgreSQL with logical schema separation per module
- **In-Process Events**: Spring Modulith application events for inter-module communication
- **Strong Module Boundaries**: Enforced at compile-time and runtime
- **Transactional Consistency**: ACID guarantees within the monolith

**Benefits**:
- Faster development and debugging
- Lower infrastructure costs
- Simpler deployment pipeline
- Easier testing (no distributed system complexity)
- Better performance (no network overhead)

#### Future Phase: Microservices Transition

When business needs demand it (scale, team autonomy, independent deployment), modules can be extracted:

| Aspect | Modular Monolith (Current) | Microservices (Future) |
|--------|---------------------------|------------------------|
| **Deployment** | Single JAR/WAR | Independent services per module |
| **Database** | Shared PostgreSQL (logical separation) | Database-per-service |
| **Communication** | In-process Spring events | Message broker (Kafka/RabbitMQ) |
| **Transactions** | ACID (single DB) | Eventual consistency (Saga pattern) |
| **Discovery** | N/A | Service registry (Eureka/Consul) |
| **API Gateway** | N/A | Spring Cloud Gateway / Kong |
| **Observability** | Simple logging | Distributed tracing (Zipkin/Jaeger) |

#### Design Principles for Easy Transition

To ensure smooth microservices extraction, we follow these rules:

1. **No Direct Method Calls Between Modules**
   - ❌ Bad: `roleService.getRoleById(roleId)` from another module
   - ✅ Good: Publish `RoleRequestedEvent` and subscribe to `RoleRetrievedEvent`

2. **Event-Driven Communication**
   ```java
   // Publishing module
   eventPublisher.publishEvent(new OrganisationCreatedEvent(orgId));

   // Subscribing module (different module)
   @ApplicationModuleListener
   void onOrganisationCreated(OrganisationCreatedEvent event) {
       // Create default roles
   }
   ```

3. **DTOs for Inter-Module Communication**
   - Never share domain entities across modules
   - Use versioned DTOs in events
   - Maintain backward compatibility

4. **Database Schema Separation**
   - Each module owns its tables
   - No foreign keys across module boundaries
   - Use logical IDs (UUIDs) for cross-module references

5. **Idempotent Event Handlers**
   - All event handlers must be idempotent
   - Use event IDs to detect duplicates
   - Prepare for at-least-once delivery semantics

6. **Bounded Contexts (DDD)**
   - Each module represents a distinct domain
   - Clear ubiquitous language per module
   - No shared domain logic

#### Migration Path

When transitioning to microservices:

1. **Extract Module**: Move module code to separate repository
2. **Replace Events**: Swap Spring events with Kafka/RabbitMQ messages
3. **Separate Database**: Extract module's tables to dedicated database
4. **Add API Layer**: Expose REST/gRPC APIs for synchronous calls
5. **Service Discovery**: Register service with Eureka/Consul
6. **Update Clients**: Point to new service via API Gateway

**No code changes needed** in business logic if design principles are followed!

### 3.2 Module Boundaries (Spring Modulith)

**Module Name**: `entitlement`

**Package Structure**:
```
src/main/java/com/ganithyanthram/modularapp/entitlement/
├── EntitlementModuleConfiguration.java  # Module configuration
├── organisation/
│   ├── controller/
│   │   └── OrganisationController.java
│   ├── service/
│   │   ├── OrganisationService.java
│   │   └── impl/
│   │       └── OrganisationServiceImpl.java
│   ├── repository/
│   │   └── OrganisationRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateOrganisationRequest.java
│   │   │   └── UpdateOrganisationRequest.java
│   │   └── response/
│   │       └── OrganisationResponse.java
│   └── mapper/
│       └── OrganisationMapper.java
├── individual/
│   ├── controller/
│   │   └── IndividualController.java
│   ├── service/
│   │   ├── IndividualService.java
│   │   └── impl/
│   │       └── IndividualServiceImpl.java
│   ├── repository/
│   │   └── IndividualRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateIndividualRequest.java
│   │   │   └── UpdateIndividualRequest.java
│   │   └── response/
│   │       └── IndividualResponse.java
│   └── mapper/
│       └── IndividualMapper.java
├── role/
│   ├── controller/
│   │   └── RoleController.java
│   ├── service/
│   │   ├── RoleService.java
│   │   └── impl/
│   │       └── RoleServiceImpl.java
│   ├── repository/
│   │   └── RoleRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateRoleRequest.java
│   │   │   ├── UpdateRoleRequest.java
│   │   │   └── RoleNode.java
│   │   └── response/
│   │       └── RoleResponse.java
│   └── mapper/
│       └── RoleMapper.java
├── resource/
│   ├── controller/
│   │   └── ResourceController.java
│   ├── service/
│   │   ├── ResourceService.java
│   │   └── impl/
│   │       └── ResourceServiceImpl.java
│   ├── repository/
│   │   └── ResourceRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateResourceRequest.java
│   │   │   └── UpdateResourceRequest.java
│   │   └── response/
│   │       └── ResourceResponse.java
│   └── mapper/
│       └── ResourceMapper.java
├── assignment/
│   ├── controller/
│   │   └── EntitlementAssignmentController.java
│   ├── service/
│   │   ├── RoleAssignmentService.java
│   │   ├── PermissionOverrideService.java
│   │   └── impl/
│   │       ├── RoleAssignmentServiceImpl.java
│   │       └── PermissionOverrideServiceImpl.java
│   ├── repository/
│   │   ├── IndividualRoleRepository.java
│   │   └── IndividualPermissionRepository.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── AssignRoleRequest.java
│   │   │   └── OverridePermissionRequest.java
│   │   └── response/
│   │       ├── UserEntitlementResponse.java
│   │       └── RoleAssignmentResponse.java
│   └── mapper/
│       └── AssignmentMapper.java
└── common/
    ├── exception/
    │   ├── OrganisationNotFoundException.java
    │   ├── IndividualNotFoundException.java
    │   ├── RoleNotFoundException.java
    │   ├── ResourceNotFoundException.java
    │   └── DuplicateAssignmentException.java
    └── security/
        ├── EntitlementSecurityService.java
        └── PermissionEvaluator.java
```

### 3.3 Database Schema

**Migration Tool**: Liquibase

**Migration Files Location**: `src/main/resources/db/changelog/`

**File Format Strategy**:
- **Master Changelog**: YAML format (`db.changelog-master.yaml`)
- **Individual Migrations**: XML format (`db.changelog-*.xml`)

**Naming Convention**: `db.changelog-{version}-{description}.xml`

**Complete Table List** (11 tables extracted from `pure-heart-backend`):

**Core Entitlement Tables (6)**:
1. `organisation` - Organisation master data
2. `individual` - User/individual master data
3. `roles` - Role definitions with permissions (JSONB)
4. `resource` - Resource hierarchy
5. `individual_role` - Role assignments (junction table)
6. `individual_permission` - Permission overrides (JSONB)

**Audit & Verification Tables (3)**:
7. `individual_password_reset_audit` - Password reset tracking
8. `individual_verification_audit` - Email/mobile verification tracking
9. `individual_sessions` - Session management (JWT tokens)

**Supporting Tables (2)**:
10. `list_names` - Dropdown list names (master)
11. `list_values` - Dropdown list values (detail)

**Excluded Tables** (not part of entitlement system):
- ~~`contact_us`~~ - Belongs to separate "Contact/Communication" module
- ~~`document_repo`~~ - Document storage handled via JSONB columns (`organisation.documents`, `organisation.meta_data`, `individual.meta_data`)

**Key Design Principles**:
- ✅ **Schema Extracted from Reference**: All tables match `pure-heart-backend` exactly
- ✅ **JSONB for Flexibility**: Use JSONB for metadata, permissions, and validations
- ✅ **Audit Columns**: All tables have `created_by`, `created_on`, `updated_by`, `updated_on`
- ✅ **Soft Delete**: Use `is_active` flag instead of hard deletes
- ✅ **UUID Primary Keys**: All tables use UUID for primary keys
- ✅ **Foreign Keys**: Within-table FKs only (e.g., roles.parent_role_id → roles.id)

**Liquibase Changelog Structure**:
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml                   # Master changelog (YAML)
├── db.changelog-1.0-create-core-tables.xml    # Core entitlement tables (XML)
├── db.changelog-1.1-create-audit-tables.xml   # Audit and verification tables (XML)
├── db.changelog-1.2-create-supporting-tables.xml  # Supporting tables (XML)
└── db.changelog-1.3-create-indexes.xml        # Performance indexes (XML)
```

**JOOQ Configuration**:
- Use `forcedType` in `build.gradle` to map JSONB columns to `List<RoleNode>`
- Generate POJOs, Records, and DAOs for all tables
- Custom converters for JSONB ↔ Java object mapping

**Gradle Commands**:
```bash
# Run Liquibase migrations
./gradlew update

# Rollback last changeset
./gradlew rollbackCount -PliquibaseCommandValue=1

# Generate JOOQ classes (after migrations)
./gradlew generateJooq
```

---

## 4. API Endpoints

### 4.1 Admin APIs

#### **Organisation Management**
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/admin/organisations` | Create organisation | `CreateOrganisationRequest` | `201 Created` + UUID |
| GET | `/api/v1/admin/organisations/{id}` | Get organisation by ID | - | `200 OK` + `OrganisationResponse` |
| GET | `/api/v1/admin/organisations` | List all organisations | Query params: `page`, `size`, `search` | `200 OK` + Page<OrganisationResponse> |
| PUT | `/api/v1/admin/organisations/{id}` | Update organisation | `UpdateOrganisationRequest` | `200 OK` + `OrganisationResponse` |
| DELETE | `/api/v1/admin/organisations/{id}` | Soft delete organisation | - | `204 No Content` |
| PATCH | `/api/v1/admin/organisations/{id}/activate` | Activate organisation | - | `200 OK` |
| PATCH | `/api/v1/admin/organisations/{id}/deactivate` | Deactivate organisation | - | `200 OK` |

#### **Individual Management**
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/admin/individuals` | Create individual | `CreateIndividualRequest` | `201 Created` + UUID |
| GET | `/api/v1/admin/individuals/{id}` | Get individual by ID | - | `200 OK` + `IndividualResponse` |
| GET | `/api/v1/admin/individuals` | List all individuals | Query params: `page`, `size`, `search`, `orgId` | `200 OK` + Page<IndividualResponse> |
| PUT | `/api/v1/admin/individuals/{id}` | Update individual | `UpdateIndividualRequest` | `200 OK` + `IndividualResponse` |
| DELETE | `/api/v1/admin/individuals/{id}` | Soft delete individual | - | `204 No Content` |
| PATCH | `/api/v1/admin/individuals/{id}/activate` | Activate individual | - | `200 OK` |
| PATCH | `/api/v1/admin/individuals/{id}/deactivate` | Deactivate individual | - | `200 OK` |

#### **Role Management**
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/admin/roles` | Create role | `CreateRoleRequest` | `201 Created` + UUID |
| GET | `/api/v1/admin/roles/{id}` | Get role by ID | - | `200 OK` + `RoleResponse` |
| GET | `/api/v1/admin/roles` | List roles | Query params: `page`, `size`, `orgId` | `200 OK` + Page<RoleResponse> |
| PUT | `/api/v1/admin/roles/{id}` | Update role | `UpdateRoleRequest` | `200 OK` + `RoleResponse` |
| DELETE | `/api/v1/admin/roles/{id}` | Soft delete role | - | `204 No Content` |
| PATCH | `/api/v1/admin/roles/{id}/activate` | Activate role | - | `200 OK` |

#### **Resource Management**
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/admin/resources` | Create resource | `CreateResourceRequest` | `201 Created` + UUID |
| GET | `/api/v1/admin/resources/{id}` | Get resource by ID | - | `200 OK` + `ResourceResponse` |
| GET | `/api/v1/admin/resources` | List resources | Query params: `page`, `size`, `type` | `200 OK` + Page<ResourceResponse> |
| PUT | `/api/v1/admin/resources/{id}` | Update resource | `UpdateResourceRequest` | `200 OK` + `ResourceResponse` |
| DELETE | `/api/v1/admin/resources/{id}` | Soft delete resource | - | `204 No Content` |

#### **Role & Permission Assignment**
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/v1/admin/assignments/roles` | Assign role to individual | `AssignRoleRequest` | `201 Created` + `RoleAssignmentResponse` |
| DELETE | `/api/v1/admin/assignments/roles/{individualId}/{roleId}` | Revoke role from individual | - | `204 No Content` |
| GET | `/api/v1/admin/assignments/individuals/{individualId}/roles` | Get roles for individual | - | `200 OK` + List<RoleResponse> |
| GET | `/api/v1/admin/assignments/roles/{roleId}/individuals` | Get individuals with role | - | `200 OK` + List<IndividualResponse> |
| POST | `/api/v1/admin/assignments/permissions` | Override permissions for individual | `OverridePermissionRequest` | `201 Created` |
| GET | `/api/v1/admin/assignments/individuals/{individualId}/permissions` | Get effective permissions | - | `200 OK` + List<RoleNode> |

### 4.2 Normal User APIs

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/v1/users/me/entitlements` | Get my entitlements | - | `200 OK` + `UserEntitlementResponse` |
| GET | `/api/v1/users/me/roles` | Get my roles | - | `200 OK` + List<RoleResponse> |
| GET | `/api/v1/users/me/permissions` | Get my effective permissions | - | `200 OK` + List<RoleNode> |
| GET | `/api/v1/users/me/resources` | Get accessible resources | - | `200 OK` + List<ResourceResponse> |

---

## 5. Development Iterations

See `ITERATIONS.md` for detailed breakdown.

---

## 6. Testing Strategy

### 6.1 Test Coverage Requirements
- **Service Layer**: 80% minimum, 90% target
- **Repository Layer**: 70% minimum, 85% target
- **Controller Layer**: 60% minimum, 80% target

### 6.2 Test Types
1. **Unit Tests** (`*UTest.java`): Test business logic in isolation with mocks
2. **API Tests** (`*ApiTest.java`): Test controller endpoints with `MockMvc`
3. **Integration Tests** (`*ITest.java`): Test full stack with PostgreSQL (Testcontainers)

---

## 7. Security Considerations

1. **Authentication**: Use existing `@CurrentUser UserPrincipal` pattern
2. **Authorization**: Implement `@PreAuthorize` with custom permission evaluator
3. **Admin vs User**: Separate endpoints with role-based access
4. **Audit Trail**: Track `createdBy`, `updatedBy`, `createdOn`, `updatedOn` for all entities

---

## 8. Next Steps

1. ✅ Complete planning document
2. Create detailed iteration breakdown (`ITERATIONS.md`)
3. Set up database schema (Liquibase migrations)
4. Begin Iteration 1: Core entities and repositories

