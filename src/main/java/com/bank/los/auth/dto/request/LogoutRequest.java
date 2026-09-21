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
@Schema(description = "User logout payload containing refresh token to revoke")
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required for logout")
    @Schema(description = "Refresh token to invalidate")
    private String refreshToken;
}
