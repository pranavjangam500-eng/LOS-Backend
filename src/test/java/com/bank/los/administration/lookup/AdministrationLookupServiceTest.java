package com.bank.los.administration.lookup;

import com.bank.los.administration.audit.service.AdminAuditService;
import com.bank.los.administration.lookup.dto.*;
import com.bank.los.administration.lookup.service.AdministrationLookupService;
import com.bank.los.administration.master.entity.MasterLookupSubType;
import com.bank.los.administration.master.entity.MasterLookupType;
import com.bank.los.administration.master.entity.Organization;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.administration.master.repository.MasterLookupTypeRepository;
import com.bank.los.administration.master.repository.OrganizationRepository;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.entity.BankLookupType;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.bank.master.repository.BankLookupTypePermissionRepository;
import com.bank.los.bank.master.repository.BankLookupTypeRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ConflictException;
import com.bank.los.security.UserPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministrationLookupServiceTest {

    @Mock
    private MasterLookupTypeRepository masterLookupTypeRepository;

    @Mock
    private MasterLookupSubTypeRepository masterLookupSubTypeRepository;

    @Mock
    private com.bank.los.administration.master.repository.MasterLookupTypePermissionRepository masterLookupTypePermissionRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private BankLookupTypeRepository bankLookupTypeRepository;

    @Mock
    private BankLookupSubTypeRepository bankLookupSubTypeRepository;

    @Mock
    private BankLookupTypePermissionRepository bankLookupTypePermissionRepository;

    @Mock
    private AdminAuditService adminAuditService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AdministrationLookupService lookupService;

    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        adminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@losplatform.com")
                .role("INTERNAL_ADMIN")
                .build();
    }

    @Test
    @DisplayName("Admin should create master lookup type with server-generated sequence code (10010)")
    void testCreateLookupType_Success() {
        CreateLookupTypeRequest request = CreateLookupTypeRequest.builder()
                .name("Collateral Type")
                .isFixed(false)
                .status("ACTIVE")
                .build();

        Query nextValQuery = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT nextval('identity.master_lookup_type_code_seq')")).thenReturn(nextValQuery);
        when(nextValQuery.getSingleResult()).thenReturn(10010L);

        when(masterLookupTypeRepository.existsByDescriptionIgnoreCase("Collateral Type")).thenReturn(false);
        when(masterLookupTypeRepository.save(any(MasterLookupType.class))).thenAnswer(i -> {
            MasterLookupType t = i.getArgument(0);
            t.setId(10L);
            return t;
        });

        LookupTypeResponse response = lookupService.createLookupType(request, adminPrincipal);

        assertNotNull(response);
        assertEquals("10010", response.getCode());
        assertEquals("Collateral Type", response.getName());
        assertEquals("Collateral Type", response.getDescription());
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.getIsActive());
        assertFalse(response.getIsFixed());
    }

    @Test
    @DisplayName("Admin creating duplicate lookup type name throws ConflictException (409)")
    void testCreateLookupType_DuplicateNameThrowsConflict() {
        CreateLookupTypeRequest request = CreateLookupTypeRequest.builder()
                .name("Collateral Type")
                .build();

        when(masterLookupTypeRepository.existsByDescriptionIgnoreCase("Collateral Type")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> lookupService.createLookupType(request, adminPrincipal));
        assertTrue(ex.getMessage().contains("Lookup type already exists with name"));
    }

    @Test
    @DisplayName("Admin creates master lookup type with custom permission codes (101, 102, 103)")
    void testCreateLookupType_WithPermissions() {
        CreateLookupTypeRequest request = CreateLookupTypeRequest.builder()
                .name("Custom Lookup")
                .isFixed(false)
                .status("ACTIVE")
                .permissions(List.of("101", "102", "103"))
                .build();

        Query nextValQuery = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT nextval('identity.master_lookup_type_code_seq')")).thenReturn(nextValQuery);
        when(nextValQuery.getSingleResult()).thenReturn(10011L);

        when(masterLookupTypeRepository.existsByDescriptionIgnoreCase("Custom Lookup")).thenReturn(false);
        when(masterLookupTypeRepository.save(any(MasterLookupType.class))).thenAnswer(i -> {
            MasterLookupType t = i.getArgument(0);
            t.setId(11L);
            return t;
        });

        LookupTypeResponse response = lookupService.createLookupType(request, adminPrincipal);

        assertNotNull(response);
        assertEquals("10011", response.getCode());
        verify(masterLookupTypePermissionRepository, times(3)).save(any());
    }

    @Test
    @DisplayName("Deleting system fixed master lookup type throws BusinessException (400)")
    void testDeleteLookupType_SystemFixed_ThrowsException() {
        MasterLookupType fixedType = MasterLookupType.builder()
                .id(1L)
                .code("10001")
                .description("Bank User Status")
                .isFixed(true)
                .build();

        when(masterLookupTypeRepository.findByCode("10001")).thenReturn(Optional.of(fixedType));

        BusinessException ex = assertThrows(BusinessException.class, () -> lookupService.deleteLookupType("10001"));
        assertTrue(ex.getMessage().contains("Cannot delete system fixed"));
    }

    @Test
    @DisplayName("Deactivating system fixed master lookup type throws BusinessException (400)")
    void testDeactivateLookupType_SystemFixed_ThrowsException() {
        MasterLookupType fixedType = MasterLookupType.builder()
                .id(1L)
                .code("10001")
                .description("Bank User Status")
                .isFixed(true)
                .build();

        when(masterLookupTypeRepository.findByCode("10001")).thenReturn(Optional.of(fixedType));

        BusinessException ex = assertThrows(BusinessException.class, () -> lookupService.deactivateLookupType("10001", adminPrincipal));
        assertTrue(ex.getMessage().contains("Cannot deactivate system-fixed"));
    }

    @Test
    @DisplayName("Admin should add sub type with generated code = max numeric + 1 under pessimistic lock")
    void testAddSubType_Success() {
        MasterLookupType type = MasterLookupType.builder()
                .id(2L)
                .code("10002")
                .description("Designation")
                .isFixed(false)
                .build();

        CreateLookupSubTypeRequest subTypeRequest = CreateLookupSubTypeRequest.builder()
                .name("Managing Director")
                .isFixed(false)
                .displayOrder(10)
                .build();

        when(masterLookupTypeRepository.findByCodeForUpdate("10002")).thenReturn(Optional.of(type));
        when(masterLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCase("10002", "Managing Director")).thenReturn(false);

        MasterLookupSubType existingOpt9 = MasterLookupSubType.builder()
                .lookupTypeCode("10002")
                .subTypeCode("9")
                .displayOrder(9)
                .build();
        when(masterLookupSubTypeRepository.findByLookupTypeCodeOrderByDisplayOrderAscIdAsc("10002")).thenReturn(List.of(existingOpt9));

        when(masterLookupSubTypeRepository.save(any(MasterLookupSubType.class))).thenAnswer(i -> {
            MasterLookupSubType st = i.getArgument(0);
            st.setId(50L);
            return st;
        });

        LookupSubTypeResponse response = lookupService.addSubType("10002", subTypeRequest, adminPrincipal);

        assertNotNull(response);
        assertEquals("10", response.getCode());
        assertEquals("10", response.getSubTypeCode());
        assertEquals("Managing Director", response.getName());
        assertEquals("Managing Director", response.getDescription());
        assertEquals("Managing Director", response.getSubTypeDescription());
        assertEquals("10002", response.getLookupTypeCode());
        assertEquals("10002", response.getTypeCode());
    }

    @Test
    @DisplayName("Admin adding duplicate sub type name under same type throws ConflictException (409)")
    void testAddSubType_DuplicateNameThrowsConflict() {
        MasterLookupType type = MasterLookupType.builder()
                .id(2L)
                .code("10002")
                .description("Designation")
                .isFixed(false)
                .build();

        CreateLookupSubTypeRequest subTypeRequest = CreateLookupSubTypeRequest.builder()
                .name("Manager")
                .build();

        when(masterLookupTypeRepository.findByCodeForUpdate("10002")).thenReturn(Optional.of(type));
        when(masterLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeDescriptionIgnoreCase("10002", "Manager")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> lookupService.addSubType("10002", subTypeRequest, adminPrincipal));
        assertTrue(ex.getMessage().contains("Lookup sub-type already exists with name 'Manager'"));
    }

    @Test
    @DisplayName("Deactivating system fixed sub type throws BusinessException (400)")
    void testDeactivateSubType_SystemFixed_ThrowsException() {
        MasterLookupSubType subType = MasterLookupSubType.builder()
                .id(1L)
                .lookupTypeCode("10001")
                .subTypeCode("1")
                .subTypeDescription("Entered")
                .isFixed(true)
                .build();

        when(masterLookupSubTypeRepository.findById(1L)).thenReturn(Optional.of(subType));
        when(masterLookupTypeRepository.findByCode("10001")).thenReturn(Optional.of(MasterLookupType.builder().code("10001").isFixed(true).build()));

        BusinessException ex = assertThrows(BusinessException.class, () -> lookupService.deactivateSubType(1L, adminPrincipal));
        assertTrue(ex.getMessage().contains("Cannot deactivate option under system-fixed"));
    }

    @Test
    @DisplayName("Search master lookup types with pagination returns PagedLookupResponse")
    void testSearchLookupTypes() {
        MasterLookupType t1 = MasterLookupType.builder().id(1L).code("10004").description("Loan Type").isActive(true).isFixed(false).build();
        Page<MasterLookupType> page = new PageImpl<>(List.of(t1), Pageable.ofSize(10), 1);

        when(masterLookupTypeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(masterLookupSubTypeRepository.countByLookupTypeCode("10004")).thenReturn(10L);

        PagedLookupResponse<LookupTypeResponse> result = lookupService.searchLookupTypes("Loan", "ACTIVE", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("10004", result.getItems().get(0).getCode());
        assertEquals("Loan Type", result.getItems().get(0).getName());
        assertEquals(10L, result.getItems().get(0).getSubTypeCount());
        assertNull(result.getItems().get(0).getSubTypes(), "subTypes list should be omitted in list/search view");
    }

    @Test
    @DisplayName("Sync master lookups into Bank DB propagates catalogue")
    void testSyncMasterLookupsToBank() {
        Organization org = Organization.builder()
                .id(1L)
                .bankCode("HDFC01")
                .dbName("los_hdfc01_db")
                .build();

        when(organizationRepository.findByCode("HDFC01")).thenReturn(Optional.of(org));

        MasterLookupType mt = MasterLookupType.builder().id(1L).code("10001").description("Status").isFixed(true).isActive(true).build();
        MasterLookupSubType mst = MasterLookupSubType.builder().id(1L).lookupTypeCode("10001").subTypeCode("1").subTypeDescription("Entered").isFixed(true).build();

        when(masterLookupTypeRepository.findAll()).thenReturn(List.of(mt));
        when(masterLookupSubTypeRepository.findAll()).thenReturn(List.of(mst));

        when(bankLookupTypeRepository.findByCode("10001")).thenReturn(Optional.empty());
        when(bankLookupTypeRepository.save(any(BankLookupType.class))).thenAnswer(i -> i.getArgument(0));

        when(bankLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode("10001", "1")).thenReturn(Optional.empty());
        when(bankLookupSubTypeRepository.save(any(BankLookupSubType.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> lookupService.syncMasterLookupsToBank("HDFC01"));
        verify(bankLookupTypeRepository, atLeastOnce()).save(any(BankLookupType.class));
        verify(bankLookupSubTypeRepository, atLeastOnce()).save(any(BankLookupSubType.class));
    }

    @Test
    @DisplayName("Admin can update bank lookup permissions")
    void testUpdateBankLookupPermissions() {
        Organization org = Organization.builder()
                .id(1L)
                .bankCode("HDFC01")
                .dbName("los_hdfc01_db")
                .build();

        when(organizationRepository.findByCode("HDFC01")).thenReturn(Optional.of(org));

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .build();

        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));
        when(bankLookupTypeRepository.save(any(BankLookupType.class))).thenAnswer(i -> i.getArgument(0));

        UpdateBankLookupPermissionsRequest req = UpdateBankLookupPermissionsRequest.builder()
                .permissions(Set.of("VIEW", "ADD", "ACTIVATE"))
                .build();

        LookupTypeResponse response = lookupService.updateBankLookupPermissions("HDFC01", "10004", req, adminPrincipal);

        assertNotNull(response);
        verify(bankLookupTypePermissionRepository, times(1)).deleteByLookupTypeCode("10004");
        verify(adminAuditService, times(1)).logAdminAction(any(), any(), eq("UPDATE_BANK_LOOKUP_PERMISSIONS"), any(), eq("HDFC01"), any(), any());
    }

    @Test
    @DisplayName("getActiveOptionsByLookupCode should return only active options ordered by display order")
    void testGetActiveOptionsByLookupCodeSuccess() {
        MasterLookupType type = MasterLookupType.builder()
                .code("10002")
                .description("Designation")
                .isActive(true)
                .build();
        when(masterLookupTypeRepository.findByCode("10002")).thenReturn(Optional.of(type));

        MasterLookupSubType opt1 = MasterLookupSubType.builder()
                .lookupTypeCode("10002")
                .subTypeCode("1")
                .subTypeDescription("Clerk")
                .isActive(true)
                .displayOrder(1)
                .build();
        MasterLookupSubType opt2 = MasterLookupSubType.builder()
                .lookupTypeCode("10002")
                .subTypeCode("2")
                .subTypeDescription("Officer")
                .isActive(true)
                .displayOrder(2)
                .build();

        when(masterLookupSubTypeRepository.findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc("10002"))
                .thenReturn(List.of(opt1, opt2));

        List<LookupOptionResponse> options = lookupService.getActiveOptionsByLookupCode("10002");
        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("1", options.get(0).getCode());
        assertEquals("Clerk", options.get(0).getDescription());
        assertEquals("2", options.get(1).getCode());
        assertEquals("Officer", options.get(1).getDescription());
    }

    @Test
    @DisplayName("getActiveOptionsByLookupCode should throw ResourceNotFoundException for unknown type code")
    void testGetActiveOptionsByLookupCodeUnknownThrows() {
        when(masterLookupTypeRepository.findByCode("99999")).thenReturn(Optional.empty());

        assertThrows(com.bank.los.common.exception.ResourceNotFoundException.class, () -> {
            lookupService.getActiveOptionsByLookupCode("99999");
        });
    }

    @Test
    @DisplayName("getActiveOptionsByLookupCode should throw ResourceNotFoundException for inactive type")
    void testGetActiveOptionsByLookupCodeInactiveThrows() {
        MasterLookupType inactiveType = MasterLookupType.builder()
                .code("10002")
                .description("Designation")
                .isActive(false)
                .build();
        when(masterLookupTypeRepository.findByCode("10002")).thenReturn(Optional.of(inactiveType));

        assertThrows(com.bank.los.common.exception.ResourceNotFoundException.class, () -> {
            lookupService.getActiveOptionsByLookupCode("10002");
        });
    }
}
