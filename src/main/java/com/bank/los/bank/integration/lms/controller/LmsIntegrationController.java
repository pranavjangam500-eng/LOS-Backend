package com.bank.los.bank.integration.lms.controller;

import com.bank.los.bank.integration.lms.dto.LmsDisbursalSyncRequest;
import com.bank.los.bank.integration.lms.dto.LmsDisbursalSyncResponse;
import com.bank.los.bank.integration.lms.service.LmsIntegrationService;
import com.bank.los.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank/integration/lms")
@RequiredArgsConstructor
@Tag(name = "LMS Integration", description = "Core Banking / Loan Management System (LMS) Integrations")
public class LmsIntegrationController {

    private final LmsIntegrationService lmsIntegrationService;

    @PostMapping("/disburse-sync")
    @PreAuthorize("hasAnyRole('CHECKER', 'TENANT_ADMIN', 'ADMIN')")
    @Operation(summary = "Handoff to LMS", description = "Trigger loan booking and disbursal synchronization with LMS")
    public ResponseEntity<ApiResponse<LmsDisbursalSyncResponse>> syncDisbursal(
            @Valid @RequestBody LmsDisbursalSyncRequest request) {
        LmsDisbursalSyncResponse response = lmsIntegrationService.syncDisbursal(request);
        return ResponseEntity.ok(ApiResponse.ok("Disbursal synced with LMS successfully", response));
    }
}
