package com.bank.los.bank.lookup;

import com.bank.los.administration.lookup.dto.LookupSubTypeResponse;
import com.bank.los.administration.master.entity.MasterLookupSubType;
import com.bank.los.administration.master.repository.MasterLookupSubTypeRepository;
import com.bank.los.bank.audit.service.BankAuditService;
import com.bank.los.bank.auth.service.PermissionService;
import com.bank.los.bank.lookup.dto.BankCreateLookupSubTypeRequest;
import com.bank.los.bank.lookup.dto.BankUpdateLookupSubTypeRequest;
import com.bank.los.bank.lookup.service.BankLookupService;
import com.bank.los.bank.master.entity.BankLookupSubType;
import com.bank.los.bank.master.entity.BankLookupType;
import com.bank.los.bank.master.repository.BankLookupSubTypeRepository;
import com.bank.los.bank.master.repository.BankLookupTypeRepository;
import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.UnauthorizedException;
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
class BankLookupServiceTest {

    @Mock
    private BankLookupTypeRepository bankLookupTypeRepository;

    @Mock
    private BankLookupSubTypeRepository bankLookupSubTypeRepository;

    @Mock
    private MasterLookupSubTypeRepository masterLookupSubTypeRepository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private BankAuditService bankAuditService;

    @InjectMocks
    private BankLookupService bankLookupService;

    private UserPrincipal bankPrincipal;

    @BeforeEach
    void setUp() {
        bankPrincipal = UserPrincipal.builder()
                .id(10L)
                .email("admin@hdfcbank.com")
                .role("ADMIN")
                .organizationDbName("los_hdfc01_db")
                .organizationCode("HDFC01")
                .build();
    }

    @Test
    @DisplayName("Bank should add custom option under non-fixed lookup type (e.g. Designation 10002)")
    void testAddBankSubType_NonFixed_Success() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ADD))).thenReturn(true);

        BankLookupType designationType = BankLookupType.builder()
                .id(2L)
                .code("10002")
                .description("Designation")
                .isFixed(false)
                .isActive(true)
                .build();

        BankCreateLookupSubTypeRequest request = BankCreateLookupSubTypeRequest.builder()
                .subTypeCode("10")
                .subTypeDescription("Executive Vice President")
                .displayOrder(10)
                .build();

        when(bankLookupTypeRepository.findByCode("10002")).thenReturn(Optional.of(designationType));
        when(bankLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeCode("10002", "10")).thenReturn(false);
        when(bankLookupSubTypeRepository.save(any(BankLookupSubType.class))).thenAnswer(i -> {
            BankLookupSubType st = i.getArgument(0);
            st.setId(100L);
            return st;
        });

        LookupSubTypeResponse response = bankLookupService.addBankSubType(bankPrincipal, "10002", request);

        assertNotNull(response);
        assertEquals("10", response.getSubTypeCode());
        assertEquals("Executive Vice President", response.getSubTypeDescription());
        assertFalse(response.getIsFixed());
    }

    @Test
    @DisplayName("Bank user without LOOKUP_BANK_ADD permission throws UnauthorizedException")
    void testAddBankSubType_NoPermission_ThrowsUnauthorized() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ADD))).thenReturn(false);

        BankCreateLookupSubTypeRequest request = BankCreateLookupSubTypeRequest.builder()
                .subTypeCode("10")
                .subTypeDescription("Executive Vice President")
                .build();

        assertThrows(UnauthorizedException.class, () ->
                bankLookupService.addBankSubType(bankPrincipal, "10002", request));
    }

    @Test
    @DisplayName("Bank trying to add option under system-fixed lookup throws BusinessException")
    void testAddBankSubType_Fixed_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ADD))).thenReturn(true);

        BankLookupType roleType = BankLookupType.builder()
                .id(3L)
                .code("10003")
                .description("Role")
                .isFixed(true)
                .build();

        BankCreateLookupSubTypeRequest request = BankCreateLookupSubTypeRequest.builder()
                .subTypeCode("99")
                .subTypeDescription("Custom Hacker Role")
                .build();

        when(bankLookupTypeRepository.findByCode("10003")).thenReturn(Optional.of(roleType));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.addBankSubType(bankPrincipal, "10003", request));

        assertTrue(ex.getMessage().contains("system-fixed"));
    }

    @Test
    @DisplayName("Bank can import option from Master DB catalogue")
    void testImportOptionFromMaster_Success() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ADD_FROM_MASTER))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .build();

        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));
        when(bankLookupSubTypeRepository.existsByLookupTypeCodeAndSubTypeCode("10004", "8")).thenReturn(false);

        MasterLookupSubType masterOption = MasterLookupSubType.builder()
                .id(18L)
                .lookupTypeCode("10004")
                .typeDescription("Loan Type")
                .subTypeCode("8")
                .subTypeDescription("Professional Loan")
                .displayOrder(8)
                .build();

        when(masterLookupSubTypeRepository.findByLookupTypeCodeAndSubTypeCode("10004", "8")).thenReturn(Optional.of(masterOption));
        when(bankLookupSubTypeRepository.save(any(BankLookupSubType.class))).thenAnswer(i -> {
            BankLookupSubType st = i.getArgument(0);
            st.setId(108L);
            return st;
        });

        LookupSubTypeResponse response = bankLookupService.importOptionFromMaster(bankPrincipal, "10004", "8");

        assertNotNull(response);
        assertEquals("8", response.getSubTypeCode());
        assertEquals("Professional Loan", response.getSubTypeDescription());
        verify(bankLookupSubTypeRepository, times(1)).save(any(BankLookupSubType.class));
    }

    @Test
    @DisplayName("Bank can update custom option with LOOKUP_BANK_EDIT permission")
    void testUpdateBankSubType_Success() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_EDIT))).thenReturn(true);

        BankLookupSubType customOption = BankLookupSubType.builder()
                .id(50L)
                .lookupTypeCode("10002")
                .subTypeCode("5")
                .subTypeDescription("Manager")
                .isFixed(false)
                .build();

        when(bankLookupSubTypeRepository.findById(50L)).thenReturn(Optional.of(customOption));
        when(bankLookupTypeRepository.findByCode("10002")).thenReturn(Optional.of(
                BankLookupType.builder().code("10002").description("Designation").isFixed(false).build()
        ));
        when(bankLookupSubTypeRepository.save(any(BankLookupSubType.class))).thenAnswer(i -> i.getArgument(0));

        BankUpdateLookupSubTypeRequest updateReq = BankUpdateLookupSubTypeRequest.builder()
                .subTypeDescription("Senior Branch Manager")
                .displayOrder(5)
                .build();

        LookupSubTypeResponse response = bankLookupService.updateBankSubType(bankPrincipal, 50L, updateReq);

        assertNotNull(response);
        assertEquals("Senior Branch Manager", response.getSubTypeDescription());
    }

    @Test
    @DisplayName("Bank can activate and deactivate options with respective permissions")
    void testActivateAndDeactivateBankSubType() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_DEACTIVATE))).thenReturn(true);
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ACTIVATE))).thenReturn(true);

        BankLookupSubType customOption = BankLookupSubType.builder()
                .id(50L)
                .lookupTypeCode("10002")
                .subTypeCode("5")
                .subTypeDescription("Manager")
                .isActive(true)
                .isFixed(false)
                .build();

        when(bankLookupSubTypeRepository.findById(50L)).thenReturn(Optional.of(customOption));
        when(bankLookupSubTypeRepository.save(any(BankLookupSubType.class))).thenAnswer(i -> i.getArgument(0));

        LookupSubTypeResponse deactivated = bankLookupService.deactivateBankSubType(bankPrincipal, 50L);
        assertNotNull(deactivated);
        assertFalse(deactivated.getIsActive());

        LookupSubTypeResponse activated = bankLookupService.activateBankSubType(bankPrincipal, 50L);
        assertNotNull(activated);
        assertTrue(activated.getIsActive());
    }

    @Test
    @DisplayName("Bank trying to delete a system-fixed option throws BusinessException")
    void testDeleteBankSubType_FixedOption_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE))).thenReturn(true);

        BankLookupSubType fixedOption = BankLookupSubType.builder()
                .id(1L)
                .lookupTypeCode("10001")
                .subTypeCode("1")
                .subTypeDescription("Entered")
                .isFixed(true)
                .build();

        when(bankLookupSubTypeRepository.findById(1L)).thenReturn(Optional.of(fixedOption));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.deleteBankSubType(bankPrincipal, 1L));

        assertTrue(ex.getMessage().contains("System-fixed options cannot be deleted"));
    }

    @Test
    @DisplayName("Bank can delete a bank-custom option")
    void testDeleteBankSubType_CustomOption_Success() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE))).thenReturn(true);

        BankLookupType customParentType = BankLookupType.builder()
                .id(2L)
                .code("10002")
                .description("Designation")
                .isFixed(false)
                .build();

        BankLookupSubType customOption = BankLookupSubType.builder()
                .id(99L)
                .lookupTypeCode("10002")
                .subTypeCode("CUSTOM_99")
                .subTypeDescription("Custom Desk")
                .isFixed(false)
                .build();

        when(bankLookupSubTypeRepository.findById(99L)).thenReturn(Optional.of(customOption));
        when(bankLookupTypeRepository.findByCode("10002")).thenReturn(Optional.of(customParentType));

        assertDoesNotThrow(() -> bankLookupService.deleteBankSubType(bankPrincipal, 99L));
        verify(bankLookupSubTypeRepository, times(1)).delete(customOption);
    }

    @Test
    @DisplayName("Get options by lookup code returns active sub-types")
    void testGetOptionsByLookupCode() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_VIEW))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .canView(true)
                .build();

        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));

        BankLookupSubType option1 = BankLookupSubType.builder()
                .id(1L)
                .lookupTypeCode("10004")
                .subTypeCode("1")
                .subTypeDescription("Personal Loan")
                .isActive(true)
                .displayOrder(1)
                .build();

        BankLookupSubType option2 = BankLookupSubType.builder()
                .id(2L)
                .lookupTypeCode("10004")
                .subTypeCode("2")
                .subTypeDescription("Home Loan")
                .isActive(true)
                .displayOrder(2)
                .build();

        when(bankLookupSubTypeRepository.findByLookupTypeCodeAndIsActiveTrueOrderByDisplayOrderAscIdAsc("10004"))
                .thenReturn(List.of(option1, option2));

        List<LookupSubTypeResponse> options = bankLookupService.getOptionsByLookupCode(bankPrincipal, "10004");

        assertNotNull(options);
        assertEquals(2, options.size());
        assertEquals("Personal Loan", options.get(0).getSubTypeDescription());
        assertEquals("Home Loan", options.get(1).getSubTypeDescription());
    }

    @Test
    @DisplayName("Dual Authorization: User has LOOKUP_BANK_EDIT, but Loan Type canEdit=false -> DENY")
    void testDualAuthorization_EditPermissionAllowed_CapabilityDisabled_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_EDIT))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .canEdit(false)
                .build();

        BankLookupSubType subType = BankLookupSubType.builder()
                .id(20L)
                .lookupTypeCode("10004")
                .subTypeCode("2")
                .subTypeDescription("Home Loan")
                .isFixed(false)
                .build();

        when(bankLookupSubTypeRepository.findById(20L)).thenReturn(Optional.of(subType));
        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));

        BankUpdateLookupSubTypeRequest req = BankUpdateLookupSubTypeRequest.builder()
                .subTypeDescription("Updated Home Loan")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.updateBankSubType(bankPrincipal, 20L, req));

        assertTrue(ex.getMessage().contains("does not allow editing"));
    }

    @Test
    @DisplayName("Dual Authorization: User has LOOKUP_BANK_ADD, but Loan Type canAdd=false -> DENY")
    void testDualAuthorization_AddPermissionAllowed_CapabilityDisabled_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_ADD))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .canAdd(false)
                .build();

        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));

        BankCreateLookupSubTypeRequest req = BankCreateLookupSubTypeRequest.builder()
                .subTypeCode("99")
                .subTypeDescription("Gold Loan")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.addBankSubType(bankPrincipal, "10004", req));

        assertTrue(ex.getMessage().contains("does not allow adding custom options"));
    }

    @Test
    @DisplayName("Dual Authorization: User has LOOKUP_BANK_DELETE, but Loan Type canDelete=false -> DENY")
    void testDualAuthorization_DeletePermissionAllowed_CapabilityDisabled_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_DELETE))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .canDelete(false)
                .build();

        BankLookupSubType subType = BankLookupSubType.builder()
                .id(20L)
                .lookupTypeCode("10004")
                .subTypeCode("2")
                .subTypeDescription("Home Loan")
                .isFixed(false)
                .build();

        when(bankLookupSubTypeRepository.findById(20L)).thenReturn(Optional.of(subType));
        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.deleteBankSubType(bankPrincipal, 20L));

        assertTrue(ex.getMessage().contains("does not allow deleting"));
    }

    @Test
    @DisplayName("Dual Authorization: View lookup when canView=false throws BusinessException")
    void testDualAuthorization_ViewDisabled_ThrowsBusinessException() {
        when(permissionService.hasEffectivePermission(any(), eq(ApplicationConstants.Permissions.LOOKUP_BANK_VIEW))).thenReturn(true);

        BankLookupType loanType = BankLookupType.builder()
                .id(4L)
                .code("10004")
                .description("Loan Type")
                .isFixed(false)
                .canView(false)
                .build();

        when(bankLookupTypeRepository.findByCode("10004")).thenReturn(Optional.of(loanType));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                bankLookupService.getLookupTypeByCode(bankPrincipal, "10004"));

        assertTrue(ex.getMessage().contains("viewing is disabled"));
    }
}
