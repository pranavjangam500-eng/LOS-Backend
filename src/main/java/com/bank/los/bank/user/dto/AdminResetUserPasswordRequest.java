package com.bank.los.bank.user.dto;

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
@Schema(description = "Admin-initiated password reset request for a staff user")
public class AdminResetUserPasswordRequest {

    @NotBlank(message = "New password is required")
    private String newPassword;
}
