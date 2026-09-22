package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Admin-initiated password reset — dual-control (maker-checker).
 * ADMIN creates reset → second ADMIN/SUPER_ADMIN verifies → password applied.
 * Maps to senior's DB design: Pkid, EmpNo, Password, CreatedBy/Date, VerifiedBy/Date.
 */
@Data
@Entity
@Table(name = "user_password_resets", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                          // Pkid

    @Column(name = "emp_no", nullable = false, length = 30)
    private String empNo;                     // EmpNo

    @Column(name = "new_password_hash", nullable = false, length = 255)
    private String newPasswordHash;           // Password (BCrypt)

    @Column(name = "created_by", nullable = false)
    private Long createdBy;                   // CreatedBy

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // CreatedDate

    @Column(name = "verified_by")
    private Long verifiedBy;                  // VerifiedBy

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;       // VerifiedDate

    @Column(name = "applied", nullable = false)
    @Builder.Default
    private Boolean applied = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
