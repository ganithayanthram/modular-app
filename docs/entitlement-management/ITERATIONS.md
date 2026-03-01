# User Entitlement Management - Development Iterations

## Overview

This document breaks down the implementation into **6 iterations**, each with specific deliverables and test cases. We follow **Test-Driven Development (TDD)**: write tests first, then implement features.

---

## Iteration 1: Database Schema & JOOQ Code Generation

### Goals
- Set up database schema using Liquibase migrations
- Configure JOOQ code generation for all core tables
- Generate JOOQ POJOs, Records, and DAOs
- Implement custom repository layer on top of JOOQ DAOs
- No tests in this iteration (repositories are tested via service-layer integration tests in later iterations)

### Module Boundary Principles (Spring Modulith)
- Each module owns its database tables (no foreign keys across modules)
- Use UUIDs for cross-module references
- Design for future database-per-service separation
- Event-driven communication between modules (no direct DB access)

### Deliverables

#### 1.1 Database Schema (Liquibase)

**Master Changelog**: `src/main/resources/db/changelog/db.changelog-master.yaml` (YAML format)

**Migration Files** (XML format):
- `db.changelog-1.0-create-core-tables.xml` - Core entitlement tables
- `db.changelog-1.1-create-audit-tables.xml` - Audit and verification tables
- `db.changelog-1.2-create-supporting-tables.xml` - Supporting tables (list_names, list_values)
- `db.changelog-1.3-create-indexes.xml` - Performance indexes

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

**Example 1**: `db.changelog-master.yaml` (Master Changelog - YAML format)
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/db.changelog-1.0-create-core-tables.xml
  - include:
      file: db/changelog/db.changelog-1.1-create-audit-tables.xml
  - include:
      file: db/changelog/db.changelog-1.2-create-supporting-tables.xml
  - include:
      file: db/changelog/db.changelog-1.3-create-indexes.xml
```

**Example 2**: `db.changelog-1.0-create-core-tables.xml` (Individual Migration - XML format)
```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.8.xsd">
    <changeSet id="db.changelog-1.0-create-core-tables" author="entitlement-module">
        <sql dbms="postgresql" splitStatements="true" stripComments="true">
            <![CDATA[
            -- Organisation table
            CREATE TABLE public.organisation (
                id uuid NOT NULL,
                name varchar(255) NOT NULL,
                category varchar(100) NOT NULL,
                meta_data jsonb NULL,
                is_active bool NULL DEFAULT true,
                status varchar(50) NULL,
                created_by uuid NULL,
                updated_by uuid NULL,
                created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT organisation_pkey PRIMARY KEY (id)
            );

            -- Individual table
            CREATE TABLE public.individual (
                id uuid NOT NULL,
                name varchar(255) NOT NULL,
                email varchar(100) NULL,
                mobile_number varchar(100) NULL,
                password varchar(100) NULL,
                social_login_provider varchar(50) NULL,
                social_login_provider_id varchar(255) NULL,
                social_login_provider_image_url varchar(255) NULL,
                meta_data jsonb NULL,
                is_active bool NULL DEFAULT true,
                created_by uuid NULL,
                updated_by uuid NULL,
                created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT individual_email_unique UNIQUE (email),
                CONSTRAINT individual_pkey PRIMARY KEY (id)
            );

            -- Roles table
            CREATE TABLE public.roles (
                id uuid NOT NULL,
                name varchar(100) NOT NULL,
                description varchar(255) NULL,
                parent_role_id uuid NULL,
                is_active bool NULL DEFAULT true,
                created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                created_by uuid NULL,
                updated_by uuid NULL,
                permissions jsonb NULL,
                org_id uuid NULL,
                CONSTRAINT roles_id_pk PRIMARY KEY (id),
                CONSTRAINT roles_parent_role_fk FOREIGN KEY (parent_role_id) REFERENCES public.roles(id)
            );

            -- Resource table
            CREATE TABLE public.resource (
                id uuid NOT NULL,
                name varchar(100) NOT NULL,
                description varchar(255) NULL,
                type varchar(50) NOT NULL,
                parent_resource_id uuid NULL,
                validations jsonb NULL,
                is_active bool NOT NULL DEFAULT true,
                created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                created_by uuid NULL,
                updated_by uuid NULL,
                CONSTRAINT resource_id_pk PRIMARY KEY (id),
                CONSTRAINT resource_parent_resource_fk FOREIGN KEY (parent_resource_id) REFERENCES public.resource(id)
            );

            -- Individual Role (junction table)
            CREATE TABLE public.individual_role (
                id uuid NOT NULL,
                individual_id uuid NOT NULL,
                role_id uuid NOT NULL,
                created_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_on timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                created_by uuid,
                updated_by uuid,
                org_id uuid,
                CONSTRAINT individual_role_id_pk PRIMARY KEY (id),
                CONSTRAINT individual_role_individual_id_fk FOREIGN KEY (individual_id) REFERENCES public.individual(id),
                CONSTRAINT individual_role_role_id_fk FOREIGN KEY (role_id) REFERENCES public.roles(id)
            );

            -- Individual Permission (overrides)
            CREATE TABLE public.individual_permission (
                id uuid NOT NULL,
                individual_id uuid NOT NULL,
                permissions jsonb NOT NULL,
                created_on timestamp DEFAULT CURRENT_TIMESTAMP,
                created_by uuid,
                updated_on timestamp DEFAULT CURRENT_TIMESTAMP,
                updated_by uuid,
                remarks varchar(512),
                org_id uuid,
                pages jsonb NULL,
                CONSTRAINT individual_permission_pk PRIMARY KEY (id),
                CONSTRAINT individual_permission_individual_fk FOREIGN KEY (individual_id) REFERENCES public.individual(id)
            );
            ]]>
        </sql>
    </changeSet>
</databaseChangeLog>
```

**Key Schema Principles**:
- ✅ **Schema Extracted from `pure-heart-backend`**: All 13 tables match the reference codebase exactly
- ✅ **Audit Columns**: All tables have `created_by`, `created_on`, `updated_by`, `updated_on`
- ✅ **Soft Delete**: Use `is_active` flag
- ✅ **JSONB Columns**: For flexible metadata, permissions, and validations
- ✅ **UUID Primary Keys**: All tables use UUID for primary keys
- ✅ **Foreign Keys**: Within-module FKs only (e.g., roles.parent_role_id → roles.id)

**Indexes** (in `db.changelog-1.3-create-indexes.xml`):
- `idx_individual_role_individual_id`
- `idx_individual_role_org_id`
- `idx_individual_permission_individual_id`
- `idx_roles_org_id`
- `idx_resource_parent_id`

**Gradle Commands**:
```bash
# Run Liquibase migrations
./gradlew update

# Rollback last changeset
./gradlew rollbackCount -PliquibaseCommandValue=1

# Validate changelog
./gradlew validate
```

#### 1.2 JOOQ Configuration & Code Generation
**File**: `build.gradle` (JOOQ configuration section)

**JOOQ Configuration**:
```gradle
jooq {
    configurations {
        main {
            generationTool {
                jdbc {
                    driver = 'org.postgresql.Driver'
                    url = 'jdbc:postgresql://localhost:5432/entitlement_db'
                    user = 'postgres'
                    password = 'postgres'
                }
                generator {
                    database {
                        name = 'org.jooq.meta.postgres.PostgresDatabase'
                        inputSchema = 'public'
                        forcedTypes {
                            forcedType {
                                name = 'JSONB'
                                includeExpression = '.*\\.permissions'
                                includeTypes = 'JSONB'
                                converter = 'com.ganithyanthram.modularapp.entitlement.common.converter.RoleNodeListConverter'
                            }
                            forcedType {
                                name = 'JSONB'
                                includeExpression = '.*\\.meta_data|.*\\.validations|.*\\.documents'
                                includeTypes = 'JSONB'
                                converter = 'com.ganithyanthram.modularapp.entitlement.common.converter.JsonbConverter'
                            }
                        }
                    }
                    generate {
                        pojos = true
                        daos = true
                        records = true
                    }
                    target {
                        packageName = 'com.ganithyanthram.modularapp.jooq'
                        directory = 'build/generated-src/jooq/main'
                    }
                }
            }
        }
    }
}
```

**Generated Files** (after running `./gradlew generateJooq`):
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/Organisation.java`
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/Individual.java`
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/Roles.java`
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/Resource.java`
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/IndividualRole.java`
- `build/generated-src/jooq/main/com/ganithyanthram/modularapp/jooq/tables/pojos/IndividualPermission.java`
- Corresponding DAOs and Records for each table

**Custom Converters**:
- `RoleNodeListConverter.java`: Converts JSONB ↔ `List<RoleNode>`
- `JsonbConverter.java`: Generic JSONB ↔ `Map<String, Object>` converter

#### 1.3 Custom Repository Layer (JOOQ-based)
**Files**:
- `src/main/java/com/ganithyanthram/modularapp/entitlement/organisation/repository/OrganisationRepository.java`
- `src/main/java/com/ganithyanthram/modularapp/entitlement/individual/repository/IndividualRepository.java`
- `src/main/java/com/ganithyanthram/modularapp/entitlement/role/repository/RoleRepository.java`
- `src/main/java/com/ganithyanthram/modularapp/entitlement/resource/repository/ResourceRepository.java`
- `src/main/java/com/ganithyanthram/modularapp/entitlement/assignment/repository/IndividualRoleRepository.java`
- `src/main/java/com/ganithyanthram/modularapp/entitlement/assignment/repository/IndividualPermissionRepository.java`

**Pattern**: Custom repositories that wrap JOOQ-generated DAOs and add business-specific queries

**Example** (`OrganisationRepository.java`):
```java
@Repository
public class OrganisationRepository {
    private final DSLContext dsl;
    private final OrganisationDao dao;

    public OrganisationRepository(DSLContext dsl, Configuration jooqConfiguration) {
        this.dsl = dsl;
        this.dao = new OrganisationDao(jooqConfiguration);
    }

    public UUID create(Organisation org) {
        dao.insert(org);
        return org.getId();
    }

    public Optional<Organisation> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(ORGANISATION)
            .where(ORGANISATION.ID.eq(id))
            .and(ORGANISATION.IS_ACTIVE.isTrue())
            .fetchOptionalInto(Organisation.class);
    }

    public List<Organisation> findByIsActiveTrue(int offset, int limit) {
        return dsl.selectFrom(ORGANISATION)
            .where(ORGANISATION.IS_ACTIVE.isTrue())
            .offset(offset)
            .limit(limit)
            .fetchInto(Organisation.class);
    }

    public List<Organisation> searchByName(String name, int offset, int limit) {
        return dsl.selectFrom(ORGANISATION)
            .where(ORGANISATION.NAME.containsIgnoreCase(name))
            .and(ORGANISATION.IS_ACTIVE.isTrue())
            .offset(offset)
            .limit(limit)
            .fetchInto(Organisation.class);
    }

    public boolean existsByNameAndIsActiveTrue(String name) {
        return dsl.fetchExists(
            dsl.selectFrom(ORGANISATION)
                .where(ORGANISATION.NAME.eq(name))
                .and(ORGANISATION.IS_ACTIVE.isTrue())
        );
    }
}
```

**Key Differences from JPA**:
- Use `DSLContext` for type-safe queries instead of method name conventions
- Explicit SQL construction with JOOQ's fluent API
- JOOQ-generated DAOs for basic CRUD operations
- Custom repository classes for complex queries
- No need for `@Entity`, `@Table`, or relationship annotations

### Test Cases

**No tests in Iteration 1**. Repositories will be tested indirectly through service-layer integration tests in Iterations 2-4.

**Rationale**:
- Repository layer is a thin wrapper around JOOQ DAOs
- Testing repositories in isolation provides limited value
- Service-layer integration tests (with Testcontainers) provide better coverage by testing:
  - Repository + Service + Database interactions end-to-end
  - Business logic validation
  - Transaction management
  - Real database constraints and behavior

### Acceptance Criteria
- ✅ All tables created with proper constraints (no cross-module foreign keys)
- ✅ Liquibase migrations run successfully (`./gradlew update`)
- ✅ JOOQ code generation completes without errors (`./gradlew generateJooq`)
- ✅ JOOQ POJOs, DAOs, and Records generated for all 11 tables
- ✅ Custom converters for JSONB columns working correctly
- ✅ All repository methods compile and follow JOOQ patterns
- ✅ Module boundaries respected (no direct cross-module DB access)
- ✅ Database schema matches `SCHEMA_REFERENCE.md` exactly

---

## Iteration 2: Service Layer - Organisation & Individual

### Goals
- Implement service layer for Organisation and Individual management
- Add business logic validation
- Write unit tests for service methods

### Deliverables

#### 2.1 DTOs
**Request DTOs**:
- `CreateOrganisationRequest.java` (name, category, metaData)
- `UpdateOrganisationRequest.java` (name, category, metaData)
- `CreateIndividualRequest.java` (name, email, mobileNumber, password, metaData)
- `UpdateIndividualRequest.java` (name, email, mobileNumber, metaData)

**Response DTOs**:
- `OrganisationResponse.java` (id, name, category, isActive, createdOn, updatedOn)
- `IndividualResponse.java` (id, name, email, mobileNumber, isActive, createdOn, updatedOn)

#### 2.2 Mappers
- `OrganisationMapper.java` (MapStruct or manual)
- `IndividualMapper.java`

#### 2.3 Services
**OrganisationService.java**:
```java
public interface OrganisationService {
    UUID createOrganisation(CreateOrganisationRequest request, UUID userId);
    OrganisationResponse getOrganisationById(UUID id);
    Page<OrganisationResponse> getAllOrganisations(Pageable pageable, String search);
    OrganisationResponse updateOrganisation(UUID id, UpdateOrganisationRequest request, UUID userId);
    void deleteOrganisation(UUID id);
    void activateOrganisation(UUID id);
    void deactivateOrganisation(UUID id);
}
```

**IndividualService.java**:
```java
public interface IndividualService {
    UUID createIndividual(CreateIndividualRequest request, UUID userId);
    IndividualResponse getIndividualById(UUID id);
    Page<IndividualResponse> getAllIndividuals(Pageable pageable, String search, UUID orgId);
    IndividualResponse updateIndividual(UUID id, UpdateIndividualRequest request, UUID userId);
    void deleteIndividual(UUID id);
    void activateIndividual(UUID id);
    void deactivateIndividual(UUID id);
}
```

#### 2.4 Business Logic
- Validate unique organisation names
- Validate unique individual emails
- Hash passwords for individuals (BCrypt)
- Validate metadata structure
- Soft delete (set `isActive = false`)

### Test Cases

#### Unit Tests (`*UTest.java`)

**OrganisationServiceUTest.java**:
- `shouldCreateOrganisationSuccessfully()`
- `shouldThrowExceptionWhenOrganisationNameExists()`
- `shouldGetOrganisationById()`
- `shouldThrowExceptionWhenOrganisationNotFound()`
- `shouldUpdateOrganisationSuccessfully()`
- `shouldSoftDeleteOrganisation()`
- `shouldActivateOrganisation()`
- `shouldDeactivateOrganisation()`
- `shouldSearchOrganisationsByName()`

**IndividualServiceUTest.java**:
- `shouldCreateIndividualSuccessfully()`
- `shouldHashPasswordWhenCreatingIndividual()`
- `shouldThrowExceptionWhenEmailExists()`
- `shouldGetIndividualById()`
- `shouldUpdateIndividualSuccessfully()`
- `shouldNotUpdatePasswordInUpdateMethod()`
- `shouldFilterIndividualsByOrganisation()`

#### Integration Tests (`*ITest.java`)

**OrganisationServiceITest.java** (Service layer with real database):
- `shouldCreateOrganisationAndPersistToDatabase()`
- `shouldEnforceDatabaseUniqueConstraintOnOrganisationName()`
- `shouldRollbackTransactionOnError()`

**IndividualServiceITest.java** (Service layer with real database):
- `shouldCreateIndividualAndPersistToDatabase()`
- `shouldEnforceDatabaseUniqueConstraintOnEmail()`
- `shouldHashPasswordBeforePersisting()`

**Testing Strategy**:
- **Unit Tests**: Test service business logic with mocked repositories
- **Integration Tests**: Test service + repository + database with Testcontainers
- **No Repository Tests**: Repositories are thin JOOQ wrappers, tested via service integration tests

### Acceptance Criteria
- ✅ All service methods implemented
- ✅ Business validation logic in place
- ✅ All unit tests pass (mocked repositories)
- ✅ All integration tests pass (Testcontainers with PostgreSQL)
- ✅ Test coverage: 90%+ for services
- ✅ Passwords are hashed (never stored in plain text)

---

## Iteration 3: Service Layer - Role & Resource

### Goals
- Implement service layer for Role and Resource management
- Handle hierarchical structures (parent roles, parent resources)
- Write unit tests

### Deliverables

#### 3.1 DTOs
**Request DTOs**:
- `CreateRoleRequest.java` (name, description, orgId, parentRoleId, permissions)
- `UpdateRoleRequest.java` (name, description, permissions)
- `RoleNode.java` (name, permissions, displayNumber, type, children)
- `CreateResourceRequest.java` (name, description, type, parentResourceId, validations)
- `UpdateResourceRequest.java` (name, description, type, validations)

**Response DTOs**:
- `RoleResponse.java` (id, name, description, orgId, parentRoleId, permissions, isActive)
- `ResourceResponse.java` (id, name, description, type, parentResourceId, validations, isActive, children)

#### 3.2 Services
**RoleService.java**:
```java
public interface RoleService {
    UUID createRole(CreateRoleRequest request, UUID userId);
    RoleResponse getRoleById(UUID id);
    Page<RoleResponse> getRolesByOrganisation(UUID orgId, Pageable pageable);
    RoleResponse updateRole(UUID id, UpdateRoleRequest request, UUID userId);
    void deleteRole(UUID id);
    void activateRole(UUID id);
    List<RoleNode> getEffectivePermissions(UUID roleId); // Includes parent role permissions
}
```

**ResourceService.java**:
```java
public interface ResourceService {
    UUID createResource(CreateResourceRequest request, UUID userId);
    ResourceResponse getResourceById(UUID id);
    Page<ResourceResponse> getAllResources(Pageable pageable, String type);
    ResourceResponse updateResource(UUID id, UpdateResourceRequest request, UUID userId);
    void deleteResource(UUID id);
    List<ResourceResponse> getResourceHierarchy(); // Tree structure
}
```

### Test Cases

#### Unit Tests (`*UTest.java`)

**RoleServiceUTest.java**:
- `shouldCreateRoleSuccessfully()`
- `shouldCreateRoleWithParentRole()`
- `shouldThrowExceptionWhenParentRoleNotFound()`
- `shouldUpdateRolePermissions()`
- `shouldGetEffectivePermissionsIncludingParent()`
- `shouldFilterRolesByOrganisation()`

**ResourceServiceUTest.java**:
- `shouldCreateResourceSuccessfully()`
- `shouldCreateResourceWithParent()`
- `shouldGetResourceHierarchy()`
- `shouldUpdateResourceValidations()`
- `shouldPreventCircularParentReferences()`

### Acceptance Criteria
- ✅ Role hierarchy works correctly
- ✅ Permission inheritance implemented
- ✅ Resource tree structure supported
- ✅ All unit tests pass
- ✅ Test coverage: 90%+ for services

---

## Iteration 4: Service Layer - Role & Permission Assignment

### Goals
- Implement role assignment to individuals
- Implement permission overrides
- Calculate effective permissions (role + overrides)
- Write unit tests

### Deliverables

#### 4.1 DTOs
**Request DTOs**:
- `AssignRoleRequest.java` (individualId, roleId, orgId)
- `OverridePermissionRequest.java` (individualId, orgId, permissions, remarks)

**Response DTOs**:
- `RoleAssignmentResponse.java` (id, individualId, roleId, orgId, createdOn)
- `UserEntitlementResponse.java` (individualId, roles, effectivePermissions, accessibleResources)

#### 4.2 Services
**RoleAssignmentService.java**:
```java
public interface RoleAssignmentService {
    UUID assignRole(AssignRoleRequest request, UUID userId);
    void revokeRole(UUID individualId, UUID roleId);
    List<RoleResponse> getRolesByIndividual(UUID individualId);
    List<IndividualResponse> getIndividualsByRole(UUID roleId);
}
```

**PermissionOverrideService.java**:
```java
public interface PermissionOverrideService {
    UUID overridePermissions(OverridePermissionRequest request, UUID userId);
    List<RoleNode> getEffectivePermissions(UUID individualId, UUID orgId);
    UserEntitlementResponse getUserEntitlements(UUID individualId);
}
```

### Test Cases

#### Unit Tests (`*UTest.java`)

**RoleAssignmentServiceUTest.java**:
- `shouldAssignRoleToIndividual()`
- `shouldThrowExceptionWhenRoleAlreadyAssigned()`
- `shouldRevokeRoleFromIndividual()`
- `shouldGetAllRolesForIndividual()`
- `shouldGetAllIndividualsWithRole()`

**PermissionOverrideServiceUTest.java**:
- `shouldOverridePermissionsForIndividual()`
- `shouldCalculateEffectivePermissions()` (role + overrides)
- `shouldMergeMultipleRolePermissions()`
- `shouldGetUserEntitlements()`

### Acceptance Criteria
- ✅ Role assignment works correctly
- ✅ Permission override logic implemented
- ✅ Effective permissions calculated correctly
- ✅ All unit tests pass
- ✅ Test coverage: 90%+ for services

---

## Iteration 5: Controller Layer - Admin APIs

### Goals
- Implement all admin REST endpoints
- Add request validation
- Write API tests

### Deliverables

#### 5.1 Controllers
- `OrganisationController.java` (7 endpoints)
- `IndividualController.java` (7 endpoints)
- `RoleController.java` (6 endpoints)
- `ResourceController.java` (5 endpoints)
- `EntitlementAssignmentController.java` (6 endpoints)

#### 5.2 Exception Handling
- `@ControllerAdvice` for global exception handling
- Custom exceptions: `OrganisationNotFoundException`, `IndividualNotFoundException`, etc.

### Test Cases

#### API Tests (`*ApiTest.java`)

**OrganisationControllerApiTest.java**:
- `shouldCreateOrganisationSuccessfully()`
- `shouldReturn400WhenNameIsMissing()`
- `shouldGetOrganisationById()`
- `shouldReturn404WhenOrganisationNotFound()`
- `shouldUpdateOrganisationSuccessfully()`
- `shouldDeleteOrganisationSuccessfully()`
- `shouldSearchOrganisationsByName()`

**IndividualControllerApiTest.java**:
- `shouldCreateIndividualSuccessfully()`
- `shouldReturn400WhenEmailIsInvalid()`
- `shouldGetIndividualById()`
- `shouldUpdateIndividualSuccessfully()`
- `shouldFilterIndividualsByOrganisation()`

**RoleControllerApiTest.java**:
- `shouldCreateRoleSuccessfully()`
- `shouldUpdateRolePermissions()`
- `shouldGetRolesByOrganisation()`

**ResourceControllerApiTest.java**:
- `shouldCreateResourceSuccessfully()`
- `shouldGetResourceHierarchy()`

**EntitlementAssignmentControllerApiTest.java**:
- `shouldAssignRoleToIndividual()`
- `shouldRevokeRoleFromIndividual()`
- `shouldOverridePermissions()`
- `shouldGetEffectivePermissions()`

### Acceptance Criteria
- ✅ All admin endpoints implemented
- ✅ Request validation works
- ✅ All API tests pass
- ✅ Test coverage: 80%+ for controllers

---

## Iteration 6: Controller Layer - User APIs & Security

### Goals
- Implement user-facing endpoints
- Add authorization checks
- Write API tests
- Integration tests for end-to-end flows

### Deliverables

#### 6.1 User Controller
**UserEntitlementController.java**:
- `GET /api/v1/users/me/entitlements`
- `GET /api/v1/users/me/roles`
- `GET /api/v1/users/me/permissions`
- `GET /api/v1/users/me/resources`

#### 6.2 Security
- `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints
- `@PreAuthorize("hasRole('USER')")` for user endpoints
- Custom permission evaluator for resource-level access

### Test Cases

#### API Tests (`*ApiTest.java`)

**UserEntitlementControllerApiTest.java**:
- `shouldGetMyEntitlements()`
- `shouldGetMyRoles()`
- `shouldGetMyEffectivePermissions()`
- `shouldGetMyAccessibleResources()`
- `shouldReturn403WhenNotAuthenticated()`

#### Integration Tests (`*ITest.java`)

**EntitlementEndToEndITest.java**:
- `shouldCreateOrganisationAndAssignRole()`
- `shouldCalculateEffectivePermissionsWithOverrides()`
- `shouldEnforceRoleBasedAccessControl()`

### Acceptance Criteria
- ✅ All user endpoints implemented
- ✅ Authorization works correctly
- ✅ All API tests pass
- ✅ All integration tests pass
- ✅ Overall test coverage: 85%+

---

## Summary

| Iteration | Focus | Test Types | Estimated Effort |
|-----------|-------|------------|------------------|
| 1 | Database & Entities | Integration | 2-3 days |
| 2 | Service: Org & Individual | Unit | 2 days |
| 3 | Service: Role & Resource | Unit | 2 days |
| 4 | Service: Assignments | Unit | 2 days |
| 5 | Controller: Admin APIs | API | 3 days |
| 6 | Controller: User APIs & Security | API + Integration | 2 days |

**Total**: ~13-14 days

