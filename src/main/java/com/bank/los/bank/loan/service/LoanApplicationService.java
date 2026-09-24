package com.bank.los.bank.loan.service;

import com.bank.los.bank.loan.dto.CreateLoanApplicationRequest;
import com.bank.los.bank.loan.dto.LoanApplicationResponse;
import com.bank.los.bank.loan.entity.LoanApplication;
import com.bank.los.bank.loan.repository.LoanApplicationRepository;
import com.bank.los.bank.product.entity.LoanProduct;
import com.bank.los.bank.product.repository.LoanProductRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.common.response.PageResponse;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;

    public PageResponse<LoanApplicationResponse> getApplications(UserPrincipal principal, Pageable pageable) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        Page<LoanApplication> page = loanApplicationRepository.findAll(pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    public LoanApplicationResponse getApplicationById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        LoanApplication app = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));
        return mapToResponse(app);
    }

    @Transactional
    public LoanApplicationResponse createApplication(UserPrincipal principal, CreateLoanApplicationRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        LoanProduct product = loanProductRepository.findByCode(request.getProductCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "code", request.getProductCode()));

        if (request.getAppliedAmount() < product.getMinAmount() || request.getAppliedAmount() > product.getMaxAmount()) {
            throw new BusinessException("INVALID_AMOUNT", String.format("Applied amount must be between %.2f and %.2f", product.getMinAmount(), product.getMaxAmount()));
        }

        String appNumber = "APP-" + principal.getOrganizationCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        LoanApplication app = LoanApplication.builder()
                .applicationNumber(appNumber)
                .customerId(request.getCustomerId())
                .branchId(request.getBranchId() != null ? request.getBranchId() : (principal.getBranchId() != null ? principal.getBranchId() : 1L))
                .productCode(product.getCode())
                .appliedAmount(request.getAppliedAmount())
                .sanctionedAmount(request.getAppliedAmount())
                .interestRate(product.getInterestRatePercent())
                .tenureMonths(request.getTenureMonths())
                .makerUserId(principal.getId())
                .status("SUBMITTED_TO_CHECKER")
                .kycStatus("VERIFIED")
                .cibilScore(750)
                .build();

        LoanApplication saved = loanApplicationRepository.save(app);
        log.info("Loan application created: appNumber={}, maker={}", appNumber, principal.getUsername());
        return mapToResponse(saved);
    }

    private LoanApplicationResponse mapToResponse(LoanApplication a) {
        return LoanApplicationResponse.builder()
                .id(a.getId())
                .applicationNumber(a.getApplicationNumber())
                .customerId(a.getCustomerId())
                .branchId(a.getBranchId())
                .productCode(a.getProductCode())
                .appliedAmount(a.getAppliedAmount())
                .sanctionedAmount(a.getSanctionedAmount())
                .interestRate(a.getInterestRate())
                .tenureMonths(a.getTenureMonths())
                .cibilScore(a.getCibilScore())
                .kycStatus(a.getKycStatus())
                .makerUserId(a.getMakerUserId())
                .checkerUserId(a.getCheckerUserId())
                .status(a.getStatus())
                .checkerRemarks(a.getCheckerRemarks())
                .approvedAt(a.getApprovedAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
