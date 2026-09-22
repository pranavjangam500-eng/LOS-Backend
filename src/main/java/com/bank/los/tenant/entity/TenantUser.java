package com.bank.los.tenant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Bank/NBFC staff user — aligned with senior's DB design.
 * Fields: Pkid(id), EmpNo(empNo), UserName(username), Password(passwordHash),
 *         2fA, Status, Name(split), DOB, Mail, Mobile, Gender, Designation,
 *         Role, M_Br_access, Login_Branch, Holiday_Login, Inactive_session_timeout,
 *         noofbadlogin, lastlogindate, CreatedBy/Date, VerifiedBy/Date, ModifiedBy/Date
 */
@Data
@Entity
@Table(name = "users", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantUser {

    // ----------------------------------------------------------------
    // Core identity (senior's design: Pkid, EmpNo, UserName, Password)
    // ----------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                                              // Pkid

    /** Business-facing employee number — auto-generated on creation */
    @Column(name = "emp_no", unique = true, length = 30)
    private String empNo;                                         // EmpNo

    /** Login credential — what the user types at the login screen */
    @Column(name = "username", unique = true, nullable = false, length = 80)
    private String username;                                      // UserName

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;                                  // Password

    // ----------------------------------------------------------------
    // Security
    // ----------------------------------------------------------------

    /** 2FA is mandatory for all tenant users (this flag controls delivery method) */
    @Column(name = "two_fa_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFaEnabled = true;                          // 2fA

    /** BCrypt hash of current OTP — cleared after use */
    @Column(name = "two_fa_otp_hash", length = 255)
    private String twoFaOtpHash;

    @Column(name = "two_fa_otp_expiry")
    private LocalDateTime twoFaOtpExpiry;

    /**
     * Status values: PENDING_VERIFICATION / OPERATIVE / NON_OPERATIVE
     * New users start at PENDING_VERIFICATION until a second ADMIN verifies them.
     */
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING_VERIFICATION";               // Status

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ----------------------------------------------------------------
    // Role & Branch
    // ----------------------------------------------------------------

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private TenantRole role;                                      // Role

    /** When multi_branch_access = false, this is the only accessible branch */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_branch_id")
    private Branch loginBranch;                                   // Login_Branch

    /** true = use identity.user_branches join table; false = use loginBranch only */
    @Column(name = "multi_branch_access", nullable = false)
    @Builder.Default
    private Boolean multiBranchAccess = false;                    // M_Br_access

    // ----------------------------------------------------------------
    // Personal info (senior's design: Name, DOB, Mail, Mobile, Gender, Designation)
    // ----------------------------------------------------------------

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;                                     // Name (split)

    @Column(name = "middle_name", length = 80)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;                                        // DOB

    @Column(name = "email", unique = true, length = 150)
    private String email;                                         // Mail

    @Column(name = "mobile", unique = true, length = 20)
    private String mobile;                                        // Mobile

    @Column(name = "gender", length = 10)
    private String gender;                                        // MALE / FEMALE / OTHER

    @Column(name = "designation", length = 100)
    private String designation;                                   // Designation

    // ----------------------------------------------------------------
    // Login controls
    // ----------------------------------------------------------------

    @Column(name = "login_on_holidays", nullable = false)
    @Builder.Default
    private Boolean loginOnHolidays = false;                      // Holiday_Login

    @Column(name = "login_time")
    private LocalTime loginTime;                                  // allowed login window start

    @Column(name = "logout_time")
    private LocalTime logoutTime;                                 // allowed logout window end

    @Column(name = "inactive_session_timeout", nullable = false)
    @Builder.Default
    private Integer inactiveSessionTimeout = 1800;                // Inactive_session_timeout (seconds)

    // ----------------------------------------------------------------
    // Login tracking
    // ----------------------------------------------------------------

    @Column(name = "no_of_bad_logins", nullable = false)
    @Builder.Default
    private Integer noOfBadLogins = 0;                            // noofbadlogin

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;                              // lastlogindate

    @Column(name = "last_login_time")
    private LocalTime lastLoginTime;

    // ----------------------------------------------------------------
    // Audit columns (senior's design: CreatedBy/Date, VerifiedBy/Date, ModifiedBy/Date)
    // ----------------------------------------------------------------

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

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

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
