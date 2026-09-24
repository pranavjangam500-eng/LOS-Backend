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
@Schema(description = "Request body for verifying 2FA OTP")
public class VerifyOtpRequest {

    @NotBlank(message = "Temporary session token is required")
    @Schema(description = "Temporary challenge session token returned from /login", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String tempSessionToken;

    @NotBlank(message = "OTP is required")
    @Schema(description = "6-digit One-Time Password sent to the registered email address", example = "123456")
    private String otp;
}
