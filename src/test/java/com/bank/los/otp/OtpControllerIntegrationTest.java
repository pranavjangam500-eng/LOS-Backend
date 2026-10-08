package com.bank.los.otp;

import com.bank.los.otp.repository.OtpRepository;
import com.bank.los.otp.service.OtpEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class OtpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @Qualifier("otpEmailService")
    private OtpEmailService otpEmailService;

    @Autowired
    @Qualifier("inMemoryOtpRepository")
    private OtpRepository otpRepository;

    @BeforeEach
    public void cleanStore() {
        otpRepository.clearAll();
    }

    @Test
    @DisplayName("HTTP POST /api/v1/otp/send: Successfully sends OTP and returns 200 OK")
    void testSendOtpEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "http-test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("OTP sent successfully")));

        Mockito.verify(otpEmailService, Mockito.times(1))
                .sendOtpEmail(eq("http-test@example.com"), anyString());
    }

    @Test
    @DisplayName("HTTP POST /api/otp/send: Alias endpoint operates identically")
    void testAliasSendEndpoint() throws Exception {
        mockMvc.perform(post("/api/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "alias-test@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("OTP sent successfully")));
    }

    @Test
    @DisplayName("HTTP POST /api/v1/otp/send: Cooldown enforcement returns HTTP 429")
    void testCooldownEndpoint() throws Exception {
        // First request: OK
        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "cooldown-http@example.com"))))
                .andExpect(status().isOk());

        // Immediate second request: 429 Too Many Requests
        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "cooldown-http@example.com"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.cooldownRemaining", greaterThan(0)))
                .andExpect(jsonPath("$.message", containsString("Please wait")));
    }

    @Test
    @DisplayName("HTTP POST /api/v1/otp/send: Validation rejects invalid email")
    void testInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HTTP POST /api/v1/otp/verify: End-to-end send -> verify -> wrong code -> single use")
    void testEndToEndVerifyFlow() throws Exception {
        final String[] capturedCode = new String[1];
        Mockito.doAnswer(invocation -> {
            capturedCode[0] = invocation.getArgument(1);
            return null;
        }).when(otpEmailService).sendOtpEmail(eq("flow@example.com"), anyString());

        // 1. Send OTP
        mockMvc.perform(post("/api/v1/otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "flow@example.com"))))
                .andExpect(status().isOk());

        // 2. Wrong OTP code -> 400 Bad Request
        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "flow@example.com",
                                "otp", "000000"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Incorrect OTP code")));

        // 3. Correct OTP code -> 200 OK
        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "flow@example.com",
                                "otp", capturedCode[0]
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Email Verified Successfully")))
                .andExpect(jsonPath("$.verifiedEmail", is("flow@example.com")))
                .andExpect(jsonPath("$.verifiedAt", notNullValue()));

        // 4. Replay of same OTP code -> 400 Bad Request (Single-use security)
        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "flow@example.com",
                                "otp", capturedCode[0]
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("No active OTP found")));
    }
}
