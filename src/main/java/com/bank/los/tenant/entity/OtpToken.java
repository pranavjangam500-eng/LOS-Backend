package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores 2FA OTPs for tenant users.
 * OTP is stored as BCrypt hash — raw OTP never persisted.
 * Each login generates a fresh OTP; previous ones are voided.
 */
@Data
@Entity
@Table(name = "otp_tokens", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** BCrypt hash of the 6-digit OTP */
    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
