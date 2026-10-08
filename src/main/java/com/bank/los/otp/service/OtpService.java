package com.bank.los.otp.service;

import com.bank.los.otp.model.OtpRecord;
import com.bank.los.otp.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing OTP generation, SHA-256 salted hashing,
 * expiry checking, attempt limiting, and single-use verification.
 */
@Slf4j
@Service("emailOtpService")
public class OtpService {

    private static final Duration OTP_VALIDITY = Duration.ofMinutes(5);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom secureRandom = new SecureRandom();
    private final OtpEmailService emailService;
    private final OtpRepository otpRepository;

    public record SendResult(
            boolean success,
            String message,
            Integer cooldownRemaining
    ) {}

    public record VerifyResult(
            boolean success,
            String message
    ) {}

    public OtpService(@Qualifier("otpEmailService") OtpEmailService emailService,
                      @Qualifier("inMemoryOtpRepository") OtpRepository otpRepository) {
        this.emailService = emailService;
        this.otpRepository = otpRepository;
    }

    /**
     * Sends a 6-digit cryptographic OTP to the specified email address.
     * Enforces a 60-second cooldown between requests.
     */
    public SendResult sendOtp(String email) {
        if (email == null || email.isBlank()) {
            return new SendResult(false, "Email address is required.", null);
        }

        String normalizedEmail = email.trim().toLowerCase();
        Instant now = Instant.now();

        Optional<OtpRecord> existingOpt = otpRepository.findByEmail(normalizedEmail);

        // Check 60-second resend cooldown
        if (existingOpt.isPresent() && existingOpt.get().lastSentAt() != null) {
            Duration elapsed = Duration.between(existingOpt.get().lastSentAt(), now);
            if (elapsed.compareTo(COOLDOWN) < 0) {
                int remaining = (int) (COOLDOWN.toSeconds() - elapsed.toSeconds());
                return new SendResult(
                        false,
                        "Please wait " + remaining + "s before requesting a new OTP.",
                        remaining
                );
            }
        }

        // Generate cryptographic 6-digit numeric OTP (100000 - 999999)
        int randomCode = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(randomCode);

        // Hash OTP with SHA-256 + salt (normalized email)
        byte[] hash = hashOtp(otp, normalizedEmail);
        Instant expiresAt = now.plus(OTP_VALIDITY);

        // Send Email via SMTP / MailSender
        emailService.sendOtpEmail(normalizedEmail, otp);

        // Store hashed OTP record in repository
        otpRepository.save(normalizedEmail, new OtpRecord(
                normalizedEmail,
                hash,
                expiresAt,
                now,
                new AtomicInteger(0)
        ));

        return new SendResult(
                true,
                "OTP sent successfully to " + normalizedEmail + ".",
                null
        );
    }

    /**
     * Verifies the submitted OTP against the stored cryptographic hash.
     * Enforces single-use consumption and max 5 attempts.
     */
    public VerifyResult verifyOtp(String email, String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return new VerifyResult(false, "Email and OTP code are required.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String cleanOtp = otp.trim();

        Optional<OtpRecord> recordOpt = otpRepository.findByEmail(normalizedEmail);

        if (recordOpt.isEmpty()) {
            return new VerifyResult(
                    false,
                    "No active OTP found for this email or it has expired. Please request a new code."
            );
        }

        OtpRecord record = recordOpt.get();
        Instant now = Instant.now();

        // Check expiration
        if (now.isAfter(record.expiresAt())) {
            otpRepository.deleteByEmail(normalizedEmail);
            return new VerifyResult(
                    false,
                    "The OTP code has expired. Please request a new code."
            );
        }

        // Check attempt count
        int attempt = record.attempts().incrementAndGet();
        if (attempt > MAX_ATTEMPTS) {
            otpRepository.deleteByEmail(normalizedEmail);
            return new VerifyResult(
                    false,
                    "Too many incorrect attempts. This OTP has been invalidated for security. Please request a new code."
            );
        }

        // Compute hash of user input with email salt
        byte[] submittedHash = hashOtp(cleanOtp, normalizedEmail);

        // Constant-time comparison to prevent timing attacks
        boolean isMatch = MessageDigest.isEqual(submittedHash, record.hash());

        if (!isMatch) {
            if (attempt >= MAX_ATTEMPTS) {
                otpRepository.deleteByEmail(normalizedEmail);
                log.warn("[VERIFICATION FAILED] Maximum attempts ({}) reached for {}. OTP invalidated.", MAX_ATTEMPTS, normalizedEmail);
                return new VerifyResult(false, "Incorrect OTP code. OTP invalidated.");
            }
            int remaining = MAX_ATTEMPTS - attempt;
            log.warn("[VERIFICATION FAILED] Failed verification attempt {}/{} for {}", attempt, MAX_ATTEMPTS, normalizedEmail);
            return new VerifyResult(
                    false,
                    "Incorrect OTP code. " + remaining + " attempt(s) remaining."
            );
        }

        log.info("[VERIFICATION SUCCESS] Successfully verified email: {}", normalizedEmail);

        // Single-use: delete immediately upon successful verification
        otpRepository.deleteByEmail(normalizedEmail);

        return new VerifyResult(true, "Email Verified Successfully");
    }

    /**
     * Periodic cleanup of expired entries every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOtps() {
        otpRepository.removeExpired(Instant.now());
    }

    /**
     * Helper: Computes SHA-256 hash using the normalized email as cryptographic salt.
     */
    private byte[] hashOtp(String otp, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            return digest.digest(otp.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
