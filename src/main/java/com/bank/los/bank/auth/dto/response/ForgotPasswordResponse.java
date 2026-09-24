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
@Schema(description = "Response returned after requesting a password reset")
public class ForgotPasswordResponse {

    @Schema(description = "Token validity window in seconds", example = "900")
    private Long expiresInSeconds;

    @Schema(description = "Informative status message", example = "Password reset instructions sent. Check your email.")
    private String message;
}
