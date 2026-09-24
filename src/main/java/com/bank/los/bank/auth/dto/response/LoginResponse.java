package com.bank.los.bank.auth.dto.response;

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
@Schema(description = "Response returned after successful authentication / OTP challenge")
public class LoginResponse {

    @Schema(description = "Indicates whether 2FA OTP is required to complete authentication (always true for bank staff)", example = "true")
    private Boolean otpRequired;

    @Schema(description = "Temporary challenge session token passed to /verify-otp when otpRequired is true")
    private String tempSessionToken;

    @Schema(description = "Raw 6-digit OTP returned during local/dev testing when SMTP is disabled", example = "123456")
    private String devOtp;

    @Schema(description = "Signed JWT Access Token (valid for 24 hours)")
    private String accessToken;

    @Schema(description = "Signed JWT Refresh Token (valid for 7 days)")
    private String refreshToken;

    @Schema(description = "Token type prefix", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in milliseconds", example = "86400000")
    private Long expiresIn;

    @Schema(description = "Landing dashboard route resolved for the authenticated user's role", example = "/dashboard/maker")
    private String dashboardUrl;

    @Schema(description = "List of authorized permission action codes for this user's role")
    private List<String> permissions;

    @Schema(description = "Authenticated user profile details")
    private UserProfileResponse user;
}
