package com.bank.los.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @Schema(description = "Business employee number (optional - auto-generated if omitted)", example = "EMP001")
    private String empNo;

    @NotBlank(message = "Username is required")
    @Schema(description = "Login username credential", example = "maker1")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Initial account password", example = "Staff@123")
    private String password;

    @NotNull(message = "Role ID is required")
    @Schema(description = "Assigned role ID", example = "2")
    private Integer roleId;

    @Schema(description = "Enable Multi-Branch Access (if false, loginBranchId is required)", example = "false")
    @Builder.Default
    private Boolean multiBranchAccess = false;

    @Schema(description = "Primary login branch ID (required if multiBranchAccess is false)", example = "1")
    private Long loginBranchId;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Rohan")
    private String firstName;

    @Schema(description = "Middle name", example = "Kumar")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Verma")
    private String lastName;

    @Schema(description = "Date of Birth", example = "1990-05-15")
    private LocalDate dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Official email address", example = "maker1@bank.com")
    private String email;

    @Schema(description = "Mobile number", example = "+919876543210")
    private String mobile;

    @Schema(description = "Gender (MALE / FEMALE / OTHER)", example = "MALE")
    private String gender;

    @Schema(description = "Designation in bank/NBFC", example = "Loan Officer")
    private String designation;

    @Schema(description = "Allow login on holidays", example = "false")
    @Builder.Default
    private Boolean loginOnHolidays = false;

    @Schema(description = "Daily login window start time (optional)", example = "09:00:00")
    private LocalTime loginTime;

    @Schema(description = "Daily logout window end time (optional)", example = "18:00:00")
    private LocalTime logoutTime;

    @Schema(description = "Inactive session timeout in seconds (default: 1800s / 30m)", example = "1800")
    @Builder.Default
    private Integer inactiveSessionTimeout = 1800;

    @Schema(description = "Enable 2FA (defaults to true)", example = "true")
    @Builder.Default
    private Boolean twoFaEnabled = true;
}
