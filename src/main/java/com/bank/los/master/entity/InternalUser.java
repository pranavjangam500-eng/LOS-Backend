package com.bank.los.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Internal / Platform team user (SUPER_ADMIN).
 * Aligned with senior's DB design for audit columns.
 */
@Data
@Entity
@Table(name = "internal_users", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                              // Pkid

    @Column(name = "emp_no", unique = true, length = 30)
    private String empNo;                                         // EmpNo

    @Column(name = "username", unique = true, nullable = false, length = 80)
    private String username;                                      // UserName (login credential)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private MasterRole role;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;                                  // Password

    // Security
    @Column(name = "two_fa_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFaEnabled = false;

    @Column(name = "two_fa_otp_hash", length = 255)
    private String twoFaOtpHash;

    @Column(name = "two_fa_otp_expiry")
    private LocalDateTime twoFaOtpExpiry;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "OPERATIVE";

    // Personal info
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "middle_name", length = 80)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "mobile", unique = true, length = 20)
    private String mobile;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "designation", length = 100)
    private String designation;

    // Login controls
    @Column(name = "login_on_holidays", nullable = false)
    @Builder.Default
    private Boolean loginOnHolidays = true;

    @Column(name = "login_time")
    private LocalTime loginTime;

    @Column(name = "logout_time")
    private LocalTime logoutTime;

    @Column(name = "inactive_session_timeout", nullable = false)
    @Builder.Default
    private Integer inactiveSessionTimeout = 3600;

    // Login tracking
    @Column(name = "no_of_bad_logins", nullable = false)
    @Builder.Default
    private Integer noOfBadLogins = 0;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "last_login_time")
    private LocalTime lastLoginTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Audit columns (senior's design)
    @Column(name = "created_by")
    private Long createdBy;                                       // CreatedBy

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();        // CreatedDate

    @Column(name = "verified_by")
    private Long verifiedBy;                                      // VerifiedBy

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;                           // VerifiedDate

    @Column(name = "modified_by")
    private Long modifiedBy;                                      // ModifiedBy

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();        // ModifiedDate

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.isBlank()) sb.append(" ").append(middleName);
        if (lastName != null && !lastName.isBlank()) sb.append(" ").append(lastName);
        return sb.toString();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
