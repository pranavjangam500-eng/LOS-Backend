package com.bank.los.bank.integration.bureau.controller;

import com.bank.los.bank.integration.bureau.dto.BureauCheckRequest;
import com.bank.los.bank.integration.bureau.dto.BureauCheckResponse;
import com.bank.los.bank.integration.bureau.service.CreditBureauIntegrationService;
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
@RequestMapping("/api/v1/bank/integration/bureau")
@RequiredArgsConstructor
@Tag(name = "Credit Bureau Integration", description = "Credit Bureau (CIBIL/Experian/Equifax/CRIF) Integrations")
public class CreditBureauIntegrationController {

    private final CreditBureauIntegrationService creditBureauIntegrationService;

    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'TENANT_ADMIN', 'ADMIN')")
    @Operation(summary = "Fetch Credit Bureau Report", description = "Query credit score and credit report from Credit Bureau")
    public ResponseEntity<ApiResponse<BureauCheckResponse>> checkCreditScore(
            @Valid @RequestBody BureauCheckRequest request) {
        BureauCheckResponse response = creditBureauIntegrationService.checkCreditScore(request);
        return ResponseEntity.ok(ApiResponse.ok("Credit report retrieved successfully", response));
    }
}
