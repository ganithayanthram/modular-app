# Entitlement Management - Quick Reference Guide

## Table of Contents
1. [Permission Bits](#permission-bits)
2. [Common Operations](#common-operations)
3. [API Quick Reference](#api-quick-reference)
4. [Code Examples](#code-examples)
5. [Troubleshooting](#troubleshooting)

---

## Permission Bits

### Binary Representation
```
Bit Position:  3    2    1    0
Permission:    D    U    C    R
Binary:        1    1    1    1  = 15 (CRUD)
```

### Common Permission Values
| Decimal | Binary | Permissions | Description |
|---------|--------|-------------|-------------|
| 0       | 0000   | ----        | No access |
| 1       | 0001   | R---        | Read only |
| 2       | 0010   | -C--        | Create only |
| 3       | 0011   | RC--        | Read + Create |
| 4       | 0100   | --U-        | Update only |
| 5       | 0101   | R-U-        | Read + Update |
| 7       | 0111   | RCU-        | Read + Create + Update |
| 8       | 1000   | ---D        | Delete only |
| 9       | 1001   | R--D        | Read + Delete |
| 15      | 1111   | RCUD        | Full CRUD |

### Checking Permissions in Code
```java
int permissions = 7; // RCU-

boolean canRead = (permissions & 1) != 0;   // true
boolean canCreate = (permissions & 2) != 0; // true
boolean canUpdate = (permissions & 4) != 0; // true
boolean canDelete = (permissions & 8) != 0; // false
```

---

## Common Operations

### 1. Create Organisation and Admin User
```bash
# Step 1: Create Organisation
curl -X POST http://localhost:8080/api/v1/admin/organisations \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corp",
    "description": "Main organisation"
  }'
# Response: {"id": "org-uuid"}

# Step 2: Create Admin User
curl -X POST http://localhost:8080/api/v1/admin/individuals \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "admin@acme.com",
    "password": "SecurePass123!",
    "orgId": "org-uuid"
  }'
# Response: {"id": "user-uuid"}
```

### 2. Create Role and Assign to User
```bash
# Step 1: Create Role
curl -X POST http://localhost:8080/api/v1/admin/roles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Manager",
    "description": "Team manager role",
    "orgId": "org-uuid",
    "permissions": [
      {
        "name": "users",
        "type": "resource",
        "permissions": 15
      }
    ]
  }'
# Response: {"id": "role-uuid"}

# Step 2: Assign Role to User
curl -X POST http://localhost:8080/api/v1/admin/assignments/roles \
  -H "Content-Type: application/json" \
  -d '{
    "individualId": "user-uuid",
    "roleId": "role-uuid",
    "orgId": "org-uuid"
  }'
```

### 3. Create Hierarchical Roles
```bash
# Step 1: Create Parent Role
curl -X POST http://localhost:8080/api/v1/admin/roles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Manager",
    "orgId": "org-uuid",
    "permissions": [
      {"name": "users", "permissions": 15}
    ]
  }'
# Response: {"id": "parent-role-uuid"}

# Step 2: Create Child Role (inherits from parent)
curl -X POST http://localhost:8080/api/v1/admin/roles \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Team Lead",
    "orgId": "org-uuid",
    "parentRoleId": "parent-role-uuid",
    "permissions": [
      {"name": "reports", "permissions": 1}
    ]
  }'
# Team Lead will have both "users" (15) and "reports" (1) permissions
```

### 4. Override Individual Permissions
```bash
curl -X POST http://localhost:8080/api/v1/admin/assignments/permissions \
  -H "Content-Type: application/json" \
  -d '{
    "individualId": "user-uuid",
    "orgId": "org-uuid",
    "permissions": [
      {
        "name": "special-reports",
        "permissions": 3
      }
    ],
    "remarks": "Temporary access for Q1"
  }'
```

---

## API Quick Reference

### Organisation APIs
```bash
# Create
POST /api/v1/admin/organisations
Body: {"name": "...", "description": "..."}

# Get by ID
GET /api/v1/admin/organisations/{id}

# List (with pagination)
GET /api/v1/admin/organisations?page=0&size=20&search=acme

# Update
PUT /api/v1/admin/organisations/{id}
Body: {"name": "...", "description": "..."}

# Delete (soft)
DELETE /api/v1/admin/organisations/{id}

# Activate/Deactivate
PATCH /api/v1/admin/organisations/{id}/activate
PATCH /api/v1/admin/organisations/{id}/deactivate
```

### Individual APIs
```bash
# Create
POST /api/v1/admin/individuals
Body: {"name": "...", "email": "...", "password": "...", "orgId": "..."}

# Get by ID
GET /api/v1/admin/individuals/{id}

# List (with pagination)
GET /api/v1/admin/individuals?page=0&size=20&search=john

# Update (no password field)
PUT /api/v1/admin/individuals/{id}
Body: {"name": "...", "email": "...", "orgId": "..."}

# Delete (soft)
DELETE /api/v1/admin/individuals/{id}

# Activate/Deactivate
PATCH /api/v1/admin/individuals/{id}/activate
PATCH /api/v1/admin/individuals/{id}/deactivate
```

### Role APIs
```bash
# Create
POST /api/v1/admin/roles
Body: {"name": "...", "orgId": "...", "permissions": [...]}

# Get by ID
GET /api/v1/admin/roles/{id}

# List (with pagination and org filter)
GET /api/v1/admin/roles?page=0&size=20&orgId=...

# Get effective permissions (includes parent)
GET /api/v1/admin/roles/{id}/effective-permissions

# Update
PUT /api/v1/admin/roles/{id}

# Delete (soft)
DELETE /api/v1/admin/roles/{id}
```

### Resource APIs
```bash
# Create
POST /api/v1/admin/resources
Body: {"name": "...", "type": "...", "parentResourceId": "..."}

# Get hierarchy (tree structure)
GET /api/v1/admin/resources/hierarchy

# List (with pagination and type filter)
GET /api/v1/admin/resources?page=0&size=20&type=menu

# Update
PUT /api/v1/admin/resources/{id}

# Delete (soft)
DELETE /api/v1/admin/resources/{id}
```

### Assignment APIs
```bash
# Assign role
POST /api/v1/admin/assignments/roles
Body: {"individualId": "...", "roleId": "...", "orgId": "..."}

# Revoke role
DELETE /api/v1/admin/assignments/roles/{individualId}/{roleId}

# Get roles for individual
GET /api/v1/admin/assignments/individuals/{id}/roles?orgId=...

# Get individuals with role
GET /api/v1/admin/assignments/roles/{id}/individuals

# Override permissions
POST /api/v1/admin/assignments/permissions
Body: {"individualId": "...", "orgId": "...", "permissions": [...]}

# Get effective permissions
GET /api/v1/admin/assignments/individuals/{id}/permissions?orgId=...
```

### User APIs
```bash
# Get my entitlements
GET /api/v1/user/entitlements

# Get my permissions for org
GET /api/v1/user/entitlements/permissions?orgId=...

# Get resource hierarchy
GET /api/v1/user/entitlements/resources

# Check permission
GET /api/v1/user/entitlements/check?orgId=...&resource=users&action=create
```

---

## Code Examples

### 1. Service Layer - Create Role
```java
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    
    @Transactional
    public UUID createRole(CreateRoleRequest request, UUID userId) {
        // Validate unique name
        if (roleRepository.existsByNameAndOrgIdAndIsActiveTrue(
                request.getName(), request.getOrgId())) {
            throw new DuplicateRoleException("Role already exists");
        }
        
        // Create entity
        Roles role = roleMapper.toEntity(request, userId);
        
        // Save
        return roleRepository.create(role);
    }
}
```

### 2. Controller - Handle Request
```java
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @PostMapping
    public ResponseEntity<Map<String, UUID>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        
        UUID userId = UUID.randomUUID(); // TODO: Get from @CurrentUser
        UUID id = roleService.createRole(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id));
    }
}
```

### 3. Repository - JOOQ Query
```java
@Repository
@RequiredArgsConstructor
public class RoleRepository {
    
    private final DSLContext dsl;
    
    public Optional<Roles> findByIdAndIsActiveTrue(UUID id) {
        return dsl.selectFrom(ROLES)
                .where(ROLES.ID.eq(id))
                .and(ROLES.IS_ACTIVE.isTrue())
                .fetchOptionalInto(Roles.class);
    }
}
```

### 4. Permission Calculation
```java
// Merge parent and child permissions
private List<RoleNode> mergePermissions(
        List<RoleNode> parent, List<RoleNode> child) {
    
    Map<String, RoleNode> merged = new HashMap<>();
    
    // Add parent permissions
    if (parent != null) {
        parent.forEach(node -> merged.put(node.getName(), node));
    }
    
    // Override with child permissions
    if (child != null) {
        child.forEach(node -> {
            if (merged.containsKey(node.getName())) {
                RoleNode existing = merged.get(node.getName());
                // Bitwise OR to combine permissions
                int combinedPerms = existing.getPermissions() | node.getPermissions();
                node.setPermissions(combinedPerms);
            }
            merged.put(node.getName(), node);
        });
    }
    
    return new ArrayList<>(merged.values());
}
```

---

## Troubleshooting

### Issue: "Role not found"
**Cause**: Role is soft-deleted (is_active = false)  
**Solution**: Use activate endpoint or check is_active flag

```bash
PATCH /api/v1/admin/roles/{id}/activate
```

### Issue: "Duplicate role name"
**Cause**: Role with same name exists in organisation  
**Solution**: Use different name or update existing role

```bash
# Check existing roles
GET /api/v1/admin/roles?orgId={orgId}
```

### Issue: "Permission calculation returns empty"
**Cause**: No roles assigned or all roles are inactive  
**Solution**: Assign active role to individual

```bash
# Check assigned roles
GET /api/v1/admin/assignments/individuals/{id}/roles
```

### Issue: "JOOQ code not generated"
**Cause**: Database schema not migrated  
**Solution**: Run Liquibase migrations first

```bash
./gradlew update
./gradlew generateJooq
```

### Issue: "Validation error on create"
**Cause**: Missing required fields or invalid format  
**Solution**: Check request body against DTO validation rules

```json
{
  "timestamp": "2026-03-01T19:45:30",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "name": "Role name is required",
    "orgId": "Organisation ID is required"
  }
}
```

---

## Database Queries

### Check Role Hierarchy
```sql
WITH RECURSIVE role_hierarchy AS (
    SELECT id, name, parent_role_id, 0 as level
    FROM roles
    WHERE parent_role_id IS NULL
    
    UNION ALL
    
    SELECT r.id, r.name, r.parent_role_id, rh.level + 1
    FROM roles r
    JOIN role_hierarchy rh ON r.parent_role_id = rh.id
)
SELECT * FROM role_hierarchy ORDER BY level, name;
```

### Check Individual Permissions
```sql
SELECT 
    i.name as individual_name,
    o.name as org_name,
    r.name as role_name,
    r.permissions as role_permissions,
    ip.permissions as override_permissions
FROM individual i
LEFT JOIN individual_role ir ON i.id = ir.individual_id
LEFT JOIN roles r ON ir.role_id = r.id
LEFT JOIN organisation o ON ir.org_id = o.id
LEFT JOIN individual_permission ip ON i.id = ip.individual_id AND ir.org_id = ip.org_id
WHERE i.id = 'user-uuid';
```

### Check Resource Tree
```sql
WITH RECURSIVE resource_tree AS (
    SELECT id, name, parent_resource_id, 0 as level
    FROM resource
    WHERE parent_resource_id IS NULL
    
    UNION ALL
    
    SELECT r.id, r.name, r.parent_resource_id, rt.level + 1
    FROM resource r
    JOIN resource_tree rt ON r.parent_resource_id = rt.id
)
SELECT * FROM resource_tree ORDER BY level, name;
```

---

## Performance Tips

1. **Use Pagination**: Always use page and size parameters
   ```bash
   GET /api/v1/admin/roles?page=0&size=20
   ```

2. **Filter by Organisation**: Reduce result set
   ```bash
   GET /api/v1/admin/roles?orgId={orgId}
   ```

3. **Cache Permissions**: Cache effective permissions per session
   ```java
   @Cacheable(value = "permissions", key = "#individualId + '-' + #orgId")
   public List<RoleNode> getEffectivePermissions(UUID individualId, UUID orgId)
   ```

4. **Use Indexes**: Database indexes are already created
   - Check query execution plans if slow

5. **Batch Operations**: Use batch endpoints when available
   - Future enhancement for bulk operations

---

## Best Practices

1. **Role Naming**: Use descriptive, hierarchical names
   - ✅ Good: "Sales-Manager", "Sales-Representative"
   - ❌ Bad: "Role1", "Role2"

2. **Permission Granularity**: Balance between too fine and too coarse
   - ✅ Good: "users", "users.profile", "users.settings"
   - ❌ Bad: "users.profile.name.first"

3. **Role Hierarchy**: Keep hierarchy shallow (max 3-4 levels)
   - ✅ Good: Admin → Manager → User
   - ❌ Bad: Admin → Super → Manager → Lead → Senior → Junior

4. **Permission Overrides**: Use sparingly, document reason
   ```json
   {
     "remarks": "Temporary access for Q1 audit - expires 2026-04-01"
   }
   ```

5. **Soft Deletes**: Never hard delete, always soft delete
   - Maintains audit trail
   - Can be reactivated if needed

---

**Quick Reference Version**: 1.0.0  
**Last Updated**: March 1, 2026
