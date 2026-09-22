package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Self-service forgot-password reset tokens.
 * Production-grade: token_hash = SHA-256(rawToken) — raw token NEVER stored.
 * - One-time use (used=true on first consumption)
 * - 15-minute expiry
 * - Rate-limited at service layer (max 3/hr per emp_no)
 * - All prior tokens invalidated when new one is generated
 */
@Data
@Entity
@Table(name = "self_service_reset_tokens", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfServiceResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_no", nullable = false, length = 30)
    private String empNo;

    /** SHA-256 hex of the raw token — raw token emailed to user, never stored */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
