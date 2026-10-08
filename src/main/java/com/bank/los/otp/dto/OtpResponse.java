package com.bank.los.otp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard OTP API Response")
public record OtpResponse(
    @Schema(description = "Indicates if the operation succeeded", example = "true")
    boolean success,

    @Schema(description = "Descriptive status or error message", example = "OTP sent successfully to demo@allianzapay.com.")
    String message,

    @Schema(description = "Remaining cooldown seconds if rate limited", example = "45")
    Integer cooldownRemaining,

    @Schema(description = "Verified email address upon successful verification", example = "demo@allianzapay.com")
    String verifiedEmail,

    @Schema(description = "Timestamp of successful verification in ISO-8601", example = "2026-10-08T06:00:00Z")
    String verifiedAt
) {

    public static OtpResponse success(String message) {
        return new OtpResponse(true, message, null, null, null);
    }

    public static OtpResponse verified(String email) {
        return new OtpResponse(true, "Email Verified Successfully", null, email, Instant.now().toString());
    }

    public static OtpResponse error(String message) {
        return new OtpResponse(false, message, null, null, null);
    }

    public static OtpResponse cooldown(String message, int remainingSeconds) {
        return new OtpResponse(false, message, remainingSeconds, null, null);
    }
}
