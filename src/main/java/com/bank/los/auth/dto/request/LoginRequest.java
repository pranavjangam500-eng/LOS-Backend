package com.bank.los.auth.dto.request;

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
@Schema(description = "User login payload supporting email / user_code / phone and password")
public class LoginRequest {

    @NotBlank(message = "Email / User Code is required")
    @Schema(description = "User email address or user code", example = "admin@losplatform.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "Account password", example = "Admin@123")
    private String password;

    @Schema(description = "Optional explicit organization/bank code", example = "HDFC01")
    private String organizationCode;
}
