package com.bank.los.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "User code is required")
    @Schema(description = "Staff user employee code", example = "EMP-001")
    private String userCode;

    @NotNull(message = "Branch ID is required")
    @Schema(description = "Assigned branch ID", example = "1")
    private Long branchId;

    @NotNull(message = "Role ID is required")
    @Schema(description = "Assigned role ID", example = "3")
    private Integer roleId;

    @NotBlank(message = "First name is required")
    @Schema(description = "First name", example = "Rahul")
    private String firstName;

    @Schema(description = "Middle name", example = "Kumar")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name", example = "Sharma")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Official email address", example = "rahul.sharma@bank.com")
    private String email;

    @Schema(description = "Phone number", example = "+919876543211")
    private String phone;

    @NotBlank(message = "Password is required")
    @Schema(description = "Initial account password", example = "Staff@123")
    private String password;
}
