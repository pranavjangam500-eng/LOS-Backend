package com.bank.los.otp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to send an OTP code to an email address")
public record SendOtpRequest(
    @Schema(description = "Recipient email address", example = "demo@allianzapay.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email
) {}
