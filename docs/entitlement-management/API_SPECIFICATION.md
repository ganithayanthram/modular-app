# User Entitlement Management - API Specification

## Base URL
```
/api/v1
```

## Authentication
All endpoints require authentication via JWT token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

---

## 1. Organisation Management APIs

### 1.1 Create Organisation
**Endpoint**: `POST /admin/organisations`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "Acme Corporation",
  "category": "TECHNOLOGY",
  "metaData": {
    "industry": "Software",
    "size": "LARGE",
    "country": "USA"
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Validation**:
- `name`: Required, 3-100 characters, unique
- `category`: Required
- `metaData`: Optional JSON object

---

### 1.2 Get Organisation by ID
**Endpoint**: `GET /admin/organisations/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Acme Corporation",
  "category": "TECHNOLOGY",
  "metaData": {
    "industry": "Software",
    "size": "LARGE",
    "country": "USA"
  },
  "isActive": true,
  "createdOn": "2024-01-15T10:30:00Z",
  "updatedOn": "2024-01-15T10:30:00Z",
  "createdBy": "admin-user-id",
  "updatedBy": "admin-user-id"
}
```

**Error Responses**:
- `404 Not Found`: Organisation not found

---

### 1.3 List All Organisations
**Endpoint**: `GET /admin/organisations`  
**Authorization**: `ROLE_ADMIN`

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `search` (optional): Search by name

**Response**: `200 OK`
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Acme Corporation",
      "category": "TECHNOLOGY",
      "isActive": true,
      "createdOn": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 1.4 Update Organisation
**Endpoint**: `PUT /admin/organisations/{id}`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "Acme Corp Updated",
  "category": "TECHNOLOGY",
  "metaData": {
    "industry": "Software",
    "size": "ENTERPRISE"
  }
}
```

**Response**: `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Acme Corp Updated",
  "category": "TECHNOLOGY",
  "isActive": true,
  "updatedOn": "2024-01-16T14:20:00Z"
}
```

---

### 1.5 Delete Organisation (Soft Delete)
**Endpoint**: `DELETE /admin/organisations/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `204 No Content`

---

### 1.6 Activate Organisation
**Endpoint**: `PATCH /admin/organisations/{id}/activate`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`

---

### 1.7 Deactivate Organisation
**Endpoint**: `PATCH /admin/organisations/{id}/deactivate`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`

---

## 2. Individual Management APIs

### 2.1 Create Individual
**Endpoint**: `POST /admin/individuals`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "password": "SecurePassword123!",
  "metaData": {
    "department": "Engineering",
    "title": "Senior Developer"
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001"
}
```

**Validation**:
- `name`: Required, 3-100 characters
- `email`: Required, valid email format, unique
- `mobileNumber`: Optional, valid phone format
- `password`: Required, min 8 characters, hashed before storage

---

### 2.2 Get Individual by ID
**Endpoint**: `GET /admin/individuals/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "mobileNumber": "+1234567890",
  "metaData": {
    "department": "Engineering",
    "title": "Senior Developer"
  },
  "isActive": true,
  "createdOn": "2024-01-15T10:30:00Z",
  "updatedOn": "2024-01-15T10:30:00Z"
}
```

**Note**: Password is never returned in responses.

---

### 2.3 List All Individuals
**Endpoint**: `GET /admin/individuals`  
**Authorization**: `ROLE_ADMIN`

**Query Parameters**:
- `page` (optional, default: 0)
- `size` (optional, default: 20)
- `search` (optional): Search by name or email
- `orgId` (optional): Filter by organisation

**Response**: `200 OK` (same structure as organisations list)

---

### 2.4 Update Individual
**Endpoint**: `PUT /admin/individuals/{id}`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "John Doe Updated",
  "email": "john.doe.updated@example.com",
  "mobileNumber": "+1234567890",
  "metaData": {
    "department": "Engineering",
    "title": "Lead Developer"
  }
}
```

**Response**: `200 OK`

**Note**: Password cannot be updated via this endpoint. Use a separate password reset endpoint.

---

### 2.5 Delete Individual (Soft Delete)
**Endpoint**: `DELETE /admin/individuals/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `204 No Content`

---

### 2.6 Activate Individual
**Endpoint**: `PATCH /admin/individuals/{id}/activate`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`

---

### 2.7 Deactivate Individual
**Endpoint**: `PATCH /admin/individuals/{id}/deactivate`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`

---

## 3. Role Management APIs

### 3.1 Create Role
**Endpoint**: `POST /admin/roles`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "Content Manager",
  "description": "Can manage content and view reports",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "parentRoleId": null,
  "permissions": [
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
    },
    {
      "name": "Content",
      "type": "MENU",
      "permissions": 15,
      "displayNumber": 2,
      "children": [
        {
          "name": "Create_Content",
          "type": "ACTION",
          "permissions": 2,
          "displayNumber": 1,
          "children": []
        },
        {
          "name": "Edit_Content",
          "type": "ACTION",
          "permissions": 4,
          "displayNumber": 2,
          "children": []
        }
      ]
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002"
}
```

**Permission Bits**:
- `1` = Read
- `2` = Write/Create
- `4` = Update
- `8` = Delete
- `15` = All (1 + 2 + 4 + 8)

---

### 3.2 Get Role by ID
**Endpoint**: `GET /admin/roles/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "name": "Content Manager",
  "description": "Can manage content and view reports",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "parentRoleId": null,
  "permissions": [ /* ... */ ],
  "isActive": true,
  "createdOn": "2024-01-15T10:30:00Z",
  "updatedOn": "2024-01-15T10:30:00Z"
}
```

---

### 3.3 List Roles by Organisation
**Endpoint**: `GET /admin/roles`  
**Authorization**: `ROLE_ADMIN`

**Query Parameters**:
- `orgId` (required): Organisation ID
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response**: `200 OK` (paginated list)

---

### 3.4 Update Role
**Endpoint**: `PUT /admin/roles/{id}`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "Content Manager Updated",
  "description": "Updated description",
  "permissions": [ /* ... */ ]
}
```

**Response**: `200 OK`

---

### 3.5 Delete Role (Soft Delete)
**Endpoint**: `DELETE /admin/roles/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `204 No Content`

---

### 3.6 Activate Role
**Endpoint**: `PATCH /admin/roles/{id}/activate`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`

---

## 4. Resource Management APIs

### 4.1 Create Resource
**Endpoint**: `POST /admin/resources`  
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "name": "User Management",
  "description": "User management module",
  "type": "MODULE",
  "parentResourceId": null,
  "validations": {
    "minAccessLevel": "ADMIN",
    "requiresApproval": false
  }
}
```

**Response**: `201 Created`
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003"
}
```

---

### 4.2 Get Resource by ID
**Endpoint**: `GET /admin/resources/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "name": "User Management",
  "description": "User management module",
  "type": "MODULE",
  "parentResourceId": null,
  "validations": { /* ... */ },
  "isActive": true,
  "children": [
    {
      "id": "990e8400-e29b-41d4-a716-446655440004",
      "name": "Create User",
      "type": "ACTION",
      "parentResourceId": "880e8400-e29b-41d4-a716-446655440003"
    }
  ]
}
```

---

### 4.3 List All Resources
**Endpoint**: `GET /admin/resources`  
**Authorization**: `ROLE_ADMIN`

**Query Parameters**:
- `type` (optional): Filter by resource type
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response**: `200 OK` (paginated list)

---

### 4.4 Update Resource
**Endpoint**: `PUT /admin/resources/{id}`  
**Authorization**: `ROLE_ADMIN`

**Request Body**: Same as create

**Response**: `200 OK`

---

### 4.5 Delete Resource (Soft Delete)
**Endpoint**: `DELETE /admin/resources/{id}`  
**Authorization**: `ROLE_ADMIN`

**Response**: `204 No Content`

---

## 5. Role & Permission Assignment APIs

### 5.1 Assign Role to Individual
**Endpoint**: `POST /admin/assignments/roles`
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "roleId": "770e8400-e29b-41d4-a716-446655440002",
  "orgId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response**: `201 Created`
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440005",
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "roleId": "770e8400-e29b-41d4-a716-446655440002",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "createdOn": "2024-01-15T10:30:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Role already assigned to individual
- `404 Not Found`: Individual or Role not found

---

### 5.2 Revoke Role from Individual
**Endpoint**: `DELETE /admin/assignments/roles/{individualId}/{roleId}`
**Authorization**: `ROLE_ADMIN`

**Response**: `204 No Content`

---

### 5.3 Get Roles for Individual
**Endpoint**: `GET /admin/assignments/individuals/{individualId}/roles`
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "name": "Content Manager",
    "description": "Can manage content",
    "orgId": "550e8400-e29b-41d4-a716-446655440000",
    "assignedOn": "2024-01-15T10:30:00Z"
  }
]
```

---

### 5.4 Get Individuals with Role
**Endpoint**: `GET /admin/assignments/roles/{roleId}/individuals`
**Authorization**: `ROLE_ADMIN`

**Response**: `200 OK`
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "assignedOn": "2024-01-15T10:30:00Z"
  }
]
```

---

### 5.5 Override Permissions for Individual
**Endpoint**: `POST /admin/assignments/permissions`
**Authorization**: `ROLE_ADMIN`

**Request Body**:
```json
{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "permissions": [
    {
      "name": "Reports",
      "type": "MENU",
      "permissions": 1,
      "displayNumber": 3,
      "children": [
        {
          "name": "View_Advanced_Reports",
          "type": "ACTION",
          "permissions": 1,
          "displayNumber": 1,
          "children": []
        }
      ]
    }
  ],
  "remarks": "Granted access to advanced reports for Q1 analysis"
}
```

**Response**: `201 Created`
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440006"
}
```

---

### 5.6 Get Effective Permissions for Individual
**Endpoint**: `GET /admin/assignments/individuals/{individualId}/permissions`
**Authorization**: `ROLE_ADMIN`

**Query Parameters**:
- `orgId` (required): Organisation ID

**Response**: `200 OK`
```json
{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "effectivePermissions": [
    {
      "name": "Dashboard",
      "type": "MENU",
      "permissions": 1,
      "source": "ROLE",
      "roleName": "Content Manager",
      "children": [ /* ... */ ]
    },
    {
      "name": "Reports",
      "type": "MENU",
      "permissions": 1,
      "source": "OVERRIDE",
      "remarks": "Granted access to advanced reports",
      "children": [ /* ... */ ]
    }
  ]
}
```

**Note**: Effective permissions = Role permissions + Individual overrides (merged)

---

## 6. User-Facing APIs

### 6.1 Get My Entitlements
**Endpoint**: `GET /users/me/entitlements`
**Authorization**: `ROLE_USER` or `ROLE_ADMIN`

**Response**: `200 OK`
```json
{
  "individualId": "660e8400-e29b-41d4-a716-446655440001",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "organisations": [
    {
      "orgId": "550e8400-e29b-41d4-a716-446655440000",
      "orgName": "Acme Corporation",
      "roles": [
        {
          "id": "770e8400-e29b-41d4-a716-446655440002",
          "name": "Content Manager"
        }
      ],
      "effectivePermissions": [ /* ... */ ],
      "accessibleResources": [ /* ... */ ]
    }
  ]
}
```

---

### 6.2 Get My Roles
**Endpoint**: `GET /users/me/roles`
**Authorization**: `ROLE_USER` or `ROLE_ADMIN`

**Query Parameters**:
- `orgId` (optional): Filter by organisation

**Response**: `200 OK`
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "name": "Content Manager",
    "description": "Can manage content",
    "orgId": "550e8400-e29b-41d4-a716-446655440000",
    "orgName": "Acme Corporation"
  }
]
```

---

### 6.3 Get My Effective Permissions
**Endpoint**: `GET /users/me/permissions`
**Authorization**: `ROLE_USER` or `ROLE_ADMIN`

**Query Parameters**:
- `orgId` (required): Organisation ID

**Response**: `200 OK`
```json
[
  {
    "name": "Dashboard",
    "type": "MENU",
    "permissions": 1,
    "displayNumber": 1,
    "children": [ /* ... */ ]
  },
  {
    "name": "Content",
    "type": "MENU",
    "permissions": 15,
    "displayNumber": 2,
    "children": [ /* ... */ ]
  }
]
```

---

### 6.4 Get My Accessible Resources
**Endpoint**: `GET /users/me/resources`
**Authorization**: `ROLE_USER` or `ROLE_ADMIN`

**Query Parameters**:
- `orgId` (optional): Filter by organisation
- `type` (optional): Filter by resource type

**Response**: `200 OK`
```json
[
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "name": "User Management",
    "description": "User management module",
    "type": "MODULE",
    "accessLevel": "READ_WRITE",
    "children": [
      {
        "id": "990e8400-e29b-41d4-a716-446655440004",
        "name": "Create User",
        "type": "ACTION",
        "accessLevel": "WRITE"
      }
    ]
  }
]
```

---

## 7. Error Responses

### Standard Error Format
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Organisation with ID 550e8400-e29b-41d4-a716-446655440000 not found",
  "path": "/api/v1/admin/organisations/550e8400-e29b-41d4-a716-446655440000"
}
```

### Common HTTP Status Codes
- `200 OK`: Successful GET/PUT/PATCH
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Validation error or duplicate entry
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Unexpected server error

---

## 8. Pagination

All list endpoints support pagination with the following query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 20, max: 100)

**Response Format**:
```json
{
  "content": [ /* ... */ ],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## 9. Sorting

List endpoints support sorting with the `sort` query parameter:
- `sort=name,asc`: Sort by name ascending
- `sort=createdOn,desc`: Sort by creation date descending
- `sort=name,asc&sort=createdOn,desc`: Multi-field sorting

---

## 10. Filtering

### Search
Most list endpoints support a `search` parameter for text-based filtering:
- `search=acme`: Searches in name, description, email (depending on entity)

### Field-Specific Filters
- `orgId`: Filter by organisation
- `type`: Filter by resource type
- `isActive`: Filter by active status (true/false)

Example:
```
GET /api/v1/admin/individuals?orgId=550e8400-e29b-41d4-a716-446655440000&isActive=true&search=john
```

