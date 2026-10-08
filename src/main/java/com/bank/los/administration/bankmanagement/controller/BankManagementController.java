package com.bank.los.administration.bankmanagement.controller;

import com.bank.los.administration.bankmanagement.dto.BankStatusUpdateRequest;
import com.bank.los.administration.bankmanagement.service.BankManagementService;
import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.administration.organization.service.OrganizationService;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/administration/banks")
@RequiredArgsConstructor
@Tag(name = "Bank Management", description = "Administration controls for bank onboarding, status updates, and management")
@SecurityRequirement(name = "BearerAuth")
public class BankManagementController {

    private final BankManagementService bankManagementService;
    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "List all onboarded banks/NBFCs")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> listAllBanks() {
        List<OrganizationResponse> banks = organizationService.getAllOrganizations();
        return ResponseEntity.ok(ApiResponse.ok(banks));
    }

    @PostMapping("/onboard")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Onboard a new Bank/NBFC institution with full regulatory and multi-tenant details")
    public ResponseEntity<ApiResponse<OrganizationResponse>> onboardBank(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.ok(ApiResponse.ok("Bank onboarded and database provisioned successfully", response));
    }

    @PatchMapping("/{organizationId}/status")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update operational status (ACTIVE, INACTIVE, SUSPENDED) of a bank/NBFC")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateBankStatus(
            @PathVariable Long organizationId,
            @Valid @RequestBody BankStatusUpdateRequest request) {
        OrganizationResponse response = bankManagementService.updateBankStatus(organizationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Organization status updated successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update an existing bank/NBFC institution by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateBank(
            @PathVariable String id,
            @RequestBody com.bank.los.administration.organization.dto.UpdateOrganizationRequest request) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Bank updated successfully", updated));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Partially update an existing bank/NBFC institution by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> patchBank(
            @PathVariable String id,
            @RequestBody com.bank.los.administration.organization.dto.UpdateOrganizationRequest request) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Bank updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Delete a bank/NBFC institution (soft-delete by default, or hard delete with ?hardDelete=true)")
    public ResponseEntity<ApiResponse<Void>> deleteBank(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean hardDelete) {
        organizationService.deleteOrganization(id, hardDelete);
        String msg = hardDelete ? "Bank permanently deleted successfully" : "Bank deactivated/deleted successfully";
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }
}

