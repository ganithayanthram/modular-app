# Database Schema Reference

## Overview

This document provides the complete database schema for the User Entitlement Management module, extracted from the `pure-heart-backend` reference codebase.

**Total Tables**: 11
**Source**: `/Users/chidhagnidev/Documents/codebase/pure-heart-backend/src/main/resources/db/changelog/`
**Migration Tool**: Liquibase
**File Format**: YAML master changelog + XML individual migrations

---

## Table Categories

### Core Entitlement Tables (6)

These tables form the foundation of the entitlement system:

1. **organisation** - Organisation/tenant master data
2. **individual** - User/individual master data
3. **roles** - Role definitions with hierarchical permissions
4. **resource** - Resource hierarchy for permission management
5. **individual_role** - Role assignments (junction table)
6. **individual_permission** - Individual-specific permission overrides

### Audit & Verification Tables (3)

These tables support authentication, verification, and session management:

7. **individual_password_reset_audit** - Password reset request tracking
8. **individual_verification_audit** - Email/mobile verification tracking
9. **individual_sessions** - Active session management (JWT tokens)

### Supporting Tables (2)

These tables provide supporting functionality:

10. **list_names** - Dropdown list names (master table)
11. **list_values** - Dropdown list values (detail table)

### Excluded Tables

The following tables were identified in `pure-heart-backend` but are **not included** in the entitlement module:

- **contact_us** - Belongs to a separate "Contact/Communication" module
- **document_repo** - Document storage is handled via JSONB columns in core tables (`organisation.documents`, `organisation.meta_data`, `individual.meta_data`)

---

## Liquibase File Structure

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml                      # Master changelog (YAML)
├── db.changelog-1.0-create-core-tables.xml       # 6 core tables (XML)
├── db.changelog-1.1-create-audit-tables.xml      # 3 audit tables (XML)
├── db.changelog-1.2-create-supporting-tables.xml # 2 supporting tables (XML)
└── db.changelog-1.3-create-indexes.xml           # Performance indexes (XML)
```

---

## Master Changelog (YAML)

**File**: `db.changelog-master.yaml`

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

---

## Table Schemas

### 1. organisation

**Purpose**: Organisation/tenant master data

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(255) | NOT NULL | Organisation name |
| category | VARCHAR(100) | NOT NULL | Organisation category |
| meta_data | JSONB | NULL | Flexible metadata (JSONB) |
| is_active | BOOLEAN | DEFAULT true | Soft delete flag |
| status | VARCHAR(50) | NULL | Organisation status |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| created_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Indexes**:
- `idx_organisation_name` on `name`
- `idx_organisation_is_active` on `is_active`

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 75-88)

---

### 2. individual

**Purpose**: User/individual master data

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(255) | NOT NULL | Individual's full name |
| email | VARCHAR(100) | UNIQUE | Email address |
| mobile_number | VARCHAR(100) | NULL | Mobile number |
| password | VARCHAR(100) | NULL | Hashed password |
| social_login_provider | VARCHAR(50) | NULL | OAuth provider (Google, Facebook, etc.) |
| social_login_provider_id | VARCHAR(255) | NULL | OAuth provider user ID |
| social_login_provider_image_url | VARCHAR(255) | NULL | Profile image URL from OAuth |
| meta_data | JSONB | NULL | Flexible metadata (JSONB) |
| is_active | BOOLEAN | DEFAULT true | Soft delete flag |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| created_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

**Indexes**:
- `idx_individual_email` on `email`
- `idx_individual_is_active` on `is_active`

**Constraints**:
- `individual_email_unique` UNIQUE constraint on `email`

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 8-27)

---

### 3. roles

**Purpose**: Role definitions with hierarchical permissions

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Role name |
| description | VARCHAR(255) | NULL | Role description |
| parent_role_id | UUID | NULL, FK → roles(id) | Parent role for inheritance |
| is_active | BOOLEAN | DEFAULT true | Soft delete flag |
| created_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| permissions | JSONB | NULL | Permission tree (JSONB) |
| org_id | UUID | NULL | Organisation ID (logical reference) |
| pages | JSONB | NULL | Page-level permissions (JSONB) |

**Indexes**:
- `idx_roles_org_id` on `org_id`
- `idx_roles_parent_role_id` on `parent_role_id`

**Foreign Keys**:
- `roles_parent_role_fk`: parent_role_id → roles(id)

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 103-117)

---

### 4. resource

**Purpose**: Resource hierarchy for permission management

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Resource name |
| description | VARCHAR(255) | NULL | Resource description |
| type | VARCHAR(50) | NOT NULL | Resource type (PAGE, API, FEATURE, etc.) |
| parent_resource_id | UUID | NULL, FK → resource(id) | Parent resource for hierarchy |
| validations | JSONB | NULL | Validation rules (JSONB) |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | Soft delete flag |
| created_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |

**Indexes**:
- `idx_resource_parent_id` on `parent_resource_id`
- `idx_resource_type` on `type`

**Foreign Keys**:
- `resource_parent_resource_fk`: parent_resource_id → resource(id)

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 89-102)

---

### 5. individual_role

**Purpose**: Role assignments (junction table)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| individual_id | UUID | NOT NULL, FK → individual(id) | Individual ID |
| role_id | UUID | NOT NULL, FK → roles(id) | Role ID |
| created_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| org_id | UUID | NULL | Organisation ID (logical reference) |

**Indexes**:
- `idx_individual_role_individual_id` on `individual_id`
- `idx_individual_role_role_id` on `role_id`
- `idx_individual_role_org_id` on `org_id`

**Foreign Keys**:
- `individual_role_individual_id_fk`: individual_id → individual(id)
- `individual_role_role_id_fk`: role_id → roles(id)

**Source**: `db.changelog-202504181434-donation-receipt-derived-tables.xml` (lines 52-65)

---

### 6. individual_permission

**Purpose**: Individual-specific permission overrides

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| individual_id | UUID | NOT NULL, FK → individual(id) | Individual ID |
| permissions | JSONB | NOT NULL | Permission tree (JSONB) |
| created_on | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Creation timestamp |
| created_by | UUID | NULL | Creator user ID |
| updated_on | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |
| updated_by | UUID | NULL | Last updater user ID |
| remarks | VARCHAR(512) | NULL | Notes/comments |
| org_id | UUID | NULL | Organisation ID (logical reference) |
| pages | JSONB | NULL | Page-level permissions (JSONB) |

**Indexes**:
- `idx_individual_permission_individual_id` on `individual_id`
- `idx_individual_permission_org_id` on `org_id`

**Foreign Keys**:
- `individual_permission_individual_fk`: individual_id → individual(id)

**Source**: `db.changelog-202504181434-donation-receipt-derived-tables.xml` (lines 38-51)

---

### 7. individual_password_reset_audit

**Purpose**: Password reset request tracking

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| individual_id | UUID | NOT NULL, FK → individual(id) | Individual ID |
| email | VARCHAR(200) | NOT NULL | Email address |
| reset_link | VARCHAR(200) | NULL | Password reset link |
| reset_link_requested_at | TIMESTAMP | NULL | Request timestamp |
| reset_link_expires_at | TIMESTAMP | NULL | Expiration timestamp |
| reset_completed_at | TIMESTAMP | NULL | Completion timestamp |
| reset_status | VARCHAR(50) | NULL | Status (PENDING, COMPLETED, EXPIRED) |
| is_active | BOOLEAN | NULL | Active flag |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |

**Indexes**:
- `idx_password_reset_individual_id` on `individual_id`
- `idx_password_reset_status` on `reset_status`

**Foreign Keys**:
- `individual_password_reset_audit_individual_id_fkey`: individual_id → individual(id)

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 29-45)

---

### 8. individual_verification_audit

**Purpose**: Email/mobile verification tracking

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| contact_type | VARCHAR(50) | NOT NULL | Contact type (EMAIL, MOBILE) |
| contact_value | VARCHAR(255) | NOT NULL | Email or mobile number |
| activation_link | BOOLEAN | DEFAULT false | Whether activation link was sent |
| activation_link_created_at | TIMESTAMP | NULL | Link creation timestamp |
| activation_link_expires_at | TIMESTAMP | NULL | Link expiration timestamp |
| activation_link_verified_at | TIMESTAMP | NULL | Verification timestamp |
| ip_address | VARCHAR(40) | NULL | IP address of request |
| role | VARCHAR(40) | NULL | Role context |
| verification_status | VARCHAR(50) | NULL | Status (PENDING, VERIFIED, EXPIRED) |
| is_active | BOOLEAN | NULL | Active flag |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| created_on | TIMESTAMP | NULL | Creation timestamp |
| updated_on | TIMESTAMP | NULL | Last update timestamp |
| otp | VARCHAR(10) | NULL | One-time password |
| meta_data | JSONB | NULL | Additional metadata (JSONB) |
| org_id | UUID | NULL | Organisation ID (logical reference) |

**Indexes**:
- `idx_verification_contact_value` on `contact_value`
- `idx_verification_status` on `verification_status`

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 47-64)

---

### 9. individual_sessions

**Purpose**: Active session management (JWT tokens)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| individual_id | UUID | NOT NULL, FK → individual(id) | Individual ID |
| accesstoken | VARCHAR | NOT NULL | JWT access token |
| accesstoken_expirytime | TIMESTAMP | NOT NULL | Access token expiration |
| accesstoken_generated_on | TIMESTAMP | NOT NULL | Access token generation time |
| refreshtoken | VARCHAR | NOT NULL | JWT refresh token |
| refreshtoken_expirytime | TIMESTAMP | NOT NULL | Refresh token expiration |
| refreshtoken_generated_on | TIMESTAMP | NOT NULL | Refresh token generation time |
| ipaddress | VARCHAR | NULL | IP address of session |

**Indexes**:
- `idx_sessions_individual_id` on `individual_id`
- `idx_sessions_accesstoken` on `accesstoken`

**Foreign Keys**:
- `individual_sessions_ind_fk`: individual_id → individual(id)

**Source**: `db.changelog-202504181434-donation-receipt-derived-tables.xml` (lines 20-37)

---

### 10. list_names

**Purpose**: Dropdown list names (master table)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(100) | UNIQUE | List name |
| is_active | BOOLEAN | DEFAULT true | Soft delete flag |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| created_on | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL | Last update timestamp |
| is_statistics | BOOLEAN | NULL | Whether used for statistics |

**Indexes**:
- `idx_list_names_name` on `name`

**Constraints**:
- `list_names_name_key` UNIQUE constraint on `name`

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 66-74)

---

### 11. list_values

**Purpose**: Dropdown list values (detail table)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY, NOT NULL | Unique identifier |
| name | VARCHAR(255) | NULL | Value name |
| list_names_id | UUID | NOT NULL, FK → list_names(id) | Parent list name ID |
| is_active | BOOLEAN | DEFAULT true | Soft delete flag |
| created_by | UUID | NULL | Creator user ID |
| updated_by | UUID | NULL | Last updater user ID |
| created_on | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_on | TIMESTAMP | NOT NULL | Last update timestamp |
| code | VARCHAR(50) | NULL | Value code |

**Indexes**:
- `idx_list_values_list_names_id` on `list_names_id`

**Foreign Keys**:
- `fk_list_names`: list_names_id → list_names(id)

**Source**: `db.changelog-202504181155-donation-receipt-core-tables.xml` (lines 76-87)

---

## Common Patterns

### Audit Columns

All tables include standard audit columns:
- `created_by` (UUID) - User who created the record
- `created_on` (TIMESTAMP) - Creation timestamp
- `updated_by` (UUID) - User who last updated the record
- `updated_on` (TIMESTAMP) - Last update timestamp

### Soft Delete

Most tables use the `is_active` (BOOLEAN) flag for soft deletes instead of hard deletes.

### JSONB Columns

Several tables use PostgreSQL's JSONB type for flexible data storage:
- `organisation.meta_data` - Organisation metadata
- `organisation.documents` - Organisation documents (replaces need for document_repo table)
- `individual.meta_data` - Individual metadata (can include profile documents)
- `roles.permissions` - Permission tree structure
- `roles.pages` - Page-level permissions
- `resource.validations` - Validation rules
- `individual_permission.permissions` - Permission overrides
- `individual_permission.pages` - Page-level permission overrides
- `individual_verification_audit.meta_data` - Verification metadata

### UUID Primary Keys

All tables use UUID as the primary key type for:
- Global uniqueness
- No sequential ID leakage
- Distributed system compatibility
- Easy cross-module references

---

## Indexes Summary

### Performance Indexes

**File**: `db.changelog-1.3-create-indexes.xml`

```sql
-- Individual indexes
CREATE INDEX idx_individual_email ON individual(email);
CREATE INDEX idx_individual_is_active ON individual(is_active);

-- Organisation indexes
CREATE INDEX idx_organisation_name ON organisation(name);
CREATE INDEX idx_organisation_is_active ON organisation(is_active);

-- Roles indexes
CREATE INDEX idx_roles_org_id ON roles(org_id);
CREATE INDEX idx_roles_parent_role_id ON roles(parent_role_id);

-- Resource indexes
CREATE INDEX idx_resource_parent_id ON resource(parent_resource_id);
CREATE INDEX idx_resource_type ON resource(type);

-- Individual Role indexes
CREATE INDEX idx_individual_role_individual_id ON individual_role(individual_id);
CREATE INDEX idx_individual_role_role_id ON individual_role(role_id);
CREATE INDEX idx_individual_role_org_id ON individual_role(org_id);

-- Individual Permission indexes
CREATE INDEX idx_individual_permission_individual_id ON individual_permission(individual_id);
CREATE INDEX idx_individual_permission_org_id ON individual_permission(org_id);

-- Session indexes
CREATE INDEX idx_sessions_individual_id ON individual_sessions(individual_id);
CREATE INDEX idx_sessions_accesstoken ON individual_sessions(accesstoken);

-- Audit indexes
CREATE INDEX idx_password_reset_individual_id ON individual_password_reset_audit(individual_id);
CREATE INDEX idx_password_reset_status ON individual_password_reset_audit(reset_status);
CREATE INDEX idx_verification_contact_value ON individual_verification_audit(contact_value);
CREATE INDEX idx_verification_status ON individual_verification_audit(verification_status);

-- Supporting table indexes
CREATE INDEX idx_list_names_name ON list_names(name);
CREATE INDEX idx_list_values_list_names_id ON list_values(list_names_id);
```

---

## Migration Commands

```bash
# Run all migrations
./gradlew update

# Rollback last changeset
./gradlew rollbackCount -PliquibaseCommandValue=1

# Validate changelog
./gradlew validate

# Generate JOOQ classes (after migrations)
./gradlew generateJooq
```

---

## Next Steps

1. Create the Liquibase changelog files based on this schema
2. Run migrations to create all 13 tables
3. Generate JOOQ code for type-safe database access
4. Implement custom repository layer
5. Write integration tests with Testcontainers

---

**Schema extracted from**: `pure-heart-backend` reference codebase
**Last updated**: 2026-03-01


