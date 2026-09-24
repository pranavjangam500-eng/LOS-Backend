package com.bank.los.administration.bankmanagement.controller;

import com.bank.los.administration.bankmanagement.dto.BankStatusUpdateRequest;
import com.bank.los.administration.bankmanagement.service.BankManagementService;
import com.bank.los.administration.organization.dto.OrganizationResponse;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/administration/banks")
@RequiredArgsConstructor
@Tag(name = "Bank Management", description = "Administration controls for bank onboarding, status updates, and management")
@SecurityRequirement(name = "BearerAuth")
public class BankManagementController {

    private final BankManagementService bankManagementService;

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
