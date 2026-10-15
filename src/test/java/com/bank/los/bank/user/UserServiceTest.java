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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

        // Complete 2FA if devOtp provided
        if (loginResp1.getDevOtp() != null) {
            LoginResponse loginResp2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                    .tempSessionToken(loginResp1.getTempSessionToken())
                    .otp(loginResp1.getDevOtp())
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

}
