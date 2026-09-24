package com.bank.los.bank.auth;

import com.bank.los.bank.auth.dto.request.LoginRequest;
import com.bank.los.bank.auth.dto.request.VerifyOtpRequest;
import com.bank.los.bank.auth.dto.response.LoginResponse;
import com.bank.los.bank.auth.service.AuthenticationService;
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
    @DisplayName("Should successfully authenticate Internal Super Admin on Master DB (single step)")
    void testInternalAdminLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("admin@losplatform.com")
                .password("Admin@123")
                .build();

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("INTERNAL_ADMIN", response.getUser().getRole());
        assertEquals("INTERNAL", response.getUser().getUserType());
        assertEquals("/dashboard/internal-admin", response.getDashboardUrl());
    }

    @Test
    @DisplayName("Should enforce mandatory 2FA OTP for Bank Admin on Tenant DB (2-step flow)")
    void testBankAdminLoginWith2FA() {
        LoginRequest step1Request = LoginRequest.builder()
                .email("admin@hdfcbank.com")
                .password("Admin@123")
                .build();

        // Step 1: Login credentials verification -> returns 2FA challenge
        LoginResponse step1 = authenticationService.login(step1Request);
        assertNotNull(step1);
        assertTrue(Boolean.TRUE.equals(step1.getOtpRequired()), "2FA OTP must be mandatory for tenant staff");
        assertNotNull(step1.getTempSessionToken());
        assertNotNull(step1.getDevOtp(), "Dev OTP should be available in dev/test mode");

        // Step 2: Verify OTP
        VerifyOtpRequest step2Request = VerifyOtpRequest.builder()
                .tempSessionToken(step1.getTempSessionToken())
                .otp(step1.getDevOtp())
                .build();

        LoginResponse step2 = authenticationService.verifyOtp(step2Request);
        assertNotNull(step2);
        assertNotNull(step2.getAccessToken());
        assertEquals("ADMIN", step2.getUser().getRole());
        assertEquals("STAFF", step2.getUser().getUserType());
        assertEquals("HDFC01", step2.getUser().getOrganizationCode());
        assertEquals("/dashboard/admin", step2.getDashboardUrl());
    }

    @Test
    @DisplayName("Should successfully authenticate Bank Maker on Tenant DB via 2FA")
    void testMakerLoginWith2FA() {
        LoginRequest step1Request = LoginRequest.builder()
                .email("maker@hdfcbank.com")
                .password("Maker@123")
                .build();

        LoginResponse step1 = authenticationService.login(step1Request);
        assertTrue(Boolean.TRUE.equals(step1.getOtpRequired()));

        LoginResponse step2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                .tempSessionToken(step1.getTempSessionToken())
                .otp(step1.getDevOtp())
                .build());

        assertNotNull(step2);
        assertEquals("MAKER", step2.getUser().getRole());
        assertEquals("/dashboard/maker", step2.getDashboardUrl());
        assertTrue(step2.getPermissions().contains("LOAN_APPLICATION_CREATE"));
    }

    @Test
    @DisplayName("Should successfully authenticate Bank Checker on Tenant DB via 2FA")
    void testCheckerLoginWith2FA() {
        LoginRequest step1Request = LoginRequest.builder()
                .email("checker@hdfcbank.com")
                .password("Checker@123")
                .build();

        LoginResponse step1 = authenticationService.login(step1Request);
        assertTrue(Boolean.TRUE.equals(step1.getOtpRequired()));

        LoginResponse step2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                .tempSessionToken(step1.getTempSessionToken())
                .otp(step1.getDevOtp())
                .build());

        assertNotNull(step2);
        assertEquals("CHECKER", step2.getUser().getRole());
        assertEquals("/dashboard/checker", step2.getDashboardUrl());
        assertTrue(step2.getPermissions().contains("LOAN_APPLICATION_APPROVE"));
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
