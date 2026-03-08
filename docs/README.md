# Modular App Documentation

Welcome to the Modular App documentation! This directory contains comprehensive guides for developers and users.

## 📚 Documentation Index

### API Documentation
- **[Swagger Quick Start](./SWAGGER_QUICK_START.md)** - Get started with Swagger UI in 2 minutes
- **[Swagger API Documentation](./SWAGGER_API_DOCUMENTATION.md)** - Complete Swagger/OpenAPI guide
- **[API Test Security Guide](./testing/API_TEST_SECURITY_GUIDE.md)** - How to write API tests with JWT security

### Architecture & Design
- **Spring Modulith** - Modular monolith architecture
- **JOOQ** - Type-safe SQL queries
- **Liquibase** - Database migrations
- **PostgreSQL** - Production database
- **Testcontainers** - Integration testing

### Security
- **JWT Authentication** - Token-based authentication
- **Role-Based Access Control (RBAC)** - Permission management
- **Spring Security** - Security framework integration

## 🚀 Quick Links

### For Developers
1. [API Test Security Guide](./testing/API_TEST_SECURITY_GUIDE.md) - Write tests with real JWT authentication
2. [Swagger Documentation](./SWAGGER_API_DOCUMENTATION.md) - Add API documentation to controllers

### For API Users
1. [Swagger Quick Start](./SWAGGER_QUICK_START.md) - Test APIs interactively
2. [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API documentation (when app is running)

## 🏗️ Project Structure

```
modular-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ganithyanthram/modularapp/
│   │   │       ├── config/           # Configuration classes
│   │   │       ├── security/         # Security & authentication
│   │   │       └── entitlement/      # Business modules
│   │   │           ├── individual/   # User management
│   │   │           ├── organisation/ # Organization management
│   │   │           ├── role/         # Role management
│   │   │           ├── resource/     # Resource management
│   │   │           ├── assignment/   # Role & permission assignment
│   │   │           └── user/         # User-facing APIs
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/
│   │           └── changelog/        # Liquibase migrations
│   └── test/
│       └── java/                     # Tests (unit, integration, API)
├── docs/                             # Documentation (you are here)
└── build.gradle                      # Build configuration
```

## 🔧 Key Technologies

| Technology | Purpose | Documentation |
|------------|---------|---------------|
| **Spring Boot 4.0** | Application framework | [Spring Docs](https://spring.io/projects/spring-boot) |
| **Spring Modulith** | Modular architecture | [Modulith Docs](https://spring.io/projects/spring-modulith) |
| **Spring Security** | Authentication & authorization | [Security Docs](https://spring.io/projects/spring-security) |
| **JOOQ 3.19** | Type-safe SQL | [JOOQ Docs](https://www.jooq.org/) |
| **Liquibase** | Database migrations | [Liquibase Docs](https://www.liquibase.org/) |
| **PostgreSQL** | Database | [PostgreSQL Docs](https://www.postgresql.org/) |
| **JWT (jjwt)** | Token authentication | [JJWT Docs](https://github.com/jwtk/jjwt) |
| **SpringDoc OpenAPI** | API documentation | [SpringDoc Docs](https://springdoc.org/) |
| **Testcontainers** | Integration testing | [Testcontainers Docs](https://www.testcontainers.org/) |
| **JUnit 5** | Testing framework | [JUnit Docs](https://junit.org/junit5/) |
| **Mockito** | Mocking framework | [Mockito Docs](https://site.mockito.org/) |

## 📖 API Endpoints Overview

### Public Endpoints (No Auth)
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token
- `/swagger-ui.html` - API documentation
- `/v3/api-docs` - OpenAPI specification

### Admin Endpoints (Auth Required)
- `/api/v1/admin/individuals/**` - User management
- `/api/v1/admin/organisations/**` - Organization management
- `/api/v1/admin/roles/**` - Role management
- `/api/v1/admin/resources/**` - Resource management
- `/api/v1/admin/assignments/**` - Role & permission assignment

### User Endpoints (Auth Required)
- `/api/v1/user/entitlements/**` - View own entitlements
- `POST /api/v1/auth/logout` - Logout

## 🧪 Testing

### Run Tests
```bash
# All tests
./gradlew test

# Unit tests only
./gradlew unitTest

# Integration tests only
./gradlew integrationTest

# API tests only
./gradlew apiTest
```

### Test Coverage
```bash
# Generate coverage report
./gradlew jacocoTestReport

# View report
open build/reports/jacoco/test/index.html
```

## 🔐 Security Features

- **JWT Authentication** - Stateless token-based auth
- **BCrypt Password Hashing** - Secure password storage
- **Role-Based Access Control** - Fine-grained permissions
- **Permission Inheritance** - Hierarchical role structure
- **Permission Overrides** - Individual permission customization
- **Token Refresh** - Long-lived sessions with refresh tokens
- **Stateless Sessions** - No server-side session storage

## 🎯 Core Concepts

### Entitlement System
The application uses a comprehensive entitlement system:

1. **Individuals** - Users in the system
2. **Organizations** - Tenant isolation
3. **Roles** - Collections of permissions
4. **Resources** - Hierarchical structure (menu → page → action)
5. **Permissions** - CRUD operations on resources
6. **Role Assignment** - Link users to roles
7. **Permission Overrides** - Fine-tune individual permissions

### Permission Model
Permissions are represented as 4-bit integers:
- **Bit 0 (1)**: Create
- **Bit 1 (2)**: Read
- **Bit 2 (4)**: Update
- **Bit 3 (8)**: Delete

Example: `15` = 1111 in binary = Full CRUD access

## 🚦 Getting Started

1. **Start the application**
   ```bash
   ./gradlew bootRun
   ```

2. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Login and test APIs**
   - See [Swagger Quick Start](./SWAGGER_QUICK_START.md)

## 📝 Contributing

When adding new features:

1. **Add Swagger annotations** to controllers
2. **Write API tests** with JWT authentication
3. **Update documentation** as needed
4. **Run tests** before committing
5. **Follow the established patterns**

## 🐛 Troubleshooting

### Common Issues

**Swagger UI not loading**
- Check application is running
- Verify security config allows `/swagger-ui/**`
- Clear browser cache

**Tests failing**
- Ensure Docker is running (for integration tests)
- Check Testcontainers configuration
- Run `./gradlew clean test`

**JWT authentication errors**
- Verify token is not expired
- Check token format: `Bearer <token>`
- Ensure user exists in database

## 📞 Support

For questions or issues:
- Check existing documentation
- Review Swagger UI for API details
- Consult test examples for usage patterns

---

**Last Updated**: March 2026  
**Version**: 1.0.0
