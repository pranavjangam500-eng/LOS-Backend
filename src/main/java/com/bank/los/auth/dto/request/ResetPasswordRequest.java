package com.bank.los.auth.dto.request;

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
public class ResetPasswordRequest {

    @NotBlank(message = "Email or User Code is required")
    @Schema(description = "User's registered email address or user code", example = "maker@hdfcbank.com")
    private String email;

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token or OTP received via email", example = "rst_9a8b7c6d5e4f")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password adhering to complexity requirements", example = "NewPass@123")
    private String newPassword;
}
