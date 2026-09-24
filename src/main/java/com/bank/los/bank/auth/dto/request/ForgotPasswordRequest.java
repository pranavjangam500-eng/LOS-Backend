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
@Schema(description = "Request body for self-service forgot password")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email / User Identifier is required")
    @Schema(description = "Registered email address, employee number, or mobile number", example = "admin@hdfcbank.com")
    private String email;
}
