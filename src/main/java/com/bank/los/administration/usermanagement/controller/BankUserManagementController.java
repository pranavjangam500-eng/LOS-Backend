package com.bank.los.administration.usermanagement.controller;

import com.bank.los.administration.usermanagement.service.BankUserManagementService;
import com.bank.los.bank.user.dto.CreateUserRequest;
import com.bank.los.bank.user.dto.UserResponse;
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
@RequestMapping("/api/v1/administration/user-management")
@RequiredArgsConstructor
@Tag(name = "Bank User Administration", description = "Endpoints for platform administrators to provision and manage bank users")
@SecurityRequirement(name = "BearerAuth")
public class BankUserManagementController {

    private final BankUserManagementService bankUserManagementService;

    @GetMapping("/organizations/{orgId}/users")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all staff and users for a specific bank organization")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listOrganizationUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orgId) {
        List<UserResponse> users = bankUserManagementService.listAllUsersForOrganization(principal, orgId);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Provision a Bank Super Admin / User for an onboarded organization")
    public ResponseEntity<ApiResponse<UserResponse>> provisionBankUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = bankUserManagementService.provisionBankAdmin(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Bank user provisioned successfully", response));
    }
}
