package com.bank.los.bank.user.controller;

import com.bank.los.bank.user.dto.AdminResetUserPasswordRequest;
import com.bank.los.bank.user.dto.CreateUserRequest;
import com.bank.los.bank.user.dto.UserResponse;
import com.bank.los.bank.user.service.UserService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for creating, managing, and verifying bank staff users")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List all staff users in the organization (or all organizations if platform admin)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long organizationId) {
        List<UserResponse> users = userService.getAllUsers(principal, organizationId);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @RequestParam(required = false) Long organizationId) {
        UserResponse user = userService.getUserById(principal, id, organizationId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create a new bank staff user (Maker/Checker dual-control: starts in PENDING_VERIFICATION)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", user));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Verify and approve a pending user record (Maker-Checker policy: 2nd admin required)")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        UserResponse user = userService.verifyUser(principal, id);
        return ResponseEntity.ok(ApiResponse.ok("User verified and activated successfully", user));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Admin-initiated password reset for a bank staff user")
    public ResponseEntity<ApiResponse<String>> adminResetPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminResetUserPasswordRequest request) {
        userService.adminResetPassword(principal, id, request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully", "OK"));
    }
}
