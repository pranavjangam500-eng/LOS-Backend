package com.bank.los.bank.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after token refresh")
public class TokenResponse {

    @Schema(description = "New signed JWT Access Token")
    private String accessToken;

    @Schema(description = "New signed JWT Refresh Token")
    private String refreshToken;

    @Schema(description = "Token type prefix", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token lifetime in milliseconds", example = "86400000")
    private Long expiresIn;
}
