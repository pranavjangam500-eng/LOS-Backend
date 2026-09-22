package com.bank.los.auth.service;

import com.bank.los.common.constant.ApplicationConstants;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.tenant.entity.OtpToken;
import com.bank.los.tenant.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Handles 2FA OTP generation and verification for tenant users.
 * OTPs are ALWAYS required for tenant roles (ADMIN, CHECKER, MAKER, VIEWER).
 *
 * Security properties:
 *  - Generated with SecureRandom (cryptographically strong)
 *  - Stored as BCrypt hash (raw OTP never persisted)
 *  - 5-minute expiry
 *  - Single-use (marked used = true on first consumption)
 *  - Prior OTPs invalidated before issuing new one
 *  - Raw OTP returned in API response until SMTP is configured
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a 6-digit OTP, invalidates all previous ones for this user,
     * stores the BCrypt hash, and returns the raw OTP for email dispatch.
     *
     * @return the raw OTP (to be emailed — or returned in API response during dev)
     */
    @Transactional
    public String generateOtp(Long userId) {
        // Invalidate all existing OTPs for this user
        otpTokenRepository.invalidateAllForUser(userId);

        // Generate cryptographically secure 6-digit OTP
        int rawOtp = 100_000 + SECURE_RANDOM.nextInt(900_000);
        String rawOtpStr = String.valueOf(rawOtp);

        // Store BCrypt hash — never store raw OTP
        OtpToken token = OtpToken.builder()
                .userId(userId)
                .otpHash(passwordEncoder.encode(rawOtpStr))
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(ApplicationConstants.OTP_EXPIRY_MINUTES))
                .build();

        otpTokenRepository.save(token);
        log.info("2FA OTP generated for userId={}, expires in {} minutes",
                userId, ApplicationConstants.OTP_EXPIRY_MINUTES);

        return rawOtpStr;
    }

    /**
     * Verifies the OTP submitted by the user.
     * On success: marks the token as used.
     * On failure: throws BusinessException.
     */
    @Transactional
    public void verifyOtp(Long userId, String rawOtp) {
        OtpToken token = otpTokenRepository
                .findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("OTP_EXPIRED", "OTP has expired or is invalid. Please login again to receive a new OTP."));

        if (!passwordEncoder.matches(rawOtp, token.getOtpHash())) {
            throw new BusinessException("OTP_INVALID", "Invalid OTP. Please check and try again.");
        }

        // Mark as used — cannot be reused
        token.setUsed(true);
        otpTokenRepository.save(token);
        log.info("2FA OTP verified successfully for userId={}", userId);
    }
}
