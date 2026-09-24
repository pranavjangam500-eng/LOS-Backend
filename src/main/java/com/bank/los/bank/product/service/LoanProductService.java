package com.bank.los.bank.product.service;

import com.bank.los.bank.product.dto.LoanProductRequest;
import com.bank.los.bank.product.dto.LoanProductResponse;
import com.bank.los.bank.product.entity.LoanProduct;
import com.bank.los.bank.product.repository.LoanProductRepository;
import com.bank.los.common.exception.BusinessException;
import com.bank.los.common.exception.ResourceNotFoundException;
import com.bank.los.config.OrganizationContext;
import com.bank.los.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository loanProductRepository;

    public List<LoanProductResponse> getAllProducts(UserPrincipal principal) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        return loanProductRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanProductResponse getProductById(UserPrincipal principal, Long id) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        LoanProduct product = loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "id", id));
        return mapToResponse(product);
    }

    @Transactional
    public LoanProductResponse createProduct(UserPrincipal principal, LoanProductRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        if (loanProductRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new BusinessException("PRODUCT_EXISTS", "Loan product code " + request.getCode() + " already exists");
        }

        LoanProduct product = LoanProduct.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .interestRatePercent(request.getInterestRatePercent())
                .minTenureMonths(request.getMinTenureMonths())
                .maxTenureMonths(request.getMaxTenureMonths())
                .processingFeePercent(request.getProcessingFeePercent() != null ? request.getProcessingFeePercent() : 1.0)
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .build();

        LoanProduct saved = loanProductRepository.save(product);
        log.info("Loan product created: code={}, org={}", saved.getCode(), principal.getOrganizationCode());
        return mapToResponse(saved);
    }

    private LoanProductResponse mapToResponse(LoanProduct p) {
        return LoanProductResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .description(p.getDescription())
                .minAmount(p.getMinAmount())
                .maxAmount(p.getMaxAmount())
                .interestRatePercent(p.getInterestRatePercent())
                .minTenureMonths(p.getMinTenureMonths())
                .maxTenureMonths(p.getMaxTenureMonths())
                .processingFeePercent(p.getProcessingFeePercent())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
