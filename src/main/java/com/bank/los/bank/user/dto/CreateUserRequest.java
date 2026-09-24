package com.bank.los.bank.user.dto;

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
@Schema(description = "Request body to onboard / create a new bank staff user")
public class CreateUserRequest {

    @Schema(description = "Target Organization ID (required if created by Super Admin)", example = "1")
    private Long organizationId;

    @Schema(description = "Target Organization Code (alternative to organizationId)", example = "HDFC01")
    private String organizationCode;

    @Schema(description = "Custom employee number (auto-generated if omitted)", example = "EMP-HDFC-001")
    private String empNo;

    @NotBlank(message = "Username is required")
    @Schema(description = "Unique login username", example = "vikram_admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Initial password", example = "Admin@123")
    private String password;

    @Schema(description = "Role ID in this bank's schema")
    private Integer roleId;

    @Schema(description = "Role name (e.g. ADMIN, MAKER, CHECKER, VIEWER)", example = "ADMIN")
    private String roleName;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dob;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String mobile;
    private String gender;
    private String designation;

    private Long loginBranchId;
    private Boolean multiBranchAccess;

    private Boolean twoFaEnabled;
    private Boolean loginOnHolidays;
    private LocalTime loginTime;
    private LocalTime logoutTime;
    private Integer inactiveSessionTimeout;

    @Schema(description = "Initial user status (OPERATIVE or PENDING_VERIFICATION)", example = "OPERATIVE")
    private String status;
}
