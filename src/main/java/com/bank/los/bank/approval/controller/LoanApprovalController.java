package com.bank.los.bank.approval.controller;

import com.bank.los.bank.approval.dto.ApprovalDecisionRequest;
import com.bank.los.bank.approval.service.LoanApprovalService;
import com.bank.los.bank.loan.dto.LoanApplicationResponse;
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

@RestController
@RequestMapping("/api/v1/loan-approvals")
@RequiredArgsConstructor
@Tag(name = "Loan Approvals", description = "Maker-Checker dual-control workflow for loan sanctioning")
@SecurityRequirement(name = "BearerAuth")
public class LoanApprovalController {

    private final LoanApprovalService loanApprovalService;

    @PostMapping("/{applicationId}/decision")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'CHECKER')")
    @Operation(summary = "Approve, Reject, or Return a loan application (Checker action with maker-checker validation)")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> processDecision(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId,
            @Valid @RequestBody ApprovalDecisionRequest request) {
        LoanApplicationResponse response = loanApprovalService.processDecision(principal, applicationId, request);
        return ResponseEntity.ok(ApiResponse.ok("Decision processed successfully", response));
    }
}
