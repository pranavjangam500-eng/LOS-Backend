package com.bank.los.bank.auth.service;

import com.bank.los.bank.master.entity.OtpToken;
import com.bank.los.bank.master.repository.OtpTokenRepository;
import com.bank.los.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Transactional
    public String generateOtp(Long userId) {
        // Invalidate any active, unused OTP for this user
        otpTokenRepository.invalidateAllForUser(userId);

        // Generate cryptographically secure 6-digit OTP
        int otpInt = 100000 + SECURE_RANDOM.nextInt(900000);
        String rawOtp = String.valueOf(otpInt);

        // Store BCrypt hash of the OTP
        OtpToken token = OtpToken.builder()
                .userId(userId)
                .otpHash(passwordEncoder.encode(rawOtp))
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        otpTokenRepository.save(token);
        log.info("2FA OTP generated for userId={}, expires in {} minutes", userId, OTP_EXPIRY_MINUTES);
        return rawOtp;
    }

    @Transactional
    public void verifyOtp(Long userId, String rawOtp) {
        OtpToken token = otpTokenRepository
                .findTopByUserIdAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(userId, LocalDateTime.now())
                .orElseThrow(() -> new UnauthorizedException("OTP is invalid or has expired. Please request a new one."));

        if (!passwordEncoder.matches(rawOtp.trim(), token.getOtpHash())) {
            throw new UnauthorizedException("Invalid OTP. Please check the code and try again.");
        }

        // Mark OTP as used immediately (single-use)
        token.setUsed(true);
        otpTokenRepository.save(token);
        log.info("2FA OTP verified successfully for userId={}", userId);
    }
}
