package com.bank.los.bank.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user login")
public class LoginRequest {

    @NotBlank(message = "Identifier (Email, Employee No, Customer Code, or Mobile) is required")
    @Schema(description = "Registered email, employee number (e.g. EMP001), customer code (e.g. CUST001), or mobile number", example = "admin@hdfcbank.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User account password", example = "Admin@123")
    private String password;
}
