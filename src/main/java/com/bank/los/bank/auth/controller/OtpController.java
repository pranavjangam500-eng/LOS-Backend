package com.bank.los.bank.auth.controller;

import com.bank.los.bank.auth.dto.request.VerifyOtpRequest;
import com.bank.los.bank.auth.dto.response.LoginResponse;
import com.bank.los.bank.auth.service.AuthenticationService;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication (2FA OTP)", description = "Endpoints for 2FA OTP verification for bank staff users")
public class OtpController {

    private final AuthenticationService authenticationService;

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify 2FA OTP and issue full JWT access token (Step 2 of login)")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        LoginResponse response = authenticationService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("2FA OTP verified successfully", response));
    }
}
