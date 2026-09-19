package com.bank.los.auth.controller;

import com.bank.los.auth.dto.request.ChangePasswordRequest;
import com.bank.los.auth.dto.request.LoginRequest;
import com.bank.los.auth.dto.request.RefreshTokenRequest;
import com.bank.los.auth.dto.response.LoginResponse;
import com.bank.los.auth.dto.response.TokenResponse;
import com.bank.los.auth.dto.response.UserProfileResponse;
import com.bank.los.auth.service.AuthenticationService;
import com.bank.los.auth.service.TokenService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for multi-tenant dynamic login, token refresh, and password management")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user via email / user code and password with dynamic tenant database routing")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using a valid refresh token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = tokenService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = authenticationService.getCurrentUserProfile(principal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password for currently authenticated user", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", "OK"));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint for authentication service")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.ok("LOS Authentication Service is up and running", "HEALTHY"));
    }
}
