# Getting Started - User Entitlement Management Module

## 📋 Documentation Overview

Welcome to the **User Entitlement Management** module implementation guide! This directory contains all the planning and design documentation needed to implement a comprehensive RBAC (Role-Based Access Control) system using Spring Modulith.

---

## 📚 Documentation Files

### 1. **[README.md](README.md)** - Start Here!
**Purpose**: High-level overview of the module  
**Contents**:
- Key features and capabilities
- Architecture overview
- Technology stack
- API endpoint summary
- Development plan overview
- Testing strategy
- Getting started guide

**When to read**: First document to read for understanding the overall scope and capabilities.

---

### 2. **[PLANNING.md](PLANNING.md)** - Design Document
**Purpose**: Detailed planning and design specifications  
**Contents**:
- Reference codebase analysis (`pure-heart-backend`)
- Data model (entities and relationships)
- Module boundaries (Spring Modulith structure)
- Complete package structure
- API endpoint specifications (Admin + User)
- Security considerations
- Development iterations overview

**When to read**: Before starting implementation to understand the design decisions and architecture.

---

### 3. **[ITERATIONS.md](ITERATIONS.md)** - Implementation Roadmap
**Purpose**: Step-by-step development guide with TDD approach  
**Contents**:
- **Iteration 1**: Database schema & core entities (2-3 days)
- **Iteration 2**: Service layer - Organisation & Individual (2 days)
- **Iteration 3**: Service layer - Role & Resource (2 days)
- **Iteration 4**: Service layer - Role & Permission Assignment (2 days)
- **Iteration 5**: Controller layer - Admin APIs (3 days)
- **Iteration 6**: Controller layer - User APIs & Security (2 days)
- Detailed test cases for each iteration
- Acceptance criteria for each iteration

**When to read**: During implementation to follow the TDD approach and track progress.

---

### 4. **[API_SPECIFICATION.md](API_SPECIFICATION.md)** - API Reference
**Purpose**: Complete REST API documentation  
**Contents**:
- **31 Admin endpoints** (Organisation, Individual, Role, Resource, Assignments)
- **4 User endpoints** (My entitlements, roles, permissions, resources)
- Request/response examples with JSON
- Error response formats
- Pagination, sorting, and filtering
- HTTP status codes
- Authentication and authorization requirements

**When to read**: When implementing controllers or writing API tests.

---

### 5. **[ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md)** - Visual Reference
**Purpose**: Visual diagrams and architecture illustrations  
**Contents**:
- Entity Relationship Diagram (ERD)
- Module structure (Spring Modulith)
- Request flow diagrams
- Permission calculation flow
- API layer architecture
- Testing pyramid
- Security flow
- Deployment architecture (future)

**When to read**: For visual understanding of relationships and flows.

---

## 🚀 Quick Start Guide

### Step 1: Read the Documentation (30 minutes)
1. Start with **README.md** for overview
2. Read **PLANNING.md** for design details
3. Skim **ITERATIONS.md** to understand the roadmap
4. Bookmark **API_SPECIFICATION.md** for reference

### Step 2: Set Up Your Environment
```bash
# Ensure you have the prerequisites
java -version    # Should be Java 17+
psql --version   # Should be PostgreSQL 15+
docker --version # For Testcontainers

# Navigate to the project
cd /Users/chidhagnidev/Documents/codebase/modular-app

# Verify tests work
./gradlew unitTest
./gradlew apiTest
./gradlew integrationTest
```

### Step 3: Start Iteration 1 (Database Schema & JOOQ)
```bash
# Create a feature branch
git checkout -b feature/entitlement-iteration-1

# Follow the steps in ITERATIONS.md for Iteration 1:
# 1. Create Liquibase master changelog: db.changelog-master.yaml (YAML format)
# 2. Create individual migration files: db.changelog-*.xml (XML format)
#    - db.changelog-1.0-create-core-tables.xml (6 core tables)
#    - db.changelog-1.1-create-audit-tables.xml (3 audit tables)
#    - db.changelog-1.2-create-supporting-tables.xml (4 supporting tables)
#    - db.changelog-1.3-create-indexes.xml (performance indexes)
# 3. Configure JOOQ code generation in build.gradle
# 4. Run Liquibase migrations: ./gradlew update
# 5. Generate JOOQ classes: ./gradlew generateJooq
# 6. Create custom repository layer on top of JOOQ DAOs
# 7. Write integration tests (*ITest.java)
# 8. Run tests and verify they pass
```

### Step 4: Follow TDD Approach
For each iteration:
1. **Write tests first** (Unit, API, or Integration)
2. **Run tests** - they should fail (Red)
3. **Implement feature** to make tests pass (Green)
4. **Refactor** code while keeping tests green
5. **Commit** changes with meaningful messages

---

## 📊 Development Timeline

| Iteration | Focus | Duration | Deliverables |
|-----------|-------|----------|--------------|
| **1** | Database schema & JOOQ | 2-3 days | Liquibase migrations (11 tables), JOOQ code generation, custom repositories (no tests) |
| **2** | Service: Org & Individual | 2 days | Services, DTOs, mappers, unit tests, integration tests |
| **3** | Service: Role & Resource | 2 days | Hierarchical structures, permission logic, unit tests, integration tests |
| **4** | Service: Assignments | 2 days | Role assignment, permission overrides, unit tests, integration tests |
| **5** | Controller: Admin APIs | 3 days | 31 admin endpoints, API tests |
| **6** | Controller: User APIs & Security | 2 days | 4 user endpoints, authorization, integration tests |

**Total**: 13-14 days

---

## 🧪 Testing Strategy

### Test Types
- **Unit Tests** (`*UTest.java`): Business logic, mocked dependencies, 90% coverage target
- **API Tests** (`*ApiTest.java`): Controller endpoints, MockMvc, 80% coverage target
- **Integration Tests** (`*ITest.java`): Full stack + PostgreSQL, 85% coverage target

### Running Tests
```bash
# Run all unit tests
./gradlew unitTest

# Run all API tests
./gradlew apiTest

# Run all integration tests
./gradlew integrationTest

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "OrganisationServiceUTest"
```

---

## 🔑 Key Concepts

### 1. **Spring Modulith (Modular Monolith Architecture)**
- **Current Phase**: Modular monolith with strong module boundaries
- **Future Ready**: Designed for easy microservices extraction
- **Module Independence**: Each module can theoretically be deployed separately
- **Event-Driven**: Modules communicate via Spring Modulith events (not direct calls)
- **Package Encapsulation**: Internal implementation is package-private
- **No Cross-Module DB Access**: Each module owns its tables

**Why Modular Monolith?**
- Simpler deployment and operations (single JAR)
- Faster development and debugging
- Lower infrastructure costs
- ACID transactions within modules
- Easy to extract into microservices when needed

**Design Principles for Microservices Transition**:
- ✅ No direct method calls between modules
- ✅ Event-driven communication only
- ✅ DTOs for inter-module data exchange
- ✅ No foreign keys across module boundaries
- ✅ Idempotent event handlers
- ✅ Eventual consistency mindset

### 2. **JOOQ (Java Object Oriented Querying)**
- Type-safe SQL query construction
- Code generation from database schema
- POJOs, DAOs, and Records auto-generated
- Custom converters for JSONB columns
- No runtime reflection (compile-time safety)
- Fluent API for complex queries

### 3. **RBAC (Role-Based Access Control)**
- **Roles**: Named permission sets (e.g., "Content Manager")
- **Permissions**: Hierarchical tree structure (JSONB)
- **Permission Bits**: Read (1), Write (2), Update (4), Delete (8)
- **Inheritance**: Roles can have parent roles
- **Overrides**: Individual-specific permission customization

### 4. **Multi-Tenancy**
- **Organisation**: Top-level tenant
- **Roles**: Scoped to organisations
- **Assignments**: Individual + Role + Organisation

---

## 📖 Reference Codebase

The design is based on patterns from `pure-heart-backend`:
- **Location**: `/Users/chidhagnidev/Documents/codebase/pure-heart-backend`
- **Key Files**:
  - `src/main/java/com/pureheart/backend/donors/` (example domain module)
  - `src/main/java/com/pureheart/backend/programs/` (example with handlers)
  - `docs/backend-code-guidelines.md` (coding standards)

---

## ❓ FAQ

**Q: Do I need to modify the reference codebase (`pure-heart-backend`)?**  
A: No, it's read-only for reference. All implementation happens in `modular-app`.

**Q: Can I skip iterations?**  
A: No, each iteration builds on the previous one. Follow the sequence.

**Q: What if I find issues in the design?**  
A: Document them and discuss with the team. Update the planning docs if needed.

**Q: How do I handle JSONB columns with JOOQ?**
A: Use JOOQ's `forcedType` configuration in `build.gradle` with custom converters. See `ITERATIONS.md` for detailed examples of `RoleNodeListConverter` and `JsonbConverter`.

**Q: Why use Liquibase instead of Flyway?**
A: Liquibase provides more flexibility with XML/YAML/SQL formats, better rollback capabilities, and is the standard used in the reference codebase (`pure-heart-backend`).

**Q: How do modules communicate in a modular monolith?**
A: Modules communicate via Spring Modulith application events. Use `ApplicationEventPublisher` to publish events and `@ApplicationModuleListener` to subscribe. Never call methods directly across module boundaries.

**Q: Can this be converted to microservices later?**
A: Yes! The architecture is designed for easy microservices extraction. Each module has clear boundaries, event-driven communication, and no cross-module database dependencies. When ready, simply extract a module, replace Spring events with Kafka/RabbitMQ, and separate the database.

---

## 🎯 Success Criteria

Before considering the module complete:
- ✅ All 6 iterations completed
- ✅ All tests passing (unit, API, integration)
- ✅ Test coverage: 85%+ overall
- ✅ All 35 API endpoints working
- ✅ Security implemented (JWT + role-based authorization)
- ✅ Documentation updated (JavaDoc, README)
- ✅ Code review completed
- ✅ Integration tests with Testcontainers passing

---

## 📞 Support

For questions or issues:
1. Check the documentation in this directory
2. Review the reference codebase (`pure-heart-backend`)
3. Consult with the team

---

## 🎉 Ready to Start?

1. Read **README.md** for overview
2. Study **PLANNING.md** for design
3. Open **ITERATIONS.md** and start with Iteration 1
4. Use **API_SPECIFICATION.md** as reference
5. Refer to **ARCHITECTURE_DIAGRAM.md** for visual guidance

**Happy coding!** 🚀

