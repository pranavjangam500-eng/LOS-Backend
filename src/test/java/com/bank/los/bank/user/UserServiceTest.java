package com.bank.los.bank.user;

import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.administration.organization.service.OrganizationService;
import com.bank.los.bank.auth.dto.request.LoginRequest;
import com.bank.los.bank.auth.dto.request.VerifyOtpRequest;
import com.bank.los.bank.auth.dto.response.LoginResponse;
import com.bank.los.bank.auth.service.AuthenticationService;
import com.bank.los.bank.user.dto.CreateUserRequest;
import com.bank.los.bank.user.dto.UserResponse;
import com.bank.los.bank.user.service.UserService;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.otp.service.OtpEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class UserServiceTest {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @MockBean
    private OtpEmailService otpEmailService;

    private String lastOtp;

    @BeforeEach
    void setupMock() {
        Mockito.doAnswer(invocation -> {
            lastOtp = invocation.getArgument(1);
            return null;
        }).when(otpEmailService).sendOtpEmail(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @DisplayName("Should register bank with organizational info and allow Super Admin to create Bank Admin who logs in")
    void testBankOnboardingAndAdminAssignmentFlow() {
        // 1. Super Admin registers a new Bank (only organization info)
        String orgCode = "ICICI99";
        String dbName = "los_icici99_db";

        CreateOrganizationRequest orgRequest = CreateOrganizationRequest.builder()
                .bankCode(orgCode)
                .bankName("ICICI Bank Ltd")
                .legalName("ICICI Bank Limited")
                .bankType("BANK")
                .registrationNumber("REG-MH-2024-9900")
                .pan("AAACI1234F")
                .cin("L65191PN1994PLC076333")
                .regulatoryAuthorityId(java.util.UUID.randomUUID())
                .regulatoryStatus("ACTIVE")
                .country("India")
                .contactEmail("contact@icicibank.com")
                .contactPhone("+912240001000")
                .dbName(dbName)
                .dbHost("localhost")
                .dbPort(5432)
                .build();

        OrganizationResponse orgResponse = organizationService.createOrganization(orgRequest);
        assertNotNull(orgResponse);
        assertNotNull(orgResponse.getId());
        assertEquals("contact@icicibank.com", orgResponse.getContactEmail());
        assertEquals("ICICI Bank Ltd", orgResponse.getBankName());
        assertEquals("BANK", orgResponse.getBankType());
        assertEquals("ICICI99", orgResponse.getBankCode());

        // 2. Super Admin creates a Bank Admin user for this new Bank
        UserPrincipal superAdminPrincipal = UserPrincipal.builder()
                .id(1L)
                .email("admin@losplatform.com")
                .userCode("EMP-MST-001")
                .role("INTERNAL_ADMIN")
                .userType("INTERNAL")
                .organizationDbName(OrganizationContext.MASTER_DB_NAME)
                .tenantDbName(OrganizationContext.MASTER_DB_NAME)
                .organizationCode("MASTER")
                .active(true)
                .build();

        CreateUserRequest adminUserRequest = CreateUserRequest.builder()
                .organizationId(orgResponse.getPkid())
                .username("icici_admin")
                .email("admin@icicibank.com")
                .password("Admin@123")
                .roleName("3")
                .designation("5")
                .firstName("Aditya")
                .lastName("Kapoor")
                .mobile("+919811122233")
                .status("OPERATIVE")
                .build();

        UserResponse userResponse = userService.createUser(superAdminPrincipal, adminUserRequest);
        assertNotNull(userResponse);
        assertNotNull(userResponse.getOrganizationCode());
        assertEquals("icici_admin", userResponse.getUsername());
        assertEquals("admin@icicibank.com", userResponse.getEmail());
        assertEquals("ADMIN", userResponse.getRoleName());
        assertEquals("OPERATIVE", userResponse.getStatus());
        assertEquals("5", userResponse.getDesignationCode());
        assertEquals("Manager", userResponse.getDesignationName());

        // Test invalid designation code should throw BusinessException (400)
        CreateUserRequest invalidDesigRequest = CreateUserRequest.builder()
                .organizationId(orgResponse.getPkid())
                .username("invalid_desig_user")
                .email("invalid_desig@icicibank.com")
                .password("Admin@123")
                .roleName("3")
                .designation("99999")
                .firstName("Test")
                .lastName("User")
                .mobile("+919811122244")
                .status("OPERATIVE")
                .build();

        assertThrows(com.bank.los.common.exception.BusinessException.class, () -> {
            userService.createUser(superAdminPrincipal, invalidDesigRequest);
        });

        // Test invalid designation name "Manager" (name instead of code "5") in Internal Admin flow -> 400
        CreateUserRequest nameDesigRequest = CreateUserRequest.builder()
                .organizationId(orgResponse.getPkid())
                .username("name_desig_user")
                .email("name_desig@icicibank.com")
                .password("Admin@123")
                .roleName("3")
                .designation("Manager")
                .firstName("Test")
                .lastName("User")
                .mobile("+919811122299")
                .status("OPERATIVE")
                .build();

        var exInternal = assertThrows(com.bank.los.common.exception.BusinessException.class, () -> {
            userService.createUser(superAdminPrincipal, nameDesigRequest);
        });
        assertTrue(exInternal.getMessage().contains("Must be an active option code under lookup type 10002"));

        // Test invalid designation name "Manager" in Bank-side flow -> 400
        UserPrincipal bankAdminPrincipal = UserPrincipal.builder()
                .id(userResponse.getId())
                .email(userResponse.getEmail())
                .userCode(userResponse.getEmpNo())
                .role("ADMIN")
                .userType("STAFF")
                .organizationId(orgResponse.getPkid())
                .organizationCode(orgResponse.getBankCode())
                .organizationDbName(orgResponse.getDbName())
                .active(true)
                .build();

        var exBank = assertThrows(com.bank.los.common.exception.BusinessException.class, () -> {
            userService.createUser(bankAdminPrincipal, nameDesigRequest);
        });
        assertTrue(exBank.getMessage().contains("Must be an active option code under lookup type 10002"));

        // Test invalid role code should throw BusinessException (400)
        CreateUserRequest invalidRoleRequest = CreateUserRequest.builder()
                .organizationId(orgResponse.getPkid())
                .username("invalid_role_user")
                .email("invalid_role@icicibank.com")
                .password("Admin@123")
                .roleName("99999")
                .designation("5")
                .firstName("Test")
                .lastName("User")
                .mobile("+919811122255")
                .status("OPERATIVE")
                .build();

        assertThrows(com.bank.los.common.exception.BusinessException.class, () -> {
            userService.createUser(superAdminPrincipal, invalidRoleRequest);
        });

        // 3. Newly created Bank Admin logs in and gets routed to ICICI bank dashboard
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@icicibank.com")
                .password("Admin@123")
                .build();

        LoginResponse loginResp1 = authenticationService.login(loginRequest);
        assertNotNull(loginResp1);
        assertTrue(Boolean.TRUE.equals(loginResp1.getOtpRequired()));

        // Complete 2FA
        String otpToUse = loginResp1.getDevOtp() != null ? loginResp1.getDevOtp() : lastOtp;
        if (otpToUse != null) {
            LoginResponse loginResp2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                    .tempSessionToken(loginResp1.getTempSessionToken())
                    .otp(otpToUse)
                    .build());

            assertNotNull(loginResp2);
            assertEquals("ADMIN", loginResp2.getUser().getRole());
            assertEquals(userResponse.getOrganizationCode(), loginResp2.getUser().getOrganizationCode());
            assertEquals("/dashboard/admin", loginResp2.getDashboardUrl());
        }


        // 4. Super Admin lists users of this bank
        List<UserResponse> iciciUsers = userService.getAllUsers(superAdminPrincipal, orgResponse.getPkid());
        assertFalse(iciciUsers.isEmpty());
        assertTrue(iciciUsers.stream().anyMatch(u -> "admin@icicibank.com".equals(u.getEmail())));
    }

    @Test
    @DisplayName("Should create user with manual unique empNo successfully")
    void testCreateUserWithManualEmpNo() {
        UserPrincipal superAdmin = createSuperAdminPrincipal();
        CreateUserRequest request = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("unique_emp_test_1")
                .email("unique1@hdfcbank.com")
                .password("Test@1234")
                .roleName("MAKER")
                .designation("5")
                .firstName("Test")
                .lastName("User1")
                .mobile("+919900112233")
                .empNo("EMP-TEST-UNIQUE-01")
                .build();

        UserResponse response = userService.createUser(superAdmin, request);
        assertNotNull(response);
        assertEquals("EMP-TEST-UNIQUE-01", response.getEmpNo());
        assertEquals("unique_emp_test_1", response.getUsername());
    }

    @Test
    @DisplayName("Should reject user creation when empNo already exists in same organization")
    void testCreateUserWithDuplicateEmpNoInSameBank() {
        UserPrincipal superAdmin = createSuperAdminPrincipal();
        CreateUserRequest user1 = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("emp_dup_test_1")
                .email("dup1@hdfcbank.com")
                .password("Test@1234")
                .roleName("MAKER")
                .designation("5")
                .firstName("First")
                .lastName("Maker")
                .mobile("+919900112244")
                .empNo("EMP-DUP-001")
                .build();
        userService.createUser(superAdmin, user1);

        CreateUserRequest user2 = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("emp_dup_test_2")
                .email("dup2@hdfcbank.com")
                .password("Test@1234")
                .roleName("CHECKER")
                .designation("5")
                .firstName("Second")
                .lastName("Checker")
                .mobile("+919900112255")
                .empNo("EMP-DUP-001")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(superAdmin, user2));
        assertEquals("EMP_NO_EXISTS", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("EMP-DUP-001"));
    }

    @Test
    @DisplayName("Should reject user creation when empNo already exists globally across organizations")
    void testCreateUserWithDuplicateEmpNoAcrossBanks() {
        UserPrincipal superAdmin = createSuperAdminPrincipal();
        CreateUserRequest user1 = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("emp_global_dup_1")
                .email("globaldup1@hdfcbank.com")
                .password("Test@1234")
                .roleName("MAKER")
                .designation("5")
                .firstName("Hdfc")
                .lastName("User")
                .mobile("+919900112266")
                .empNo("EMP-GLOBAL-001")
                .build();
        userService.createUser(superAdmin, user1);

        // Try to create in BAJAJ02 with the same empNo
        CreateUserRequest user2 = CreateUserRequest.builder()
                .organizationCode("BAJAJ02")
                .username("emp_global_dup_2")
                .email("globaldup2@bajaj.com")
                .password("Test@1234")
                .roleName("MAKER")
                .designation("5")
                .firstName("Bajaj")
                .lastName("User")
                .mobile("+919900112277")
                .empNo("EMP-GLOBAL-001")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.createUser(superAdmin, user2));
        assertEquals("EMP_NO_EXISTS", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("EMP-GLOBAL-001"));
    }

    @Test
    @DisplayName("Should auto-generate unique empNo when not provided")
    void testAutoGeneratedEmpNoIsUnique() {
        UserPrincipal superAdmin = createSuperAdminPrincipal();
        CreateUserRequest user1 = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("autogen_user_1")
                .email("autogen1@hdfcbank.com")
                .password("Test@1234")
                .roleName("MAKER")
                .designation("5")
                .firstName("Auto")
                .lastName("One")
                .mobile("+919900112288")
                .build();
        UserResponse resp1 = userService.createUser(superAdmin, user1);

        CreateUserRequest user2 = CreateUserRequest.builder()
                .organizationCode("HDFC01")
                .username("autogen_user_2")
                .email("autogen2@hdfcbank.com")
                .password("Test@1234")
                .roleName("CHECKER")
                .designation("5")
                .firstName("Auto")
                .lastName("Two")
                .mobile("+919900112299")
                .build();
        UserResponse resp2 = userService.createUser(superAdmin, user2);

        assertNotNull(resp1.getEmpNo());
        assertNotNull(resp2.getEmpNo());
        assertFalse(resp1.getEmpNo().isBlank());
        assertFalse(resp2.getEmpNo().isBlank());
        assertNotEquals(resp1.getEmpNo(), resp2.getEmpNo());
    }

    private UserPrincipal createSuperAdminPrincipal() {
        return UserPrincipal.builder()
                .id(1L)
                .email("admin@losplatform.com")
                .userCode("EMP-MST-001")
                .role("INTERNAL_ADMIN")
                .userType("INTERNAL")
                .organizationDbName(OrganizationContext.MASTER_DB_NAME)
                .tenantDbName(OrganizationContext.MASTER_DB_NAME)
                .organizationCode("MASTER")
                .active(true)
                .build();
    }
}
