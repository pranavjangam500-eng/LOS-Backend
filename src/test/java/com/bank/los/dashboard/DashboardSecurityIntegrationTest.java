package com.bank.los.dashboard;

import com.bank.los.auth.dto.request.LoginRequest;
import com.bank.los.auth.dto.request.VerifyOtpRequest;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class DashboardSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationService authenticationService;

    private String internalAdminToken;
    private String tenantAdminToken;
    private String makerToken;
    private String checkerToken;
    private String customerToken;

    @BeforeEach
    void setUp() {
        // 1. Internal Super Admin (single-step)
        LoginResponse adminRes = authenticationService.login(LoginRequest.builder()
                .email("admin@losplatform.com").password("Admin@123").build());
        internalAdminToken = adminRes.getAccessToken();

        // 2. Bank Admin (2FA)
        LoginResponse superAdminStep1 = authenticationService.login(LoginRequest.builder()
                .email("admin@hdfcbank.com").password("Admin@123").build());
        LoginResponse superAdminStep2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                .tempSessionToken(superAdminStep1.getTempSessionToken())
                .otp(superAdminStep1.getDevOtp()).build());
        tenantAdminToken = superAdminStep2.getAccessToken();

        // 3. Bank Maker (2FA)
        LoginResponse makerStep1 = authenticationService.login(LoginRequest.builder()
                .email("maker@hdfcbank.com").password("Maker@123").build());
        LoginResponse makerStep2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                .tempSessionToken(makerStep1.getTempSessionToken())
                .otp(makerStep1.getDevOtp()).build());
        makerToken = makerStep2.getAccessToken();

        // 4. Bank Checker (2FA)
        LoginResponse checkerStep1 = authenticationService.login(LoginRequest.builder()
                .email("checker@hdfcbank.com").password("Checker@123").build());
        LoginResponse checkerStep2 = authenticationService.verifyOtp(VerifyOtpRequest.builder()
                .tempSessionToken(checkerStep1.getTempSessionToken())
                .otp(checkerStep1.getDevOtp()).build());
        checkerToken = checkerStep2.getAccessToken();

        // 5. Customer (single-step)
        LoginResponse custRes = authenticationService.login(LoginRequest.builder()
                .email("rajesh.kumar@gmail.com").password("Customer@123").build());
        customerToken = custRes.getAccessToken();
    }

    @Test
    @DisplayName("INTERNAL_ADMIN can access Internal Admin Dashboard")
    void testInternalAdminAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/internal-admin")
                        .header("Authorization", "Bearer " + internalAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.panelTitle").value("LOS Platform Master Administration"));
    }

    @Test
    @DisplayName("Bank ADMIN can access Tenant Admin Dashboard")
    void testTenantAdminAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/tenant-admin")
                        .header("Authorization", "Bearer " + tenantAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bankCode").value("HDFC01"));
    }

    @Test
    @DisplayName("MAKER cannot access Checker Dashboard (403 Forbidden)")
    void testMakerForbiddenOnCheckerDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/checker")
                        .header("Authorization", "Bearer " + makerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("CHECKER can access Checker Dashboard")
    void testCheckerAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/checker")
                        .header("Authorization", "Bearer " + checkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvalQueue").isArray());
    }

    @Test
    @DisplayName("CUSTOMER can access Customer Dashboard")
    void testCustomerAccess() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/customer")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerName").value("Rajesh Kumar"));
    }

    @Test
    @DisplayName("Unauthenticated request to Dashboard is rejected with 401 Unauthorized")
    void testUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/internal-admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
