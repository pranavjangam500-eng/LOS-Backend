package com.bank.los.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    @Schema(description = "Password reset token generated for testing/reset flow", example = "rst_9a8b7c6d5e4f")
    private String resetToken;

    @Schema(description = "Token expiration duration in seconds", example = "900")
    private Long expiresInSeconds;

    @Schema(description = "Status message", example = "Password reset token issued successfully. Please use this token to reset your password.")
    private String message;
}
