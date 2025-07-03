package com.nuraly.library.acl.service;

import com.nuraly.library.acl.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AuditService
 * Tests audit logging functionality and queries
 */
@QuarkusTest
public class AuditServiceTest {
    
    @Inject
    AuditService auditService;
    
    private UUID tenantId;
    private UUID userId;
    private UUID resourceId;
    private UUID permissionId;
    
    @BeforeEach
    @TestTransaction
    public void setup() {
        // Use unique identifiers to avoid conflicts
        String uniqueId = UUID.randomUUID().toString();
        tenantId = UUID.randomUUID();
        
        // Create test entities (let Hibernate generate the IDs)
        User user = new User();
        user.username = "testuser_" + uniqueId;
        user.email = "test" + uniqueId + "@example.com";
        user.tenantId = tenantId;
        user.persistAndFlush();
        userId = user.id;
        
        Resource resource = new Resource();
        resource.name = "Test Resource " + uniqueId;
        resource.resourceType = "document";
        resource.tenantId = tenantId;
        resource.ownerId = userId;
        resource.persistAndFlush();
        resourceId = resource.id;
        
        // Create unique permission (let Hibernate generate the ID)
        Permission permission = new Permission();
        permission.name = "test_audit_read_" + uniqueId;
        permission.description = "Test Audit Read permission";
        permission.isSystemPermission = false;
        permission.tenantId = tenantId;
        permission.persistAndFlush();
        permissionId = permission.id;
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log permission grant actions")
    public void testLogPermissionGrant() {
        // When
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.PERMISSION_GRANTED, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(resourceId, log.resourceId);
        assertEquals(permissionId, log.permissionId);
        assertEquals(tenantId, log.tenantId);
        assertNotNull(log.createdAt);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log permission revocation")
    public void testLogPermissionRevocation() {
        // When
        auditService.logPermissionRevoked(tenantId, userId, userId, resourceId, permissionId, "Testing revocation");
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.PERMISSION_REVOKED, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(resourceId, log.resourceId);
        assertEquals(permissionId, log.permissionId);
        assertEquals("Testing revocation", log.details);
        assertTrue(log.success);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log access attempts")
    public void testLogAccessAttempt() {
        // When
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, true, null);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.ACCESS_ATTEMPT, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(resourceId, log.resourceId);
        assertEquals(permissionId, log.permissionId);
        assertTrue(log.success);
        assertNull(log.errorMessage);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log failed access attempts")
    public void testLogFailedAccessAttempt() {
        // When
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, false, "Access denied");
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.ACCESS_ATTEMPT, log.actionType);
        assertFalse(log.success);
        assertEquals("Access denied", log.errorMessage);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log anonymous access attempts")
    public void testLogAnonymousAccess() {
        // When
        auditService.logAnonymousAccess(tenantId, resourceId, permissionId, true, null);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.ANONYMOUS_ACCESS, log.actionType);
        assertNull(log.userId); // Anonymous
        assertEquals(resourceId, log.resourceId);
        assertEquals(permissionId, log.permissionId);
        assertTrue(log.success);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log resource publishing")
    public void testLogResourcePublishing() {
        // When
        auditService.logResourcePublished(tenantId, userId, resourceId);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.RESOURCE_PUBLISHED, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(resourceId, log.resourceId);
        assertTrue(log.success);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log resource unpublishing")
    public void testLogResourceUnpublishing() {
        // When
        auditService.logResourceUnpublished(tenantId, userId, resourceId);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.RESOURCE_UNPUBLISHED, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(resourceId, log.resourceId);
        assertTrue(log.success);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should log resource sharing")
    public void testLogResourceSharing() {
        // Given
        UUID roleId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        
        // When
        auditService.logResourceShared(tenantId, userId, targetUserId, resourceId, roleId);
        
        // Then
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertEquals(AuditActionType.RESOURCE_SHARED, log.actionType);
        assertEquals(userId, log.userId);
        assertEquals(targetUserId, log.targetUserId);
        assertEquals(resourceId, log.resourceId);
        assertEquals(roleId, log.roleId);
        assertTrue(log.success);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find audit logs by user")
    public void testFindAuditLogsByUser() {
        // Given
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, true, null);
        
        // Create log for different user
        UUID otherUserId = UUID.randomUUID();
        auditService.logPermissionGranted(tenantId, otherUserId, otherUserId, resourceId, permissionId);
        
        // When
        List<AuditLog> userLogs = AuditLog.findByUser(userId);
        
        // Then
        assertEquals(2, userLogs.size());
        assertTrue(userLogs.stream().allMatch(log -> log.userId.equals(userId)));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find audit logs by resource")
    public void testFindAuditLogsByResource() {
        // Given
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        auditService.logResourcePublished(tenantId, userId, resourceId);
        
        // Create log for different resource
        UUID otherResourceId = UUID.randomUUID();
        auditService.logPermissionGranted(tenantId, userId, userId, otherResourceId, permissionId);
        
        // When
        List<AuditLog> resourceLogs = AuditLog.findByResource(resourceId);
        
        // Then
        assertEquals(2, resourceLogs.size());
        assertTrue(resourceLogs.stream().allMatch(log -> log.resourceId.equals(resourceId)));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find audit logs by action type")
    public void testFindAuditLogsByActionType() {
        // Given
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        auditService.logPermissionRevoked(tenantId, userId, userId, resourceId, permissionId, "test");
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, true, null);
        
        // When
        List<AuditLog> grantLogs = AuditLog.findByActionType(AuditActionType.PERMISSION_GRANTED)
            .stream().filter(log -> log.tenantId != null && log.tenantId.equals(tenantId)).toList();
        List<AuditLog> accessLogs = AuditLog.findByActionType(AuditActionType.ACCESS_ATTEMPT)
            .stream().filter(log -> log.tenantId != null && log.tenantId.equals(tenantId)).toList();
        
        // Then
        assertEquals(1, grantLogs.size());
        assertEquals(1, accessLogs.size());
        assertEquals(AuditActionType.PERMISSION_GRANTED, grantLogs.get(0).actionType);
        assertEquals(AuditActionType.ACCESS_ATTEMPT, accessLogs.get(0).actionType);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find audit logs by date range")
    public void testFindAuditLogsByDateRange() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);
        
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        
        // When
        List<AuditLog> recentLogs = AuditLog.findByDateRange(yesterday, tomorrow)
            .stream().filter(log -> log.tenantId != null && log.tenantId.equals(tenantId)).toList();
        List<AuditLog> futureLogs = AuditLog.findByDateRange(tomorrow, tomorrow.plusDays(1))
            .stream().filter(log -> log.tenantId != null && log.tenantId.equals(tenantId)).toList();
        
        // Then
        assertEquals(1, recentLogs.size());
        assertEquals(0, futureLogs.size());
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should find failed actions")
    public void testFindFailedActions() {
        // Given
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, true, null);
        auditService.logAccessAttempt(tenantId, userId, resourceId, permissionId, false, "Access denied");
        auditService.logAnonymousAccess(tenantId, resourceId, permissionId, false, "Not public");
        
        // When
        List<AuditLog> failedLogs = AuditLog.findFailedActions()
            .stream().filter(log -> log.tenantId != null && log.tenantId.equals(tenantId)).toList();
        
        // Then
        assertEquals(2, failedLogs.size());
        assertTrue(failedLogs.stream().allMatch(log -> !log.success));
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle tenant isolation in audit logs")
    public void testTenantIsolation() {
        // Given
        UUID otherTenantId = UUID.randomUUID();
        
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        auditService.logPermissionGranted(otherTenantId, userId, userId, resourceId, permissionId);
        
        // When
        List<AuditLog> tenant1Logs = AuditLog.findByTenant(tenantId);
        List<AuditLog> tenant2Logs = AuditLog.findByTenant(otherTenantId);
        
        // Then
        assertEquals(1, tenant1Logs.size());
        assertEquals(1, tenant2Logs.size());
        assertEquals(tenantId, tenant1Logs.get(0).tenantId);
        assertEquals(otherTenantId, tenant2Logs.get(0).tenantId);
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should handle null values gracefully")
    public void testNullValueHandling() {
        // When/Then - should not throw exceptions
        assertDoesNotThrow(() -> {
            auditService.logAccessAttempt(tenantId, null, resourceId, permissionId, true, null);
            auditService.logAccessAttempt(tenantId, userId, null, permissionId, true, null);
            auditService.logAccessAttempt(tenantId, userId, resourceId, null, true, null);
            auditService.logAnonymousAccess(tenantId, resourceId, permissionId, true, null);
        });
        
        // Verify logs were created (some fields may be null)
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(4, logs.size());
    }
    
    @Test
    @TestTransaction
    @DisplayName("Should create audit logs with proper timestamps")
    public void testAuditLogTimestamps() {
        // Given
        LocalDateTime beforeTest = LocalDateTime.now();
        
        // When
        auditService.logPermissionGranted(tenantId, userId, userId, resourceId, permissionId);
        
        // Then
        LocalDateTime afterTest = LocalDateTime.now();
        List<AuditLog> logs = AuditLog.findByTenant(tenantId);
        assertEquals(1, logs.size());
        
        AuditLog log = logs.get(0);
        assertNotNull(log.createdAt);
        assertTrue(log.createdAt.isAfter(beforeTest.minusSeconds(1)));
        assertTrue(log.createdAt.isBefore(afterTest.plusSeconds(1)));
    }
}
