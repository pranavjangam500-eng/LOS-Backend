package com.bank.los.bank.approval.service;

import com.bank.los.bank.approval.dto.ApprovalDecisionRequest;
import com.bank.los.bank.loan.dto.LoanApplicationResponse;
import com.bank.los.bank.loan.entity.LoanApplication;
import com.bank.los.bank.loan.repository.LoanApplicationRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApprovalService {

    private final LoanApplicationRepository loanApplicationRepository;

    @Transactional
    public LoanApplicationResponse processDecision(UserPrincipal principal, Long applicationId, ApprovalDecisionRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        LoanApplication app = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", applicationId));

        // Maker-Checker enforcement: Maker cannot approve their own application
        if (app.getMakerUserId() != null && app.getMakerUserId().equals(principal.getId())) {
            throw new BusinessException("MAKER_CHECKER_VIOLATION", "The maker who created the loan application cannot review or approve it.");
        }

        String decision = request.getDecision().toUpperCase();
        if ("APPROVE".equals(decision)) {
            app.setStatus("APPROVED");
            if (request.getSanctionedAmount() != null && request.getSanctionedAmount() > 0) {
                app.setSanctionedAmount(request.getSanctionedAmount());
            }
            app.setApprovedAt(LocalDateTime.now());
        } else if ("REJECT".equals(decision)) {
            app.setStatus("REJECTED");
        } else if ("RETURN_TO_MAKER".equals(decision)) {
            app.setStatus("RETURNED_FOR_CORRECTION");
        }

        app.setCheckerUserId(principal.getId());
        app.setCheckerRemarks(request.getRemarks());
        app.setUpdatedAt(LocalDateTime.now());

        LoanApplication saved = loanApplicationRepository.save(app);
        log.info("Loan approval decision processed: appNumber={}, decision={}, checker={}",
                saved.getApplicationNumber(), decision, principal.getUsername());

        return LoanApplicationResponse.builder()
                .id(saved.getId())
                .applicationNumber(saved.getApplicationNumber())
                .customerId(saved.getCustomerId())
                .branchId(saved.getBranchId())
                .productCode(saved.getProductCode())
                .appliedAmount(saved.getAppliedAmount())
                .sanctionedAmount(saved.getSanctionedAmount())
                .interestRate(saved.getInterestRate())
                .tenureMonths(saved.getTenureMonths())
                .cibilScore(saved.getCibilScore())
                .kycStatus(saved.getKycStatus())
                .makerUserId(saved.getMakerUserId())
                .checkerUserId(saved.getCheckerUserId())
                .status(saved.getStatus())
                .checkerRemarks(saved.getCheckerRemarks())
                .approvedAt(saved.getApprovedAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
