package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ACLInitializationService
 * Tests system and tenant initialization functionality
 */
@QuarkusTest
public class ACLInitializationServiceTest {
    
    @Inject
    ACLInitializationService initService;
    
    @Test
    @TestTransaction
    @DisplayName("Should initialize tenant with share policies")
    public void testInitializeTenant() {
        // Given
        UUID tenantId = UUID.randomUUID();
        
        // When
        initService.initializeTenant(tenantId);
        
        // Then - verify tenant-specific share policies were created
        List<SharePolicy> tenantPolicies = SharePolicy.find("tenantId", tenantId).list();
        assertFalse(tenantPolicies.isEmpty());
        
        // Verify default policies exist
        SharePolicy publicPolicy = SharePolicy.find("tenantId = ?1 and name = ?2", tenantId, "Public Read-Only").firstResult();
        assertNotNull(publicPolicy);
        assertEquals(SharePolicyType.PUBLIC_LINK, publicPolicy.policyType);
        assertTrue(publicPolicy.allowAnonymous);
        
        SharePolicy teamPolicy = SharePolicy.find("tenantId = ?1 and name = ?2", tenantId, "Team Collaboration").firstResult();
        assertNotNull(teamPolicy);
        assertEquals(SharePolicyType.ORGANIZATION, teamPolicy.policyType);
        assertFalse(teamPolicy.allowAnonymous);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle multiple tenant initializations")
    public void testMultipleTenantInitialization() {
        // Given
        UUID tenant1Id = UUID.randomUUID();
        UUID tenant2Id = UUID.randomUUID();
        
        // When
        initService.initializeTenant(tenant1Id);
        initService.initializeTenant(tenant2Id);
        
        // Then - verify each tenant has its own policies
        List<SharePolicy> tenant1Policies = SharePolicy.find("tenantId", tenant1Id).list();
        List<SharePolicy> tenant2Policies = SharePolicy.find("tenantId", tenant2Id).list();
        
        assertFalse(tenant1Policies.isEmpty());
        assertFalse(tenant2Policies.isEmpty());
        
        // Verify tenant isolation
        assertTrue(tenant1Policies.stream().allMatch(policy -> policy.tenantId.equals(tenant1Id)));
        assertTrue(tenant2Policies.stream().allMatch(policy -> policy.tenantId.equals(tenant2Id)));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should not duplicate tenant initialization")
    public void testIdempotentTenantInitialization() {
        // Given
        UUID tenantId = UUID.randomUUID();
        
        // When - initialize tenant multiple times
        initService.initializeTenant(tenantId);
        long firstCount = SharePolicy.count("tenantId", tenantId);
        
        initService.initializeTenant(tenantId);
        long secondCount = SharePolicy.count("tenantId", tenantId);
        
        // Then - should not create duplicates
        assertEquals(firstCount, secondCount);
        assertTrue(firstCount > 0, "At least one share policy should be created for the tenant");
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle null tenant ID gracefully")
    public void testNullTenantHandling() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> {
            initService.initializeTenant(null);
        });
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify system permissions exist after startup")
    public void testSystemPermissionsExist() {
        // Note: System permissions are created during startup event
        // This test verifies they exist
        
        // Basic permissions should exist
        Permission readPermission = Permission.find("name", "read").firstResult();
        assertNotNull(readPermission);
        assertTrue(readPermission.isSystemPermission);
        
        Permission writePermission = Permission.find("name", "write").firstResult();
        assertNotNull(writePermission);
        assertTrue(writePermission.isSystemPermission);
        
        Permission deletePermission = Permission.find("name", "delete").firstResult();
        assertNotNull(deletePermission);
        assertTrue(deletePermission.isSystemPermission);
        
        Permission sharePermission = Permission.find("name", "share").firstResult();
        assertNotNull(sharePermission);
        assertTrue(sharePermission.isSystemPermission);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify system roles exist after startup")
    public void testSystemRolesExist() {
        // Note: System roles are created during startup event
        // This test verifies they exist
        
        // Application-level roles
        Role superAdminRole = Role.find("name = ?1 and externalTenantId is null", "Super Admin").firstResult();
        assertNotNull(superAdminRole);
        assertEquals(RoleScope.APPLICATION, superAdminRole.scope);
        assertTrue(superAdminRole.isSystemRole);
        assertFalse(superAdminRole.permissions.isEmpty());
        
        Role platformUserRole = Role.find("name = ?1 and externalTenantId is null", "Platform User").firstResult();
        assertNotNull(platformUserRole);
        assertEquals(RoleScope.APPLICATION, platformUserRole.scope);
        assertTrue(platformUserRole.isSystemRole);
        
        // Organization-level roles
        Role orgOwnerRole = Role.find("name = ?1 and externalTenantId is null", "Organization Owner").firstResult();
        assertNotNull(orgOwnerRole);
        assertEquals(RoleScope.ORGANIZATION, orgOwnerRole.scope);
        assertTrue(orgOwnerRole.isSystemRole);
        
        // Resource-level roles
        Role viewerRole = Role.find("name = ?1 and externalTenantId is null", "Viewer").firstResult();
        assertNotNull(viewerRole);
        assertEquals(RoleScope.RESOURCE, viewerRole.scope);
        assertTrue(viewerRole.isSystemRole);
        
        Role editorRole = Role.find("name = ?1 and externalTenantId is null", "Editor").firstResult();
        assertNotNull(editorRole);
        assertEquals(RoleScope.RESOURCE, editorRole.scope);
        assertTrue(editorRole.isSystemRole);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify role permission assignments")
    public void testRolePermissionAssignments() {
        // Verify role permission assignments
        Role viewerRole = Role.find("name = ?1 and externalTenantId is null", "Viewer").firstResult();
        if (viewerRole != null) {
            assertTrue(viewerRole.permissions.stream().anyMatch(p -> p.name.equals("read")));
            // Viewer should not have write/delete permissions
            assertFalse(viewerRole.permissions.stream().anyMatch(p -> p.name.equals("delete")));
        }
        
        Role editorRole = Role.find("name = ?1 and externalTenantId is null", "Editor").firstResult();
        if (editorRole != null) {
            assertTrue(editorRole.permissions.stream().anyMatch(p -> p.name.equals("read")));
            assertTrue(editorRole.permissions.stream().anyMatch(p -> p.name.equals("write")));
            assertTrue(editorRole.permissions.stream().anyMatch(p -> p.name.equals("share")));
        }
        
        Role publisherRole = Role.find("name = ?1 and externalTenantId is null", "Publisher").firstResult();
        if (publisherRole != null) {
            assertTrue(publisherRole.permissions.stream().anyMatch(p -> p.name.equals("read")));
            assertTrue(publisherRole.permissions.stream().anyMatch(p -> p.name.equals("write")));
            assertTrue(publisherRole.permissions.stream().anyMatch(p -> p.name.equals("publish")));
        }
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify resource-specific permissions exist")
    public void testResourceSpecificPermissions() {
        // Document permissions
        Permission docReadPermission = Permission.find("name", "document:read").firstResult();
        assertNotNull(docReadPermission);
        assertEquals("document", docReadPermission.resourceType);
        
        Permission docWritePermission = Permission.find("name", "document:write").firstResult();
        assertNotNull(docWritePermission);
        assertEquals("document", docWritePermission.resourceType);
        
        // Dashboard permissions
        Permission dashReadPermission = Permission.find("name", "dashboard:read").firstResult();
        assertNotNull(dashReadPermission);
        assertEquals("dashboard", dashReadPermission.resourceType);
        
        // Function permissions
        Permission funcExecutePermission = Permission.find("name", "function:execute").firstResult();
        assertNotNull(funcExecutePermission);
        assertEquals("function", funcExecutePermission.resourceType);
        
        Permission funcDeployPermission = Permission.find("name", "function:deploy").firstResult();
        assertNotNull(funcDeployPermission);
        assertEquals("function", funcDeployPermission.resourceType);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify organization permissions exist")
    public void testOrganizationPermissions() {
        Permission orgReadPermission = Permission.find("name", "organization:read").firstResult();
        assertNotNull(orgReadPermission);
        assertEquals("organization", orgReadPermission.resourceType);
        
        Permission orgAdminPermission = Permission.find("name", "organization:admin").firstResult();
        assertNotNull(orgAdminPermission);
        assertEquals("organization", orgAdminPermission.resourceType);
        
        Permission orgInvitePermission = Permission.find("name", "organization:invite").firstResult();
        assertNotNull(orgInvitePermission);
        assertEquals("organization", orgInvitePermission.resourceType);
        
        Permission orgRemovePermission = Permission.find("name", "organization:remove").firstResult();
        assertNotNull(orgRemovePermission);
        assertEquals("organization", orgRemovePermission.resourceType);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should verify all required system permissions exist")
    public void testAllSystemPermissions() {
        // Basic permissions
        String[] basicPermissions = {
            "read", "write", "delete", "share", "publish", "annotate", "moderate", "admin"
        };
        
        for (String permissionName : basicPermissions) {
            Permission permission = Permission.find("name", permissionName).firstResult();
            assertNotNull(permission, "Basic permission '" + permissionName + "' should exist");
            assertTrue(permission.isSystemPermission);
        }
        
        // Resource-specific permissions
        String[] resourceTypes = {"document", "dashboard", "function", "organization"};
        
        for (String resourceType : resourceTypes) {
            Permission readPermission = Permission.find("name", resourceType + ":read").firstResult();
            assertNotNull(readPermission, "Read permission for " + resourceType + " should exist");
            assertEquals(resourceType, readPermission.resourceType);
        }
    }
}
