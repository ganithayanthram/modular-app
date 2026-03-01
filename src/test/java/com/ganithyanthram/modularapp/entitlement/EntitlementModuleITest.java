package com.ganithyanthram.modularapp.entitlement;

import com.ganithyanthram.modularapp.config.DockerEnvironmentDetector;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.AssignRoleRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.request.OverridePermissionRequest;
import com.ganithyanthram.modularapp.entitlement.assignment.dto.response.UserEntitlementResponse;
import com.ganithyanthram.modularapp.entitlement.assignment.service.PermissionOverrideService;
import com.ganithyanthram.modularapp.entitlement.assignment.service.RoleAssignmentService;
import com.ganithyanthram.modularapp.entitlement.common.dto.RoleNode;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateIndividualException;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateOrganisationException;
import com.ganithyanthram.modularapp.entitlement.common.exception.DuplicateRoleException;
import com.ganithyanthram.modularapp.entitlement.common.exception.IndividualNotFoundException;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.CreateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.request.UpdateIndividualRequest;
import com.ganithyanthram.modularapp.entitlement.individual.dto.response.IndividualResponse;
import com.ganithyanthram.modularapp.entitlement.individual.service.IndividualService;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.CreateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.request.UpdateOrganisationRequest;
import com.ganithyanthram.modularapp.entitlement.organisation.dto.response.OrganisationResponse;
import com.ganithyanthram.modularapp.entitlement.organisation.service.OrganisationService;
import com.ganithyanthram.modularapp.entitlement.resource.dto.request.CreateResourceRequest;
import com.ganithyanthram.modularapp.entitlement.resource.dto.response.ResourceResponse;
import com.ganithyanthram.modularapp.entitlement.resource.service.ResourceService;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.CreateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.request.UpdateRoleRequest;
import com.ganithyanthram.modularapp.entitlement.role.dto.response.RoleResponse;
import com.ganithyanthram.modularapp.entitlement.role.service.RoleService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for Entitlement Management Module
 * Tests all services with real database using Testcontainers
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
@DisplayName("Entitlement Module Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EntitlementModuleITest {
    
    static {
        DockerEnvironmentDetector.configureFromProfile("test");
    }
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jooq.sql-dialect", () -> "POSTGRES");
    }
    
    @Autowired
    private OrganisationService organisationService;
    
    @Autowired
    private IndividualService individualService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private ResourceService resourceService;
    
    @Autowired
    private RoleAssignmentService roleAssignmentService;
    
    @Autowired
    private PermissionOverrideService permissionOverrideService;
    
    private static UUID testUserId;
    private static UUID orgId;
    private static UUID individualId;
    private static UUID roleId;
    private static UUID parentRoleId;
    private static UUID resourceId;
    
    @Test
    @Order(1)
    @DisplayName("1. Should verify PostgreSQL container is running")
    void shouldVerifyPostgreSQLContainerIsRunning() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        assertNotNull(postgres.getJdbcUrl(), "JDBC URL should be available");
    }
    
    @Test
    @Order(2)
    @DisplayName("2. Organisation: Should create organisation successfully")
    void shouldCreateOrganisationSuccessfully() {
        testUserId = UUID.randomUUID();
        
        CreateOrganisationRequest request = CreateOrganisationRequest.builder()
                .name("Test Organisation")
                .category("Technology")
                .metaData(Map.of("industry", "Software", "size", "Enterprise"))
                .status("active")
                .build();
        
        orgId = organisationService.createOrganisation(request, testUserId);
        
        assertNotNull(orgId);
    }
    
    @Test
    @Order(3)
    @DisplayName("3. Organisation: Should get organisation by ID")
    void shouldGetOrganisationById() {
        OrganisationResponse response = organisationService.getOrganisationById(orgId);
        
        assertNotNull(response);
        assertEquals(orgId, response.getId());
        assertEquals("Test Organisation", response.getName());
        assertEquals("Technology", response.getCategory());
        assertTrue(response.getIsActive());
    }
    
    @Test
    @Order(4)
    @DisplayName("4. Organisation: Should throw exception for duplicate name")
    void shouldThrowExceptionForDuplicateOrganisation() {
        CreateOrganisationRequest request = CreateOrganisationRequest.builder()
                .name("Test Organisation")
                .category("Technology")
                .build();
        
        assertThrows(DuplicateOrganisationException.class, 
                () -> organisationService.createOrganisation(request, testUserId));
    }
    
    @Test
    @Order(5)
    @DisplayName("5. Organisation: Should update organisation")
    void shouldUpdateOrganisation() {
        UpdateOrganisationRequest request = UpdateOrganisationRequest.builder()
                .name("Test Organisation")
                .category("Updated Category")
                .metaData(Map.of("industry", "Tech", "size", "Large"))
                .build();
        
        OrganisationResponse response = organisationService.updateOrganisation(orgId, request, testUserId);
        
        assertNotNull(response);
        assertEquals("Updated Category", response.getCategory());
    }
    
    @Test
    @Order(6)
    @DisplayName("6. Organisation: Should list organisations with pagination")
    void shouldListOrganisationsWithPagination() {
        List<OrganisationResponse> organisations = organisationService.getAllOrganisations(0, 20, null);
        
        assertNotNull(organisations);
        assertFalse(organisations.isEmpty());
        assertTrue(organisations.size() >= 1);
    }
    
    @Test
    @Order(7)
    @DisplayName("7. Individual: Should create individual successfully")
    void shouldCreateIndividualSuccessfully() {
        CreateIndividualRequest request = CreateIndividualRequest.builder()
                .name("John Doe")
                .email("john@test.com")
                .mobileNumber("+1234567890")
                .password("SecurePass123!")
                .metaData(Map.of("department", "IT", "level", "Senior"))
                .build();
        
        individualId = individualService.createIndividual(request, testUserId);
        
        assertNotNull(individualId);
    }
    
    @Test
    @Order(8)
    @DisplayName("8. Individual: Should get individual by ID")
    void shouldGetIndividualById() {
        IndividualResponse response = individualService.getIndividualById(individualId);
        
        assertNotNull(response);
        assertEquals(individualId, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@test.com", response.getEmail());
        assertTrue(response.getIsActive());
    }
    
    @Test
    @Order(9)
    @DisplayName("9. Individual: Should throw exception for duplicate email")
    void shouldThrowExceptionForDuplicateIndividual() {
        CreateIndividualRequest request = CreateIndividualRequest.builder()
                .name("Jane Doe")
                .email("john@test.com")
                .password("SecurePass123!")
                .build();
        
        assertThrows(DuplicateIndividualException.class, 
                () -> individualService.createIndividual(request, testUserId));
    }
    
    @Test
    @Order(10)
    @DisplayName("10. Individual: Should update individual")
    void shouldUpdateIndividual() {
        UpdateIndividualRequest request = UpdateIndividualRequest.builder()
                .name("John Updated")
                .email("john@test.com")
                .mobileNumber("+9876543210")
                .metaData(Map.of("department", "HR"))
                .build();
        
        IndividualResponse response = individualService.updateIndividual(individualId, request, testUserId);
        
        assertNotNull(response);
        assertEquals("John Updated", response.getName());
        assertEquals("+9876543210", response.getMobileNumber());
    }
    
    @Test
    @Order(11)
    @DisplayName("11. Individual: Should list individuals with pagination")
    void shouldListIndividualsWithPagination() {
        List<IndividualResponse> individuals = individualService.getAllIndividuals(0, 20, null);
        
        assertNotNull(individuals);
        assertFalse(individuals.isEmpty());
        assertTrue(individuals.size() >= 1);
    }
    
    @Test
    @Order(12)
    @DisplayName("12. Role: Should create parent role successfully")
    void shouldCreateParentRoleSuccessfully() {
        List<RoleNode> permissions = Arrays.asList(
                RoleNode.builder()
                        .name("users")
                        .type("resource")
                        .permissions(15)
                        .children(Arrays.asList(
                                RoleNode.builder()
                                        .name("view")
                                        .type("action")
                                        .permissions(1)
                                        .build()
                        ))
                        .build()
        );
        
        CreateRoleRequest request = CreateRoleRequest.builder()
                .name("Manager")
                .description("Manager role with full access")
                .orgId(orgId)
                .permissions(permissions)
                .build();
        
        parentRoleId = roleService.createRole(request, testUserId);
        
        assertNotNull(parentRoleId);
    }
    
    @Test
    @Order(13)
    @DisplayName("13. Role: Should create child role with parent")
    void shouldCreateChildRoleWithParent() {
        List<RoleNode> permissions = Arrays.asList(
                RoleNode.builder()
                        .name("reports")
                        .type("resource")
                        .permissions(1)
                        .build()
        );
        
        CreateRoleRequest request = CreateRoleRequest.builder()
                .name("Team Lead")
                .description("Team lead role inheriting from Manager")
                .orgId(orgId)
                .parentRoleId(parentRoleId)
                .permissions(permissions)
                .build();
        
        roleId = roleService.createRole(request, testUserId);
        
        assertNotNull(roleId);
    }
    
    @Test
    @Order(14)
    @DisplayName("14. Role: Should get role by ID")
    void shouldGetRoleById() {
        RoleResponse response = roleService.getRoleById(roleId);
        
        assertNotNull(response);
        assertEquals(roleId, response.getId());
        assertEquals("Team Lead", response.getName());
        assertEquals(parentRoleId, response.getParentRoleId());
    }
    
    @Test
    @Order(15)
    @DisplayName("15. Role: Should throw exception for duplicate role name")
    void shouldThrowExceptionForDuplicateRole() {
        CreateRoleRequest request = CreateRoleRequest.builder()
                .name("Manager")
                .orgId(orgId)
                .build();
        
        assertThrows(DuplicateRoleException.class, 
                () -> roleService.createRole(request, testUserId));
    }
    
    @Test
    @Order(16)
    @DisplayName("16. Role: Should calculate effective permissions with parent")
    void shouldCalculateEffectivePermissionsWithParent() {
        List<RoleNode> effectivePermissions = roleService.getEffectivePermissions(roleId);
        
        assertNotNull(effectivePermissions);
        assertEquals(2, effectivePermissions.size());
        
        boolean hasUsersPermission = effectivePermissions.stream()
                .anyMatch(node -> "users".equals(node.getName()) && node.getPermissions() == 15);
        boolean hasReportsPermission = effectivePermissions.stream()
                .anyMatch(node -> "reports".equals(node.getName()) && node.getPermissions() == 1);
        
        assertTrue(hasUsersPermission, "Should inherit 'users' permission from parent");
        assertTrue(hasReportsPermission, "Should have own 'reports' permission");
    }
    
    @Test
    @Order(17)
    @DisplayName("17. Role: Should update role")
    void shouldUpdateRole() {
        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .name("Team Lead")
                .description("Updated description")
                .permissions(Arrays.asList(
                        RoleNode.builder()
                                .name("reports")
                                .permissions(3)
                                .build()
                ))
                .build();
        
        RoleResponse response = roleService.updateRole(roleId, request, testUserId);
        
        assertNotNull(response);
        assertEquals("Updated description", response.getDescription());
    }
    
    @Test
    @Order(18)
    @DisplayName("18. Role: Should list roles by organisation")
    void shouldListRolesByOrganisation() {
        List<RoleResponse> roles = roleService.getRolesByOrganisation(orgId, 0, 20);
        
        assertNotNull(roles);
        assertEquals(2, roles.size());
    }
    
    @Test
    @Order(19)
    @DisplayName("19. Resource: Should create root resource")
    void shouldCreateRootResource() {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .name("Dashboard")
                .description("Main dashboard")
                .type("menu")
                .validations(Map.of("required", true))
                .build();
        
        resourceId = resourceService.createResource(request, testUserId);
        
        assertNotNull(resourceId);
    }
    
    @Test
    @Order(20)
    @DisplayName("20. Resource: Should create child resource")
    void shouldCreateChildResource() {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .name("Analytics")
                .description("Analytics section")
                .type("menu")
                .parentResourceId(resourceId)
                .build();
        
        UUID childId = resourceService.createResource(request, testUserId);
        
        assertNotNull(childId);
    }
    
    @Test
    @Order(21)
    @DisplayName("21. Resource: Should get resource hierarchy")
    void shouldGetResourceHierarchy() {
        List<ResourceResponse> hierarchy = resourceService.getResourceHierarchy();
        
        assertNotNull(hierarchy);
        assertFalse(hierarchy.isEmpty());
        
        ResourceResponse root = hierarchy.stream()
                .filter(r -> "Dashboard".equals(r.getName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(root);
        assertNotNull(root.getChildren());
        assertEquals(1, root.getChildren().size());
        assertEquals("Analytics", root.getChildren().get(0).getName());
    }
    
    @Test
    @Order(22)
    @DisplayName("22. Assignment: Should assign role to individual")
    void shouldAssignRoleToIndividual() {
        AssignRoleRequest request = AssignRoleRequest.builder()
                .individualId(individualId)
                .roleId(roleId)
                .orgId(orgId)
                .build();
        
        UUID assignmentId = roleAssignmentService.assignRole(request, testUserId);
        
        assertNotNull(assignmentId);
    }
    
    @Test
    @Order(23)
    @DisplayName("23. Assignment: Should get roles for individual")
    void shouldGetRolesForIndividual() {
        List<RoleResponse> roles = roleAssignmentService.getRolesByIndividual(individualId);
        
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals(roleId, roles.get(0).getId());
        assertEquals("Team Lead", roles.get(0).getName());
    }
    
    @Test
    @Order(24)
    @DisplayName("24. Assignment: Should get individuals by role")
    void shouldGetIndividualsByRole() {
        List<IndividualResponse> individuals = roleAssignmentService.getIndividualsByRole(roleId);
        
        assertNotNull(individuals);
        assertEquals(1, individuals.size());
        assertEquals(individualId, individuals.get(0).getId());
    }
    
    @Test
    @Order(25)
    @DisplayName("25. Permission Override: Should override permissions for individual")
    void shouldOverridePermissionsForIndividual() {
        List<RoleNode> overridePermissions = Arrays.asList(
                RoleNode.builder()
                        .name("special-reports")
                        .type("resource")
                        .permissions(3)
                        .build()
        );
        
        OverridePermissionRequest request = OverridePermissionRequest.builder()
                .individualId(individualId)
                .orgId(orgId)
                .permissions(overridePermissions)
                .remarks("Temporary Q1 access")
                .build();
        
        UUID permissionId = permissionOverrideService.overridePermissions(request, testUserId);
        
        assertNotNull(permissionId);
    }
    
    @Test
    @Order(26)
    @DisplayName("26. Permission Override: Should get effective permissions with overrides")
    void shouldGetEffectivePermissionsWithOverrides() {
        List<RoleNode> effectivePermissions = permissionOverrideService.getEffectivePermissions(individualId, orgId);
        
        assertNotNull(effectivePermissions);
        assertTrue(effectivePermissions.size() >= 3);
        
        boolean hasUsersPermission = effectivePermissions.stream()
                .anyMatch(node -> "users".equals(node.getName()));
        boolean hasReportsPermission = effectivePermissions.stream()
                .anyMatch(node -> "reports".equals(node.getName()));
        boolean hasSpecialReportsPermission = effectivePermissions.stream()
                .anyMatch(node -> "special-reports".equals(node.getName()));
        
        assertTrue(hasUsersPermission, "Should have 'users' from parent role");
        assertTrue(hasReportsPermission, "Should have 'reports' from assigned role");
        assertTrue(hasSpecialReportsPermission, "Should have 'special-reports' from override");
    }
    
    @Test
    @Order(27)
    @DisplayName("27. Permission Override: Should get complete user entitlements")
    void shouldGetCompleteUserEntitlements() {
        UserEntitlementResponse entitlements = permissionOverrideService.getUserEntitlements(individualId);
        
        assertNotNull(entitlements);
        assertEquals(individualId, entitlements.getIndividualId());
        assertEquals("John Updated", entitlements.getName());
        assertEquals("john@test.com", entitlements.getEmail());
        
        assertNotNull(entitlements.getOrganisations());
        assertEquals(1, entitlements.getOrganisations().size());
        
        UserEntitlementResponse.OrganisationEntitlement orgEntitlement = entitlements.getOrganisations().get(0);
        assertEquals(orgId, orgEntitlement.getOrgId());
        assertEquals("Test Organisation", orgEntitlement.getOrgName());
        assertNotNull(orgEntitlement.getRoles());
        assertEquals(1, orgEntitlement.getRoles().size());
        assertNotNull(orgEntitlement.getEffectivePermissions());
        assertTrue(orgEntitlement.getEffectivePermissions().size() >= 3);
    }
    
    @Test
    @Order(28)
    @DisplayName("28. Assignment: Should revoke role from individual")
    void shouldRevokeRoleFromIndividual() {
        assertDoesNotThrow(() -> roleAssignmentService.revokeRole(individualId, roleId));
        
        List<RoleResponse> roles = roleAssignmentService.getRolesByIndividual(individualId);
        assertTrue(roles.isEmpty(), "Individual should have no roles after revocation");
    }
    
    @Test
    @Order(29)
    @DisplayName("29. Role: Should delete role (soft delete)")
    void shouldDeleteRole() {
        assertDoesNotThrow(() -> roleService.deleteRole(roleId));
        
        assertThrows(Exception.class, () -> roleService.getRoleById(roleId));
    }
    
    @Test
    @Order(30)
    @DisplayName("30. Role: Should activate deleted role")
    void shouldActivateDeletedRole() {
        assertDoesNotThrow(() -> roleService.activateRole(roleId));
        
        RoleResponse response = roleService.getRoleById(roleId);
        assertNotNull(response);
        assertTrue(response.getIsActive());
    }
    
    @Test
    @Order(31)
    @DisplayName("31. Resource: Should delete resource (soft delete)")
    void shouldDeleteResource() {
        assertDoesNotThrow(() -> resourceService.deleteResource(resourceId));
        
        assertThrows(Exception.class, () -> resourceService.getResourceById(resourceId));
    }
    
    @Test
    @Order(32)
    @DisplayName("32. Individual: Should deactivate individual")
    void shouldDeactivateIndividual() {
        assertDoesNotThrow(() -> individualService.deactivateIndividual(individualId));
        
        assertThrows(IndividualNotFoundException.class, 
                () -> individualService.getIndividualById(individualId));
    }
    
    @Test
    @Order(33)
    @DisplayName("33. Individual: Should activate individual")
    void shouldActivateIndividual() {
        assertDoesNotThrow(() -> individualService.activateIndividual(individualId));
        
        IndividualResponse response = individualService.getIndividualById(individualId);
        assertNotNull(response);
        assertTrue(response.getIsActive());
    }
    
    @Test
    @Order(34)
    @DisplayName("34. Organisation: Should deactivate organisation")
    void shouldDeactivateOrganisation() {
        assertDoesNotThrow(() -> organisationService.deactivateOrganisation(orgId));
    }
    
    @Test
    @Order(35)
    @DisplayName("35. Organisation: Should activate organisation")
    void shouldActivateOrganisation() {
        assertDoesNotThrow(() -> organisationService.activateOrganisation(orgId));
        
        OrganisationResponse response = organisationService.getOrganisationById(orgId);
        assertNotNull(response);
        assertTrue(response.getIsActive());
    }
}
