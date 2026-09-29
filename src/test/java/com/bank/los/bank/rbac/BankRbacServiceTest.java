package com.bank.los.bank.rbac;

import com.bank.los.bank.audit.service.BankAuditService;
import com.bank.los.bank.auth.service.PermissionService;
import com.bank.los.bank.master.entity.DesignationRoleMapping;
import com.bank.los.bank.master.entity.OrganizationRole;
import com.bank.los.bank.master.entity.Permission;
import com.bank.los.bank.master.entity.PermissionOverride;
import com.bank.los.bank.master.repository.DesignationRoleMappingRepository;
import com.bank.los.bank.master.repository.OrganizationRoleRepository;
import com.bank.los.bank.master.repository.PermissionOverrideRepository;
import com.bank.los.bank.master.repository.PermissionRepository;
import com.bank.los.bank.rbac.dto.*;
import com.bank.los.bank.rbac.service.BankRbacService;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.BankDataSourceProvider;
import com.bank.los.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankRbacServiceTest {

    @Mock
    private OrganizationRoleRepository organizationRoleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private DesignationRoleMappingRepository designationRoleMappingRepository;

    @Mock
    private PermissionOverrideRepository permissionOverrideRepository;

    @Mock
    private BankDataSourceProvider bankDataSourceProvider;

    @Mock
    private PermissionService permissionService;

    @Mock
    private BankAuditService bankAuditService;

    @InjectMocks
    private BankRbacService bankRbacService;

    private UserPrincipal adminPrincipal;
    private OrganizationRole adminRole;

    @BeforeEach
    void setUp() {
        adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@hdfcbank.com")
                .role("ADMIN")
                .organizationDbName("los_hdfc01_db")
                .organizationCode("HDFC01")
                .build();

        adminRole = OrganizationRole.builder()
                .id(1)
                .name("ADMIN")
                .panel("BANK_NBFC")
                .build();
    }

    @Test
    @DisplayName("Create Designation to Role mapping successfully")
    void testCreateDesignationMapping_Success() {
        DesignationRoleMappingRequest request = DesignationRoleMappingRequest.builder()
                .designation("General Manager")
                .roleName("ADMIN")
                .isActive(true)
                .build();

        when(organizationRoleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(designationRoleMappingRepository.findByDesignationIgnoreCase("General Manager")).thenReturn(Optional.empty());
        when(designationRoleMappingRepository.save(any(DesignationRoleMapping.class))).thenAnswer(i -> {
            DesignationRoleMapping m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        when(permissionOverrideRepository.findByTargetTypeAndTargetNameIgnoreCase(any(), any())).thenReturn(List.of());
        when(permissionService.getEffectivePermissions(any(), any(), any())).thenReturn(List.of("LOOKUP_BANK_VIEW"));

        DesignationRoleMappingResponse response = bankRbacService.createOrUpdateDesignationMapping(request, adminPrincipal);

        assertNotNull(response);
        assertEquals("General Manager", response.getDesignation());
        assertEquals("ADMIN", response.getRoleName());
        verify(designationRoleMappingRepository, times(1)).save(any(DesignationRoleMapping.class));
        verify(bankAuditService, times(1)).logAction(any(), eq("MAPPING_DESIGNATION_ROLE"), eq("RBAC"), any(), any());
    }

    @Test
    @DisplayName("Create Designation mapping with invalid role throws ResourceNotFoundException")
    void testCreateDesignationMapping_InvalidRole_ThrowsException() {
        DesignationRoleMappingRequest request = DesignationRoleMappingRequest.builder()
                .designation("General Manager")
                .roleName("INVALID_ROLE")
                .build();

        when(organizationRoleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bankRbacService.createOrUpdateDesignationMapping(request, adminPrincipal));
    }

    @Test
    @DisplayName("Delete Designation mapping successfully")
    void testDeleteDesignationMapping_Success() {
        DesignationRoleMapping mapping = DesignationRoleMapping.builder()
                .id(5L)
                .designation("General Manager")
                .role(adminRole)
                .build();

        when(designationRoleMappingRepository.findByDesignationIgnoreCase("General Manager"))
                .thenReturn(Optional.of(mapping));

        assertDoesNotThrow(() -> bankRbacService.deleteDesignationMapping("General Manager", adminPrincipal));
        verify(designationRoleMappingRepository, times(1)).delete(mapping);
        verify(bankAuditService, times(1)).logAction(any(), eq("DELETE_DESIGNATION_MAPPING"), eq("RBAC"), any(), any());
    }

    @Test
    @DisplayName("Create Permission Override with ALLOW / DENY effect successfully")
    void testCreatePermissionOverride_Success() {
        PermissionOverrideRequest request = PermissionOverrideRequest.builder()
                .targetType("DESIGNATION")
                .targetName("General Manager")
                .permissionCode(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE)
                .effect("DENY")
                .reason("Security policy: General Managers cannot delete bank lookups")
                .isActive(true)
                .build();

        when(permissionRepository.findByCode(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE))
                .thenReturn(Optional.of(Permission.builder().id(5).code(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE).build()));

        when(permissionOverrideRepository.findByTargetTypeAndTargetNameIgnoreCaseAndPermissionCodeIgnoreCase(
                "DESIGNATION", "General Manager", ApplicationConstants.Permissions.LOOKUP_BANK_DELETE
        )).thenReturn(Optional.empty());

        when(permissionOverrideRepository.save(any(PermissionOverride.class))).thenAnswer(i -> {
            PermissionOverride po = i.getArgument(0);
            po.setId(100L);
            return po;
        });

        PermissionOverrideResponse response = bankRbacService.createOrUpdatePermissionOverride(request, adminPrincipal);

        assertNotNull(response);
        assertEquals("DESIGNATION", response.getTargetType());
        assertEquals("General Manager", response.getTargetName());
        assertEquals(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE, response.getPermissionCode());
        assertEquals("DENY", response.getEffect());
        verify(permissionOverrideRepository, times(1)).save(any(PermissionOverride.class));
        verify(bankAuditService, times(1)).logAction(any(), eq("SET_PERMISSION_OVERRIDE"), eq("RBAC"), any(), any());
    }

    @Test
    @DisplayName("Create Permission Override with invalid effect throws BusinessException")
    void testCreatePermissionOverride_InvalidEffect_ThrowsException() {
        PermissionOverrideRequest request = PermissionOverrideRequest.builder()
                .targetType("ROLE")
                .targetName("ADMIN")
                .permissionCode(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE)
                .effect("INVALID_EFFECT")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankRbacService.createOrUpdatePermissionOverride(request, adminPrincipal));

        assertTrue(ex.getMessage().contains("ALLOW"));
    }

    @Test
    @DisplayName("Delete Permission Override successfully")
    void testDeletePermissionOverride_Success() {
        PermissionOverride override = PermissionOverride.builder()
                .id(20L)
                .targetType("DESIGNATION")
                .targetName("General Manager")
                .permissionCode("LOOKUP_BANK_DELETE")
                .effect("DENY")
                .build();

        when(permissionOverrideRepository.findById(20L)).thenReturn(Optional.of(override));

        assertDoesNotThrow(() -> bankRbacService.deletePermissionOverride(20L, adminPrincipal));
        verify(permissionOverrideRepository, times(1)).delete(override);
        verify(bankAuditService, times(1)).logAction(any(), eq("DELETE_PERMISSION_OVERRIDE"), eq("RBAC"), any(), any());
    }
}
