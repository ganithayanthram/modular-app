# Swagger Quick Start Guide

## 🚀 Access Swagger UI

Start the application and navigate to:
```
http://localhost:8080/swagger-ui.html
```

## 🔐 Quick Authentication Setup

### 1. Login to Get Token
1. Expand **Authentication** section
2. Click on `POST /api/v1/auth/login`
3. Click **"Try it out"**
4. Use this example request:
```json
{
  "email": "admin@example.com",
  "password": "your-password",
  "orgId": "your-org-uuid"
}
```
5. Click **"Execute"**
6. Copy the `accessToken` from the response

### 2. Authorize All Requests
1. Click the **"Authorize"** button (🔒 icon at top)
2. Enter: `Bearer <paste-your-token-here>`
3. Click **"Authorize"**
4. Click **"Close"**

✅ Now all API calls will include your JWT token!

## 📚 API Sections

### Authentication
- Login, logout, refresh token
- **No auth required** for login/refresh

### Individual Management
- Create, read, update, delete users
- Activate/deactivate users
- **Auth required**

### Organization Management
- Manage organizations
- **Auth required**

### Role Management
- Create and manage roles
- View role permissions
- **Auth required**

### Resource Management
- Manage hierarchical resources (menus, pages, actions)
- View resource hierarchy
- **Auth required**

### Entitlement Assignment
- Assign/revoke roles to users
- Override permissions
- View user roles and permissions
- **Auth required**

### User Entitlements
- View your own entitlements
- Check your permissions
- View available resources
- **Auth required**

## 💡 Tips

- **Try it out**: Click this button on any endpoint to test it
- **Example values**: Swagger auto-fills example values
- **Response codes**: See all possible HTTP status codes
- **Schemas**: Click on schemas to see data structures
- **Download spec**: Get OpenAPI JSON at `/v3/api-docs`

## 🔄 Common Workflow

1. **Login** → Get access token
2. **Authorize** → Set Bearer token
3. **Create Organization** → Get org ID
4. **Create Individual** → Get user ID
5. **Create Role** → Get role ID
6. **Assign Role** → Link user to role
7. **Check Permissions** → Verify user entitlements

## 📖 Full Documentation

See [SWAGGER_API_DOCUMENTATION.md](./SWAGGER_API_DOCUMENTATION.md) for complete details.
