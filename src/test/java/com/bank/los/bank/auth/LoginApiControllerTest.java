package com.bank.los.bank.auth;

import com.bank.los.otp.service.OtpEmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class LoginApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OtpEmailService otpEmailService;

    @Test
    @DisplayName("POST /api/v1/auth/login should dispatch OTP to email, omit devOtp from response, and allow verify-otp")
    void testLoginDispatchesOtpToEmailAndExcludesDevOtp() throws Exception {
        // Step 1: POST /api/v1/auth/login
        String loginPayload = """
            {
                "email": "demo@allianzapay.com",
                "password": "Admin@123"
            }
        """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otpRequired").value(true))
                .andExpect(jsonPath("$.data.tempSessionToken").isString())
                .andExpect(jsonPath("$.data.devOtp").doesNotExist())
                .andReturn();

        // Verify email was dispatched via OtpEmailService
        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpEmailService, atLeastOnce()).sendOtpEmail(eq("demo@allianzapay.com"), otpCaptor.capture());
        String receivedOtp = otpCaptor.getValue();
        assertNotNull(receivedOtp, "OTP sent to email must not be null");
        assertEquals(6, receivedOtp.length(), "OTP must be 6 digits");

        // Extract tempSessionToken from login response
        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String tempSessionToken = jsonNode.path("data").path("tempSessionToken").asText();
        assertNotNull(tempSessionToken);

        // Step 2: POST /api/v1/auth/verify-otp using received email OTP
        String verifyPayload = String.format("""
            {
                "tempSessionToken": "%s",
                "otp": "%s"
            }
        """, tempSessionToken, receivedOtp);

        mockMvc.perform(post("/api/v1/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andExpect(jsonPath("$.data.user.email").value("demo@allianzapay.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should support 'identifier' alias in request body")
    void testLoginSupportsIdentifierAlias() throws Exception {
        String loginPayload = """
            {
                "identifier": "admin@hdfcbank.com",
                "password": "Admin@123"
            }
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otpRequired").value(true))
                .andExpect(jsonPath("$.data.devOtp").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/admin/auth/login and /api/v1/auth/internal/login should authenticate Master internal admin")
    void testMasterInternalAdminEndpoints() throws Exception {
        String internalPayload = """
            {
                "email": "admin@losplatform.com",
                "password": "Admin@123"
            }
        """;

        // Test 1: Dedicated administration endpoint
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(internalPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.user.role").value("INTERNAL_ADMIN"))
                .andExpect(jsonPath("$.data.user.userType").value("INTERNAL"));

        // Test 2: Dedicated /auth/internal/login endpoint
        mockMvc.perform(post("/api/v1/auth/internal/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(internalPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString());

        // Test 3: Banking endpoint rejects Master internal admin
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(internalPayload))
                .andExpect(status().isUnauthorized());

        // Test 4: Internal admin endpoint rejects banking staff user
        String bankUserPayload = """
            {
                "email": "admin@hdfcbank.com",
                "password": "Admin@123"
            }
        """;
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bankUserPayload))
                .andExpect(status().isUnauthorized());
    }
}
