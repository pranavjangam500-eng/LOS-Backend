package com.bank.los.bank.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request body for updating an existing bank staff user")
public class UpdateUserRequest {

    @JsonProperty("first_name")
    @JsonAlias({"firstName", "FirstName"})
    @Schema(description = "First name of user", example = "Sohan")
    private String firstName;

    @JsonProperty("middle_name")
    @JsonAlias({"middleName", "MiddleName"})
    @Schema(description = "Middle name of user (optional)", example = "Singh")
    private String middleName;

    @JsonProperty("last_name")
    @JsonAlias({"lastName", "LastName"})
    @Schema(description = "Last name of user", example = "Verma")
    private String lastName;

    @JsonProperty("name")
    @JsonAlias({"Name", "fullName", "full_name"})
    @Schema(description = "Full name of the user (auto-split if first/last name not given)", example = "Sohan Singh Verma")
    private String name;

    @Email(message = "Valid email is required")
    @JsonProperty("email")
    @JsonAlias({"Email", "mail", "Mail"})
    @Schema(description = "Corporate email address", example = "sohan@gmail.com")
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
    @JsonAlias({"DOB", "Dob", "dateOfBirth", "birthDate", "date_of_birth", "birth_date"})
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date of Birth (YYYY-MM-DD)", example = "1990-05-15")
    private LocalDate dob;

    @JsonProperty("designation")
    @JsonAlias({"Designation"})
    @Schema(description = "Official job designation", example = "Senior Relationship Manager")
    private String designation;

    @JsonProperty("role")
    @JsonAlias({"Role", "roleName", "role_name"})
    @Schema(description = "Role name in organization (SUPER_ADMIN, ADMIN, MAKER, CHECKER, VIEWER)", example = "CHECKER")
    private String roleName;

    @JsonProperty("role_id")
    @JsonAlias({"roleId", "RoleId"})
    @Schema(description = "Role ID in this bank's database schema", example = "2")
    private Integer roleId;

    @JsonProperty("2fA")
    @JsonAlias({"twoFa", "two_fa", "twoFaEnabled", "two_fa_enabled", "2FA"})
    @Schema(description = "Two-factor authentication enabled flag", example = "true")
    private Boolean twoFaEnabled;

    @JsonProperty("status")
    @JsonAlias({"Status"})
    @Schema(description = "User operational status (OPERATIVE, ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION)", example = "OPERATIVE")
    private String status;

    @JsonProperty("is_active")
    @JsonAlias({"isActive", "active", "IsActive"})
    @Schema(description = "Is user account active", example = "true")
    private Boolean isActive;

    @JsonProperty("m_br_access")
    @JsonAlias({"mBrAccess", "M_Br_access", "multiBranchAccess", "multi_branch_access", "allow_multibranch", "allow_multi_branch", "allowMultibranch", "allowMultiBranch"})
    @Schema(description = "Allow multi-branch access", example = "true")
    private Boolean multiBranchAccess;

    @JsonProperty("login_branch")
    @JsonAlias({"loginBranch", "Login_Branch", "loginBranchName", "login_branch_name", "branch_name", "branchName"})
    @Schema(description = "Primary login branch ID or Name", example = "1")
    private String loginBranch;

    @JsonProperty("login_branch_id")
    @JsonAlias({"loginBranchId", "branchId", "branch_id"})
    @Schema(description = "Primary login branch ID", example = "1")
    private Long loginBranchId;

    @JsonProperty("holiday_login")
    @JsonAlias({"holidayLogin", "Holiday_Login", "loginOnHolidays", "login_on_holidays", "allow_login_in_holidays", "allow_login_on_holidays"})
    @Schema(description = "Allow login on weekends/bank holidays", example = "false")
    private Boolean loginOnHolidays;

    @JsonProperty("login_time")
    @JsonAlias({"loginTime", "Login_Time"})
    @JsonFormat(pattern = "HH:mm[:ss]")
    @Schema(description = "Allowed login window start time (HH:mm:ss)", example = "08:00:00")
    private LocalTime loginTime;

    @JsonProperty("logout_time")
    @JsonAlias({"logoutTime", "Logout_Time"})
    @JsonFormat(pattern = "HH:mm[:ss]")
    @Schema(description = "Allowed logout window end time (HH:mm:ss)", example = "20:00:00")
    private LocalTime logoutTime;

    @JsonProperty("inactive_session_timeout")
    @JsonAlias({"inactiveSessionTimeout", "Inactive_session_timeout"})
    @Schema(description = "Session inactivity timeout in seconds", example = "1800")
    private Integer inactiveSessionTimeout;

    // Custom setters for boolean coercion (0/1, string/boolean)
    @JsonSetter("m_br_access")
    public void setMBrAccess(Object val) {
        this.multiBranchAccess = parseBooleanFlag(val);
    }

    @JsonSetter("allow_multibranch")
    public void setAllowMultiBranch(Object val) {
        this.multiBranchAccess = parseBooleanFlag(val);
    }

    @JsonSetter("multi_branch_access")
    public void setMultiBranchAccessProp(Object val) {
        this.multiBranchAccess = parseBooleanFlag(val);
    }

    @JsonSetter("holiday_login")
    public void setHolidayLoginProp(Object val) {
        this.loginOnHolidays = parseBooleanFlag(val);
    }

    @JsonSetter("allow_login_in_holidays")
    public void setAllowLoginInHolidays(Object val) {
        this.loginOnHolidays = parseBooleanFlag(val);
    }

    @JsonSetter("login_on_holidays")
    public void setLoginOnHolidaysProp(Object val) {
        this.loginOnHolidays = parseBooleanFlag(val);
    }

    @JsonSetter("login_branch")
    public void setLoginBranchProp(Object val) {
        if (val != null) {
            String str = val.toString().trim();
            this.loginBranch = str;
            try {
                this.loginBranchId = Long.parseLong(str);
            } catch (NumberFormatException ignored) {}
        }
    }

    private static Boolean parseBooleanFlag(Object val) {
        if (val == null) return null;
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.intValue() == 1;
        String s = val.toString().trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s);
    }
}
