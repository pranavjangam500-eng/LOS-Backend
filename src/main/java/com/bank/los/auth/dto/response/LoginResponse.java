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

    @Schema(description = "JWT Access token for authorizing subsequent requests")
    private String accessToken;

    @Schema(description = "Refresh token for generating new access tokens")
    private String refreshToken;

    @Schema(description = "Token type (Bearer)", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in milliseconds")
    private Long expiresIn;

    @Schema(description = "User profile details")
    private UserProfileResponse user;

    @Schema(description = "Target role-based dashboard landing route", example = "/dashboard/tenant-admin")
    private String dashboardUrl;

    @Schema(description = "List of permitted features/actions for this role")
    private List<String> permissions;
}
