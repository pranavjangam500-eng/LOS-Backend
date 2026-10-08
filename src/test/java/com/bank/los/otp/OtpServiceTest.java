package com.bank.los.otp;

import com.bank.los.otp.model.OtpRecord;
import com.bank.los.otp.repository.InMemoryOtpRepository;
import com.bank.los.otp.repository.OtpRepository;
import com.bank.los.otp.service.OtpEmailService;
import com.bank.los.otp.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class OtpServiceTest {

    private OtpEmailService emailService;
    private OtpRepository otpRepository;
    private OtpService otpService;

    @BeforeEach
    public void setUp() {
        emailService = Mockito.mock(OtpEmailService.class);
        otpRepository = new InMemoryOtpRepository();
        otpService = new OtpService(emailService, otpRepository);
    }

    @Test
    @DisplayName("Send OTP: Dispatches email and creates valid OTP record")
    void testSendOtp() {
        OtpService.SendResult result = otpService.sendOtp("test@example.com");

        assertTrue(result.success());
        assertNull(result.cooldownRemaining());
        assertTrue(otpRepository.findByEmail("test@example.com").isPresent());
        Mockito.verify(emailService, Mockito.times(1))
                .sendOtpEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("Cooldown / Resend: Resending within 60s is rejected with remaining seconds")
    void testCooldown() {
        OtpService.SendResult firstSend = otpService.sendOtp("cooldown@example.com");
        assertTrue(firstSend.success());

        OtpService.SendResult resend = otpService.sendOtp("cooldown@example.com");

        assertFalse(resend.success());
        assertNotNull(resend.cooldownRemaining());
        assertTrue(resend.cooldownRemaining() > 0 && resend.cooldownRemaining() <= 60);
        assertTrue(resend.message().contains("Please wait"));
    }

    @Test
    @DisplayName("Wrong OTP: Decreases attempt count and returns remaining attempts")
    void testWrongOtp() {
        otpService.sendOtp("wrong@example.com");
        OtpService.VerifyResult result = otpService.verifyOtp("wrong@example.com", "000000");

        assertFalse(result.success());
        assertTrue(result.message().contains("Incorrect OTP code"));
        assertTrue(result.message().contains("4 attempt(s) remaining"));
    }

    @Test
    @DisplayName("Attempt Limit: Exceeding 5 wrong attempts invalidates OTP")
    void testMaxAttemptsInvalidation() {
        otpService.sendOtp("bruteforce@example.com");

        for (int i = 0; i < 5; i++) {
            OtpService.VerifyResult result = otpService.verifyOtp("bruteforce@example.com", "11111" + i);
            assertFalse(result.success());
        }

        // OTP should now be invalidated / removed
        assertTrue(otpRepository.findByEmail("bruteforce@example.com").isEmpty());

        // 6th attempt should be blocked because OTP was invalidated
        OtpService.VerifyResult sixthAttempt = otpService.verifyOtp("bruteforce@example.com", "999999");
        assertFalse(sixthAttempt.success());
        assertTrue(sixthAttempt.message().contains("No active OTP"));
    }

    @Test
    @DisplayName("Expired OTP: Code past 5 minutes expiry is rejected and removed")
    void testExpiredOtp() {
        otpService.sendOtp("expired@example.com");

        // Manually update record in repository to be expired in the past
        OtpRecord current = otpRepository.findByEmail("expired@example.com").orElseThrow();
        OtpRecord expiredRecord = new OtpRecord(
                current.email(),
                current.hash(),
                Instant.now().minus(Duration.ofMinutes(1)), // Expired 1 min ago
                current.lastSentAt(),
                new AtomicInteger(0)
        );
        otpRepository.save("expired@example.com", expiredRecord);

        OtpService.VerifyResult result = otpService.verifyOtp("expired@example.com", "123456");
        assertFalse(result.success());
        assertTrue(result.message().contains("expired"));
        assertTrue(otpRepository.findByEmail("expired@example.com").isEmpty());
    }

    @Test
    @DisplayName("Single-use OTP: Correct verification succeeds; subsequent replay is rejected")
    void testCorrectOtpAndSingleUse() {
        final String[] capturedOtp = new String[1];
        Mockito.doAnswer(invocation -> {
            capturedOtp[0] = invocation.getArgument(1);
            return null;
        }).when(emailService).sendOtpEmail(eq("singleuse@example.com"), anyString());

        otpService.sendOtp("singleuse@example.com");
        assertNotNull(capturedOtp[0], "Generated OTP must be captured");

        // First verification: SUCCESS
        OtpService.VerifyResult firstVerify = otpService.verifyOtp("singleuse@example.com", capturedOtp[0]);
        assertTrue(firstVerify.success());
        assertEquals("Email Verified Successfully", firstVerify.message());

        // Second verification (Replay attack): MUST FAIL (Single-use protection)
        OtpService.VerifyResult replayVerify = otpService.verifyOtp("singleuse@example.com", capturedOtp[0]);
        assertFalse(replayVerify.success());
        assertTrue(replayVerify.message().contains("No active OTP"));
    }

    @Test
    @DisplayName("Scheduled Cleanup: Evicts expired records from repository")
    void testScheduledCleanup() {
        otpRepository.save("expired1@example.com", new OtpRecord(
                "expired1@example.com",
                new byte[]{1, 2},
                Instant.now().minus(Duration.ofMinutes(2)),
                Instant.now().minus(Duration.ofMinutes(3)),
                new AtomicInteger(0)
        ));

        otpRepository.save("active@example.com", new OtpRecord(
                "active@example.com",
                new byte[]{3, 4},
                Instant.now().plus(Duration.ofMinutes(4)),
                Instant.now(),
                new AtomicInteger(0)
        ));

        otpService.cleanupExpiredOtps();

        assertTrue(otpRepository.findByEmail("expired1@example.com").isEmpty());
        assertTrue(otpRepository.findByEmail("active@example.com").isPresent());
    }
}
