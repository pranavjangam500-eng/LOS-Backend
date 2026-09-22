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
@Schema(description = "2FA OTP verification request — always required for bank/NBFC staff logins")
public class VerifyOtpRequest {

    @NotBlank(message = "Temporary session token is required")
    @Schema(description = "The tempSessionToken returned from the initial login response", example = "eyJ...")
    private String tempSessionToken;

    @NotBlank(message = "OTP is required")
    @Schema(description = "6-digit OTP received on email", example = "482913")
    private String otp;
}
