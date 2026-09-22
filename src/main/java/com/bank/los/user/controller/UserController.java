package com.bank.los.user.controller;

import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import com.bank.los.user.dto.AdminResetUserPasswordRequest;
import com.bank.los.user.dto.CreateUserRequest;
import com.bank.los.user.dto.UserResponse;
import com.bank.los.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Tenant staff user administration (Bank Admin / Super Admin)")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INTERNAL_ADMIN')")
    @Operation(summary = "List all staff users in this bank/NBFC")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(@AuthenticationPrincipal UserPrincipal principal) {
        List<UserResponse> users = userService.getAllUsers(principal);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INTERNAL_ADMIN')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        UserResponse user = userService.getUserById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INTERNAL_ADMIN')")
    @Operation(summary = "Create and onboard a new Bank/NBFC staff user (e.g. Maker, Checker, Viewer)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("User created successfully (Status: PENDING_VERIFICATION)", response));
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INTERNAL_ADMIN')")
    @Operation(summary = "Verify newly created user and change status to OPERATIVE (Maker-Checker policy)")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        UserResponse response = userService.verifyUser(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("User verified successfully and marked OPERATIVE", response));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'INTERNAL_ADMIN')")
    @Operation(summary = "Admin reset password for a staff user (User Password Reset)")
    public ResponseEntity<ApiResponse<Void>> adminResetPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminResetUserPasswordRequest request) {
        userService.adminResetPassword(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("User password reset successfully", null));
    }
}
