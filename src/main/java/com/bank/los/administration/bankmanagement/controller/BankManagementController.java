package com.bank.los.administration.bankmanagement.controller;

import com.bank.los.administration.bankmanagement.dto.BankStatusUpdateRequest;
import com.bank.los.administration.bankmanagement.service.BankManagementService;
import com.bank.los.administration.organization.dto.CreateOrganizationRequest;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.administration.organization.service.OrganizationService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.config.BankContext;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> listAllBanks(
            @AuthenticationPrincipal UserPrincipal principal) {
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        List<OrganizationResponse> banks = organizationService.getAllOrganizations(principal);
        return ResponseEntity.ok(ApiResponse.ok(banks));
    }

    @PostMapping("/onboard")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Onboard a new Bank/NBFC institution with full regulatory and multi-tenant details")
    public ResponseEntity<ApiResponse<OrganizationResponse>> onboardBank(
            @Valid @RequestBody CreateOrganizationRequest request) {
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.ok(ApiResponse.ok("Bank onboarded and database provisioned successfully", response));
    }

    @PatchMapping("/{organizationId}/status")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update operational status (ACTIVE, INACTIVE, SUSPENDED) of a bank/NBFC")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateBankStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long organizationId,
            @Valid @RequestBody BankStatusUpdateRequest request) {
        if (principal != null && !principal.belongsToOrganization(organizationId)) {
            throw new AccessDeniedException("Access denied: You cannot update status of organization " + organizationId);
        }
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        OrganizationResponse response = bankManagementService.updateBankStatus(organizationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Organization status updated successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update an existing bank/NBFC institution by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody com.bank.los.administration.organization.dto.UpdateOrganizationRequest request) {
        if (principal != null && !principal.belongsToOrganization(id)) {
            throw new AccessDeniedException("Access denied: You cannot modify organization " + id);
        }
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Bank updated successfully", updated));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('INTERNAL_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Partially update an existing bank/NBFC institution by ID, UUID, or Bank Code")
    public ResponseEntity<ApiResponse<OrganizationResponse>> patchBank(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @RequestBody com.bank.los.administration.organization.dto.UpdateOrganizationRequest request) {
        if (principal != null && !principal.belongsToOrganization(id)) {
            throw new AccessDeniedException("Access denied: You cannot modify organization " + id);
        }
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Bank updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INTERNAL_ADMIN')")
    @Operation(summary = "Delete a bank/NBFC institution (soft-delete by default, or hard delete with ?hardDelete=true)")
    public ResponseEntity<ApiResponse<Void>> deleteBank(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean hardDelete) {
        BankContext.setCurrentBank(BankContext.MASTER_BANK_ID);
        OrganizationContext.setCurrentOrganization(OrganizationContext.MASTER_ORG_ID);
        organizationService.deleteOrganization(id, hardDelete);
        String msg = hardDelete ? "Bank permanently deleted successfully" : "Bank deactivated/deleted successfully";
        return ResponseEntity.ok(ApiResponse.ok(msg, null));
    }
}


