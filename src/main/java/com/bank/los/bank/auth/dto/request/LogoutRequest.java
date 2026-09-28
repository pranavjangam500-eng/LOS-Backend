package com.bank.los.bank.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for user logout (optional refresh token)")
public class LogoutRequest {

    @Schema(description = "Optional refresh token to revoke upon logout")
    private String refreshToken;
}
