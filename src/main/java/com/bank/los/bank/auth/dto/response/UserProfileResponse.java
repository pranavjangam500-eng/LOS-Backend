package com.bank.los.bank.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authenticated user profile details")
public class UserProfileResponse {

    private Long id;
    private String empNo;
    private String userCode;
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
    private String userType;
    private String role;
    private String status;
    private Boolean twoFaEnabled;
    private Boolean multiBranchAccess;
    private Long branchId;
    private String branchName;
    private String organizationCode;
    private String organizationName;
    private Boolean loginOnHolidays;
    private LocalTime loginTime;
    private LocalTime logoutTime;
    private Integer inactiveSessionTimeout;
    private LocalDate lastLoginDate;
    private LocalTime lastLoginTime;
}
