package com.bank.los.auth.controller;

import com.bank.los.auth.dto.request.VerifyOtpRequest;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and 2FA OTP verification")
public class OtpController {

    private final AuthenticationService authenticationService;

    /**
     * Step 2 of the bank/NBFC staff login flow.
     * Always required after a successful email+password step.
     * Accepts the tempSessionToken from step 1 + the 6-digit OTP.
     * Returns the full JWT on success.
     */
    @PostMapping("/verify-otp")
    @Operation(
        summary     = "Verify 2FA OTP",
        description = "Mandatory second step for all bank/NBFC staff logins. " +
                      "Pass the tempSessionToken received in the login response along with the 6-digit OTP."
    )
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        LoginResponse response = authenticationService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }
}
