package com.bank.los.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Login authentication response")
public class LoginResponse {

    // ── 2FA challenge (when otpRequired = true, token fields are null) ──

    @Schema(description = "True when 2FA OTP step is required (always for tenant roles)")
    private Boolean otpRequired;

    @Schema(description = "Temporary signed session token to pass with OTP verification")
    private String tempSessionToken;

    /**
     * Raw OTP returned ONLY when SMTP is not configured (dev/test mode).
     * Removed from response once app.mail.enabled=true.
     */
    @Schema(description = "[DEV ONLY] OTP shown in response when email is disabled")
    private String devOtp;

    // ── Full auth response (after 2FA verified) ──

    @Schema(description = "JWT Access token")
    private String accessToken;

    @Schema(description = "Refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token TTL in milliseconds")
    private Long expiresIn;

    @Schema(description = "User profile")
    private UserProfileResponse user;

    @Schema(description = "Role-based dashboard route", example = "/dashboard/admin")
    private String dashboardUrl;

    @Schema(description = "Permissions loaded from DB for this role")
    private List<String> permissions;
}
