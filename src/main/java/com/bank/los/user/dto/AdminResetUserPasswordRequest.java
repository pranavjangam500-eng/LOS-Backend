package com.bank.los.user.dto;

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
public class AdminResetUserPasswordRequest {

    @NotBlank(message = "New password is required")
    @Schema(description = "New password conforming to password policy", example = "NewSecurePass@2026")
    private String newPassword;
}
