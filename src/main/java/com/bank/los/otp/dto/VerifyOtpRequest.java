package com.bank.los.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload to verify an OTP code")
public record VerifyOtpRequest(
    @Schema(description = "Email address to verify", example = "demo@allianzapay.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    @Schema(description = "6-digit OTP code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit numeric code")
    String otp
) {}
