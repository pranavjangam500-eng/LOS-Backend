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
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
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
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Update operational status (ACTIVE, INACTIVE, SUSPENDED) of a bank/NBFC")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateBankStatus(
            @PathVariable Long organizationId,
            @Valid @RequestBody BankStatusUpdateRequest request) {
        OrganizationResponse response = bankManagementService.updateBankStatus(organizationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Organization status updated successfully", response));
    }
}

