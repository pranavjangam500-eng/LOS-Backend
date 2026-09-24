package com.bank.los.bank.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_password_resets", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_no", nullable = false, length = 30)
    private String empNo;

    @Column(name = "new_password_hash", nullable = false, length = 255)
    private String newPasswordHash;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean applied = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
