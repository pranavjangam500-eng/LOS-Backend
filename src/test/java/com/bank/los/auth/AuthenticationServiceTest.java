package com.bank.los.auth;

import com.bank.los.auth.dto.request.LoginRequest;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.service.AuthenticationService;
import com.bank.los.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("Should successfully authenticate Internal Admin on Master DB")
    void testInternalAdminLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@losplatform.com")
                .password("Admin@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("INTERNAL_ADMIN", response.getUser().getRole());
        assertEquals("INTERNAL", response.getUser().getUserType());
        assertEquals("/dashboard/internal-admin", response.getDashboardUrl());
    }

    @Test
    @DisplayName("Should successfully authenticate Bank Super Admin on Tenant DB")
    void testTenantSuperAdminLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("superadmin@hdfcbank.com")
                .password("Admin@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("SUPER_ADMIN", response.getUser().getRole());
        assertEquals("STAFF", response.getUser().getUserType());
        assertEquals("HDFC01", response.getUser().getOrganizationCode());
        assertEquals("/dashboard/tenant-admin", response.getDashboardUrl());
    }

    @Test
    @DisplayName("Should successfully authenticate Bank Maker on Tenant DB")
    void testMakerLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("maker@hdfcbank.com")
                .password("Maker@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("MAKER", response.getUser().getRole());
        assertEquals("/dashboard/maker", response.getDashboardUrl());
        assertTrue(response.getPermissions().contains("LOAN_APPLICATION_CREATE"));
    }

    @Test
    @DisplayName("Should successfully authenticate Bank Checker on Tenant DB")
    void testCheckerLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("checker@hdfcbank.com")
                .password("Checker@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("CHECKER", response.getUser().getRole());
        assertEquals("/dashboard/checker", response.getDashboardUrl());
        assertTrue(response.getPermissions().contains("LOAN_APPLICATION_APPROVE"));
    }

    @Test
    @DisplayName("Should successfully authenticate Customer on Customer Portal")
    void testCustomerLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("rajesh.kumar@gmail.com")
                .password("Customer@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("CUSTOMER", response.getUser().getRole());
        assertEquals("CUSTOMER", response.getUser().getUserType());
        assertEquals("/dashboard/customer", response.getDashboardUrl());
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for invalid password")
    void testInvalidPasswordLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@losplatform.com")
                .password("WrongPassword")
                .build();

        assertThrows(UnauthorizedException.class, () -> authenticationService.login(request));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException for non-existent user")
    void testNonExistentUserLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@nonexistent.com")
                .password("Random@123")
                .build();

        assertThrows(UnauthorizedException.class, () -> authenticationService.login(request));
    }
}
