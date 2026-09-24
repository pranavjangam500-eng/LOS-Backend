package com.bank.los.administration.master.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "internal_users", schema = "identity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InternalUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_no", unique = true, length = 30)
    private String empNo;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private MasterRole role;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "two_fa_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFaEnabled = false;

    @Column(name = "two_fa_otp_hash", length = 255)
    private String twoFaOtpHash;

    @Column(name = "two_fa_otp_expiry")
    private LocalDateTime twoFaOtpExpiry;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "OPERATIVE";

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "middle_name", length = 80)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(unique = true, length = 150)
    private String email;

    @Column(unique = true, length = 20)
    private String mobile;

    @Column(length = 10)
    private String gender;

    @Column(length = 100)
    private String designation;

    @Column(name = "login_on_holidays", nullable = false)
    @Builder.Default
    private Boolean loginOnHolidays = true;

    @Column(name = "login_time")
    private LocalTime loginTime;

    @Column(name = "logout_time")
    private LocalTime logoutTime;

    @Column(name = "inactive_session_timeout", nullable = false)
    @Builder.Default
    private Integer inactiveSessionTimeout = 1800;

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

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

    public String getFullName() {
        if (middleName != null && !middleName.isBlank()) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }
}
