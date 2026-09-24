package com.bank.los.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representing a bank staff user")
public class UserResponse {

    private Long id;
    private Long organizationId;
    private String organizationCode;
    private String organizationName;
    private String empNo;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String email;
    private String mobile;
    private String gender;
    private LocalDate dob;
    private String designation;
    private String status;
    private Boolean isActive;
    private Boolean twoFaEnabled;
    private Integer roleId;
    private String roleName;
    private Boolean multiBranchAccess;
    private Long loginBranchId;
    private String loginBranchName;
    private Boolean loginOnHolidays;
    private LocalTime loginTime;
    private LocalTime logoutTime;
    private Integer inactiveSessionTimeout;
    private Integer noOfBadLogins;
    private LocalDate lastLoginDate;
    private LocalTime lastLoginTime;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long verifiedBy;
    private LocalDateTime verifiedDate;
    private Long modifiedBy;
    private LocalDateTime updatedAt;
}
