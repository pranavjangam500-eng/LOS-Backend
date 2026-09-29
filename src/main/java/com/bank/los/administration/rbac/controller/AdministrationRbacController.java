package com.bank.los.administration.rbac.controller;

import com.bank.los.administration.rbac.dto.*;
import com.bank.los.administration.rbac.service.AdministrationRbacService;
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
@RequestMapping("/api/v1/administration/rbac")
@RequiredArgsConstructor
@Tag(name = "Administration RBAC", description = "Central Platform Permission & Master Role Management")
@SecurityRequirement(name = "BearerAuth")
public class AdministrationRbacController {

    private final AdministrationRbacService administrationRbacService;

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "List all central master permissions")
    public ResponseEntity<ApiResponse<List<MasterPermissionResponse>>> getAllPermissions() {
        List<MasterPermissionResponse> perms = administrationRbacService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.ok(perms));
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Create a new central master permission")
    public ResponseEntity<ApiResponse<MasterPermissionResponse>> createPermission(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateMasterPermissionRequest request) {
        MasterPermissionResponse created = administrationRbacService.createPermission(request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Permission created successfully in Master DB", created));
    }

    @PutMapping("/permissions/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Update a master permission's description or module")
    public ResponseEntity<ApiResponse<MasterPermissionResponse>> updatePermission(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Integer id,
            @Valid @RequestBody UpdateMasterPermissionRequest request) {
        MasterPermissionResponse updated = administrationRbacService.updatePermission(id, request, principal);
        return ResponseEntity.ok(ApiResponse.ok("Permission updated successfully in Master DB", updated));
    }

    @GetMapping("/banks/{bankCode}/summary")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "View complete RBAC summary for a specific Bank/NBFC (roles, designations, overrides)")
    public ResponseEntity<ApiResponse<BankRbacSummaryResponse>> getBankRbacSummary(@PathVariable String bankCode) {
        BankRbacSummaryResponse summary = administrationRbacService.getBankRbacSummary(bankCode);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PostMapping("/sync/{bankCode}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Push/Sync master permissions to a specific Bank/NBFC without altering bank overrides")
    public ResponseEntity<ApiResponse<Void>> syncPermissionsToBank(@PathVariable String bankCode) {
        administrationRbacService.syncPermissionsToBank(bankCode);
        return ResponseEntity.ok(ApiResponse.ok("Master permissions synced to bank " + bankCode + " successfully", null));
    }

    @PostMapping("/sync-all")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Push/Sync master permissions to ALL active Bank/NBFC databases")
    public ResponseEntity<ApiResponse<Void>> syncPermissionsToAllBanks() {
        administrationRbacService.syncPermissionsToAllBanks();
        return ResponseEntity.ok(ApiResponse.ok("Master permissions synced to all banks successfully", null));
    }
}
