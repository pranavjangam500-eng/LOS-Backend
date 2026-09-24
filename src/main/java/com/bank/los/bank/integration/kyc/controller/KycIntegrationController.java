package com.bank.los.bank.integration.kyc.controller;

import com.bank.los.bank.integration.kyc.dto.KycVerificationRequest;
import com.bank.los.bank.integration.kyc.dto.KycVerificationResponse;
import com.bank.los.bank.integration.kyc.service.KycIntegrationService;
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
@RequestMapping("/api/v1/bank/integration/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC Integration", description = "Third-party KYC Verification Integrations")
public class KycIntegrationController {

    private final KycIntegrationService kycIntegrationService;

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'TENANT_ADMIN', 'ADMIN')")
    @Operation(summary = "Verify KYC document", description = "Trigger KYC verification via simulated third-party gateway")
    public ResponseEntity<ApiResponse<KycVerificationResponse>> verifyDocument(
            @Valid @RequestBody KycVerificationRequest request) {
        KycVerificationResponse response = kycIntegrationService.verifyDocument(request);
        return ResponseEntity.ok(ApiResponse.ok("KYC verification completed", response));
    }
}
