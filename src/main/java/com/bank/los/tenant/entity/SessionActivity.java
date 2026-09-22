package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks last API activity per JWT ID (jti) for per-user session auto-logout.
 * The bank ADMIN sets inactive_session_timeout on each user.
 * The JWT filter checks this on every request and returns 401 if idle too long.
 */
@Data
@Entity
@Table(name = "session_activity", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionActivity {

    /** JWT ID claim (jti) — unique per issued token */
    @Id
    @Column(name = "jti", length = 100)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;

    /** Copied from user.inactive_session_timeout at login time */
    @Column(name = "timeout_secs", nullable = false)
    private Integer timeoutSecs;

    @Column(name = "invalidated", nullable = false)
    @Builder.Default
    private Boolean invalidated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
