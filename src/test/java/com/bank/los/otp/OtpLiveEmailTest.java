package com.bank.los.otp;

import com.bank.los.otp.service.OtpEmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("local")
class OtpLiveEmailTest {

    @Autowired
    @Qualifier("otpEmailService")
    private OtpEmailService otpEmailService;

    @Test
    @DisplayName("Verify live dispatch or simulated dispatch completes without unhandled exception")
    void testLiveOrSimulatedDispatch() {
        assertDoesNotThrow(() -> {
            otpEmailService.sendOtpEmail("demo@allianzapay.com", "882194");
        });
    }
}
