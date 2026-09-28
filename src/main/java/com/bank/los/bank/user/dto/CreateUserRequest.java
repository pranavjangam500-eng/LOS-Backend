package com.bank.los.bank.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request body to onboard / create a new bank staff user in the organization's database")
public class CreateUserRequest {

    @JsonProperty("pkid")
    @JsonAlias({"id", "Pkid", "userId"})
    @Schema(description = "User primary key ID (optional)", example = "1")
    private Long id;

    @JsonProperty("organization_id")
    @JsonAlias({"organizationId", "bankId", "bank_id", "Organisation_id", "organisationId", "orgId"})
    @Schema(description = "Target Bank / Organisation ID to map the user into", example = "1")
    private Long organizationId;

    @JsonProperty("organization_code")
    @JsonAlias({"organizationCode", "bankCode", "bank_code", "orgCode"})
    @Schema(description = "Target Organisation Code (alternative to organizationId)", example = "HDFC01")
    private String organizationCode;

    @JsonProperty("emp_no")
    @JsonAlias({"empNo", "EmpNo", "employeeNumber", "employee_no"})
    @Schema(description = "Employee Number (auto-generated if omitted)", example = "EMP-HDFC-001")
    private String empNo;

    @JsonProperty("name")
    @JsonAlias({"Name", "fullName", "full_name"})
    @Schema(description = "Full name of the user (can provide name or firstName/lastName)", example = "Vikram Sharma")
    private String name;

    @JsonProperty("first_name")
    @JsonAlias({"firstName", "FirstName"})
    @Schema(description = "First name of user", example = "Vikram")
    private String firstName;

    @JsonProperty("middle_name")
    @JsonAlias({"middleName", "MiddleName"})
    @Schema(description = "Middle name of user", example = "Kumar")
    private String middleName;

    @JsonProperty("last_name")
    @JsonAlias({"lastName", "LastName"})
    @Schema(description = "Last name of user", example = "Sharma")
    private String lastName;

    @JsonProperty("username")
    @JsonAlias({"userName", "UserName"})
    @Schema(description = "Unique login username (defaults to email/empNo if omitted)", example = "vikram_admin")
    private String username;

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    @JsonAlias({"Password", "pwd"})
    @Schema(description = "Initial user password (min 8 chars, 1 upper, 1 lower, 1 digit, 1 special char)", example = "Admin@123")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    @JsonProperty("email")
    @JsonAlias({"Email", "mail", "Mail"})
    @Schema(description = "User corporate email address", example = "vikram.sharma@hdfcbank.com")
    private String email;

    @JsonProperty("mobile")
    @JsonAlias({"Mobile", "phone", "Phone", "contactNumber"})
    @Schema(description = "Mobile number", example = "+919876543210")
    private String mobile;

    @JsonProperty("gender")
    @JsonAlias({"Gender"})
    @Schema(description = "Gender (MALE, FEMALE, OTHER)", example = "MALE")
    private String gender;

    @JsonProperty("dob")
    @JsonAlias({"DOB", "Dob", "dateOfBirth", "birthDate"})
    @Schema(description = "Date of Birth (Optional)", example = "1990-05-15")
    private LocalDate dob;

    @JsonProperty("designation")
    @JsonAlias({"Designation"})
    @Schema(description = "Official job designation", example = "Branch Manager")
    private String designation;

    @JsonProperty("role")
    @JsonAlias({"Role", "roleName", "role_name"})
    @Schema(description = "Role name in organization (ADMIN, MAKER, CHECKER, VIEWER)", example = "ADMIN")
    private String roleName;

    @JsonProperty("role_id")
    @JsonAlias({"roleId", "RoleId"})
    @Schema(description = "Role ID in this bank's database schema", example = "1")
    private Integer roleId;

    @JsonProperty("2fA")
    @JsonAlias({"twoFa", "two_fa", "twoFaEnabled", "two_fa_enabled", "2FA"})
    @Schema(description = "Two-factor authentication enabled flag", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean twoFaEnabled = true;

    @JsonProperty("status")
    @JsonAlias({"Status"})
    @Schema(description = "Initial user status (OPERATIVE, PENDING_VERIFICATION, ACTIVE)", example = "OPERATIVE", defaultValue = "OPERATIVE")
    private String status;

    @JsonProperty("m_br_access")
    @JsonAlias({"mBrAccess", "M_Br_access", "multiBranchAccess", "multi_branch_access"})
    @Schema(description = "Multi-branch access flag", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean multiBranchAccess = false;

    @JsonProperty("login_branch")
    @JsonAlias({"loginBranch", "Login_Branch", "loginBranchId", "login_branch_id", "branchId"})
    @Schema(description = "Primary login branch ID", example = "1")
    private Long loginBranchId;

    @JsonProperty("holiday_login")
    @JsonAlias({"holidayLogin", "Holiday_Login", "loginOnHolidays", "login_on_holidays"})
    @Schema(description = "Allow login on weekends/bank holidays", example = "false", defaultValue = "false")
    @Builder.Default
    private Boolean loginOnHolidays = false;

    @JsonProperty("login_time")
    @JsonAlias({"loginTime", "Login_Time"})
    @Schema(description = "Allowed login window start time", example = "09:00:00")
    private LocalTime loginTime;

    @JsonProperty("logout_time")
    @JsonAlias({"logoutTime", "Logout_Time"})
    @Schema(description = "Allowed logout window end time", example = "19:00:00")
    private LocalTime logoutTime;

    @JsonProperty("inactive_session_timeout")
    @JsonAlias({"inactiveSessionTimeout", "Inactive_session_timeout"})
    @Schema(description = "Session inactivity timeout in seconds", example = "1800", defaultValue = "1800")
    @Builder.Default
    private Integer inactiveSessionTimeout = 1800;

    @JsonProperty("lastlogindate")
    @JsonAlias({"lastLoginDate", "last_login_date", "LastLoginDate"})
    @Schema(description = "Last login date")
    private LocalDate lastLoginDate;

    @JsonProperty("created_by")
    @JsonAlias({"createdBy", "CreatedBy"})
    private Long createdBy;

    @JsonProperty("created_date")
    @JsonAlias({"createdDate", "CreatedDate", "createdAt", "created_at"})
    private LocalDateTime createdDate;

    @JsonProperty("verified_by")
    @JsonAlias({"verifiedBy", "VerifiedBy"})
    private Long verifiedBy;

    @JsonProperty("verified_date")
    @JsonAlias({"verifiedDate", "VerifiedDate"})
    private LocalDateTime verifiedDate;

    @JsonProperty("modified_by")
    @JsonAlias({"modifiedBy", "ModifiedBy"})
    private Long modifiedBy;

    @JsonProperty("modified_date")
    @JsonAlias({"modifiedDate", "ModifiedDate", "updatedAt", "updated_at"})
    private LocalDateTime modifiedDate;
}

