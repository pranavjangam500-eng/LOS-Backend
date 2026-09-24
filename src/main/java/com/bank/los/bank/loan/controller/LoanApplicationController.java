package com.bank.los.bank.loan.controller;

import com.bank.los.bank.loan.dto.CreateLoanApplicationRequest;
import com.bank.los.bank.loan.dto.LoanApplicationResponse;
import com.bank.los.bank.loan.service.LoanApplicationService;
import com.bank.los.common.response.ApiResponse;
import com.bank.los.common.response.PageResponse;
import com.bank.los.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Applications", description = "Endpoints for creating and viewing loan applications")
@SecurityRequirement(name = "BearerAuth")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get paginated loan applications for the organization")
    public ResponseEntity<ApiResponse<PageResponse<LoanApplicationResponse>>> getApplications(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LoanApplicationResponse> apps = loanApplicationService.getApplications(principal, pageable);
        return ResponseEntity.ok(ApiResponse.ok(apps));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER', 'CHECKER', 'VIEWER', 'CUSTOMER')")
    @Operation(summary = "Get loan application details by ID")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplicationById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        LoanApplicationResponse app = loanApplicationService.getApplicationById(principal, id);
        return ResponseEntity.ok(ApiResponse.ok(app));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MAKER')")
    @Operation(summary = "Initiate a new loan application (Maker action)")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> createApplication(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateLoanApplicationRequest request) {
        LoanApplicationResponse app = loanApplicationService.createApplication(principal, request);
        return ResponseEntity.ok(ApiResponse.ok("Loan application created successfully", app));
    }
}
