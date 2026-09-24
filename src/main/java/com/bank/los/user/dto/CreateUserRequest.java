package com.bank.los.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class CreateUserRequest {

    @Schema(description = "Target Bank/Organization ID (required when created by Super Admin / INTERNAL_ADMIN)", example = "1")
    private Long organizationId;

    @Schema(description = "Target Bank/Organization Code (alternative to organizationId)", example = "HDFC01")
    private String organizationCode;

    @Schema(description = "Business employee number (optional - auto-generated if omitted)", example = "EMP001")
    private String empNo;

    @NotBlank(message = "Username is required")
    @Schema(description = "Login username credential", example = "bankadmin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Initial account password", example = "Admin@123")
    private String password;

    @Schema(description = "Assigned role ID in tenant DB (e.g. 1 for ADMIN, 2 for MAKER, 3 for CHECKER)", example = "1")
    private Integer roleId;

    @Schema(description = "Assigned role name (e.g. 'ADMIN', 'MAKER', 'CHECKER', 'VIEWER')", example = "ADMIN")
    private String roleName;

    @Schema(description = "Initial status (default: OPERATIVE for Super Admin provisioning, PENDING_VERIFICATION for maker-checker)", example = "OPERATIVE")
    private String status;

    @Schema(description = "Enable Multi-Branch Access (if false and branches exist, loginBranchId is recommended)", example = "true")
    @Builder.Default
    private Boolean multiBranchAccess = false;

    @Schema(description = "Primary login branch ID (optional - auto-assigns primary branch if omitted)", example = "1")
    private Long loginBranchId;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Vikram")
    private String firstName;

    @Schema(description = "Middle name", example = "Aditya")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Mehta")
    private String lastName;

    @Schema(description = "Date of Birth", example = "1988-08-20")
    private LocalDate dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Staff user personal/official login email", example = "admin@hdfcbank.com")
    private String email;

    @Schema(description = "Mobile number", example = "+919876543210")
    private String mobile;

    @Schema(description = "Gender (MALE / FEMALE / OTHER)", example = "MALE")
    private String gender;

    @Schema(description = "Designation in bank/NBFC", example = "Bank Administrator")
    private String designation;

    @Schema(description = "Allow login on holidays", example = "true")
    @Builder.Default
    private Boolean loginOnHolidays = false;

    @Schema(description = "Daily login window start time (optional)", example = "08:00:00")
    private LocalTime loginTime;

    @Schema(description = "Daily logout window end time (optional)", example = "20:00:00")
    private LocalTime logoutTime;

    @Schema(description = "Inactive session timeout in seconds (default: 1800s / 30m)", example = "1800")
    @Builder.Default
    private Integer inactiveSessionTimeout = 1800;

    @Schema(description = "Enable 2FA (defaults to true)", example = "true")
    @Builder.Default
    private Boolean twoFaEnabled = true;
}
