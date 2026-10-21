package com.bank.los.bank.loan.service;

import com.bank.los.bank.loan.dto.CreateLoanApplicationRequest;
import com.bank.los.bank.loan.dto.IncomeExpenseItemDto;
import com.bank.los.bank.loan.dto.LoanApplicationResponse;
import com.bank.los.bank.loan.entity.IncomeExpenseItem;
import com.bank.los.bank.loan.entity.LoanApplication;
import com.bank.los.bank.loan.repository.LoanApplicationRepository;
import com.bank.los.bank.master.entity.Customer;
import com.bank.los.bank.master.repository.CustomerRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanProductRepository loanProductRepository;
    private final CustomerRepository customerRepository;

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

    public LoanApplicationResponse getApplicationByNumber(UserPrincipal principal, String applicationNumber) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());
        LoanApplication app = loanApplicationRepository.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "applicationNumber", applicationNumber));
        return mapToResponse(app);
    }

    @Transactional
    public LoanApplicationResponse createApplication(UserPrincipal principal, CreateLoanApplicationRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        validateEligibilityRequest(request);

        String productCode = resolveProductCode(request);
        LoanProduct product = loanProductRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "code", productCode));

        Double requestedAmount = resolveRequestedAmount(request);
        if (product.getMinAmount() != null && product.getMaxAmount() != null) {
            if (requestedAmount < product.getMinAmount() || requestedAmount > product.getMaxAmount()) {
                throw new BusinessException("INVALID_AMOUNT",
                        String.format("Requested amount must be between %.2f and %.2f for product %s",
                                product.getMinAmount(), product.getMaxAmount(), product.getCode()));
            }
        }

        Double annualRate = resolveInterestRate(request, product);
        Integer tenureMonths = resolveTenureMonths(request);

        // Section 2 calculations
        CashflowCalculation cashflow = calculateCashflow(request.getIncomeExpenses(),
                request.getTotalMonthlyIncome(), request.getTotalMonthlyExpenses());

        // Section 3
        Double reqDebt = request.getExistingMonthlyDebt();
        Double existingDebt = reqDebt != null ? reqDebt : 0.0;
        Integer reqCibil = request.getCibilScore();
        Integer cibilScore = reqCibil != null ? reqCibil : 750;
        Double collateralVal = request.getCollateralValue();

        // Underwriting calculations
        EligibilityAssessment assessment = performEligibilityAssessment(
                requestedAmount, annualRate, tenureMonths,
                cashflow.totalIncome, cashflow.netIncome, existingDebt, cibilScore, collateralVal);

        String appNumber = "APP-" + (principal.getOrganizationCode() != null ? principal.getOrganizationCode() : "BANK")
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Customer and Applicant Name resolution
        ResolvedCustomer resolvedCustomer = resolveCustomerInfo(request);

        Long effectiveBranchId = request.getBranchId();
        if (effectiveBranchId == null) {
            effectiveBranchId = principal.getBranchId();
        }
        if (effectiveBranchId == null) {
            effectiveBranchId = 1L;
        }

        LoanApplication app = LoanApplication.builder()
                .applicationNumber(appNumber)
                .customerNumber(resolvedCustomer.customerNumber)
                .applicantName(resolvedCustomer.applicantName)
                .customerId(resolvedCustomer.customerId)
                .branchId(effectiveBranchId)
                .productCode(product.getCode())
                .appliedAmount(requestedAmount)
                .interestRate(annualRate)
                .tenureMonths(tenureMonths)
                .incomeExpenses(cashflow.items)
                .totalMonthlyIncome(cashflow.totalIncome)
                .totalMonthlyExpenses(cashflow.totalExpenses)
                .netMonthlyIncome(cashflow.netIncome)
                .existingMonthlyDebt(existingDebt)
                .cibilScore(cibilScore)
                .collateralValue(collateralVal)
                .emi(assessment.emi)
                .dtiRatio(assessment.dtiRatio)
                .maxEligibleAmount(assessment.maxEligibleAmount)
                .eligibilityStatus(assessment.eligibilityStatus)
                .sanctionedAmount(assessment.sanctionedAmount)
                .makerUserId(principal.getId())
                .status("SUBMITTED_TO_CHECKER")
                .kycStatus("VERIFIED")
                .build();

        LoanApplication saved = loanApplicationRepository.save(app);
        log.info("Loan application/eligibility created: appNumber={}, customer={}, status={}",
                appNumber, resolvedCustomer.customerNumber, assessment.eligibilityStatus);
        return mapToResponse(saved);
    }

    @Transactional
    public LoanApplicationResponse updateApplication(UserPrincipal principal, Long id, CreateLoanApplicationRequest request) {
        OrganizationContext.setCurrentOrganization(principal.getOrganizationDbName());

        LoanApplication app = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));

        validateEligibilityRequest(request);

        String productCode = resolveProductCode(request);
        LoanProduct product = loanProductRepository.findByCode(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", "code", productCode));

        Double requestedAmount = resolveRequestedAmount(request);
        Double annualRate = resolveInterestRate(request, product);
        Integer tenureMonths = resolveTenureMonths(request);

        CashflowCalculation cashflow = calculateCashflow(request.getIncomeExpenses(),
                request.getTotalMonthlyIncome(), request.getTotalMonthlyExpenses());

        Double reqDebt = request.getExistingMonthlyDebt();
        Double existingDebt = reqDebt != null ? reqDebt : 0.0;
        Integer reqCibil = request.getCibilScore();
        Integer cibilScore = reqCibil != null ? reqCibil : 750;
        Double collateralVal = request.getCollateralValue();

        EligibilityAssessment assessment = performEligibilityAssessment(
                requestedAmount, annualRate, tenureMonths,
                cashflow.totalIncome, cashflow.netIncome, existingDebt, cibilScore, collateralVal);

        ResolvedCustomer resolvedCustomer = resolveCustomerInfo(request);

        app.setCustomerNumber(resolvedCustomer.customerNumber);
        app.setApplicantName(resolvedCustomer.applicantName);
        if (resolvedCustomer.customerId != null) {
            app.setCustomerId(resolvedCustomer.customerId);
        }
        if (request.getBranchId() != null) {
            app.setBranchId(request.getBranchId());
        }
        app.setProductCode(product.getCode());
        app.setAppliedAmount(requestedAmount);
        app.setInterestRate(annualRate);
        app.setTenureMonths(tenureMonths);
        app.setIncomeExpenses(cashflow.items);
        app.setTotalMonthlyIncome(cashflow.totalIncome);
        app.setTotalMonthlyExpenses(cashflow.totalExpenses);
        app.setNetMonthlyIncome(cashflow.netIncome);
        app.setExistingMonthlyDebt(existingDebt);
        app.setCibilScore(cibilScore);
        app.setCollateralValue(collateralVal);
        app.setEmi(assessment.emi);
        app.setDtiRatio(assessment.dtiRatio);
        app.setMaxEligibleAmount(assessment.maxEligibleAmount);
        app.setEligibilityStatus(assessment.eligibilityStatus);
        app.setSanctionedAmount(assessment.sanctionedAmount);

        LoanApplication saved = loanApplicationRepository.save(app);
        log.info("Loan application/eligibility updated: id={}, status={}", saved.getId(), saved.getEligibilityStatus());
        return mapToResponse(saved);
    }

    private void validateEligibilityRequest(CreateLoanApplicationRequest request) {
        String productCode = resolveProductCode(request);
        if (productCode == null || productCode.isBlank()) {
            throw new BusinessException("PRODUCT_REQUIRED", "Loan product is required");
        }

        Double reqAmt = resolveRequestedAmount(request);
        if (reqAmt == null || reqAmt <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Requested amount must be greater than zero");
        }

        Integer term = resolveTenureMonths(request);
        if (term != null && term <= 0) {
            throw new BusinessException("INVALID_TENURE", "Loan term in months must be greater than zero");
        }

        Double rate = resolveInterestRate(request, null);
        if (rate != null && rate < 0) {
            throw new BusinessException("INVALID_INTEREST_RATE", "Annual interest rate cannot be negative");
        }

        if (request.getExistingMonthlyDebt() != null && request.getExistingMonthlyDebt() < 0) {
            throw new BusinessException("INVALID_DEBT", "Existing monthly debt cannot be negative");
        }

        if (request.getCibilScore() != null) {
            if (request.getCibilScore() < 300 || request.getCibilScore() > 900) {
                throw new BusinessException("INVALID_CIBIL_SCORE", "CIBIL Score must be between 300 and 900");
            }
        }

        if (request.getCollateralValue() != null && request.getCollateralValue() < 0) {
            throw new BusinessException("INVALID_COLLATERAL", "Collateral value cannot be negative");
        }

        if (request.getIncomeExpenses() != null) {
            for (int i = 0; i < request.getIncomeExpenses().size(); i++) {
                IncomeExpenseItemDto item = request.getIncomeExpenses().get(i);
                if (item == null) continue;
                if (item.getType() == null || (!"INCOME".equalsIgnoreCase(item.getType().trim()) && !"EXPENSE".equalsIgnoreCase(item.getType().trim()))) {
                    throw new BusinessException("INVALID_CASHFLOW_TYPE", "Item at index " + i + " must have type 'INCOME' or 'EXPENSE'");
                }
                if (item.getMonthlyAmount() != null && item.getMonthlyAmount() < 0) {
                    throw new BusinessException("INVALID_CASHFLOW_AMOUNT", "Monthly amount at index " + i + " cannot be negative");
                }
            }
        }
    }

    private CashflowCalculation calculateCashflow(List<IncomeExpenseItemDto> itemsDto, Double manualIncome, Double manualExpenses) {
        List<IncomeExpenseItem> items = new ArrayList<>();
        double totalInc = 0.0;
        double totalExp = 0.0;

        if (itemsDto != null && !itemsDto.isEmpty()) {
            for (IncomeExpenseItemDto dto : itemsDto) {
                if (dto == null) continue;
                String type = dto.getType() != null ? dto.getType().trim().toUpperCase() : "INCOME";
                String source = dto.getSource() != null ? dto.getSource().trim() : "Unspecified";
                Double monthlyAmount = dto.getMonthlyAmount();
                double amt = monthlyAmount != null ? monthlyAmount : 0.0;

                items.add(IncomeExpenseItem.builder()
                        .type(type)
                        .source(source)
                        .monthlyAmount(amt)
                        .build());

                if ("INCOME".equalsIgnoreCase(type)) {
                    totalInc += amt;
                } else if ("EXPENSE".equalsIgnoreCase(type)) {
                    totalExp += amt;
                }
            }
        } else {
            if (manualIncome != null && manualIncome > 0) {
                totalInc = manualIncome;
                items.add(IncomeExpenseItem.builder().type("INCOME").source("Total Declared Income").monthlyAmount(manualIncome).build());
            }
            if (manualExpenses != null && manualExpenses > 0) {
                totalExp = manualExpenses;
                items.add(IncomeExpenseItem.builder().type("EXPENSE").source("Total Declared Expenses").monthlyAmount(manualExpenses).build());
            }
        }

        totalInc = Math.round(totalInc * 100.0) / 100.0;
        totalExp = Math.round(totalExp * 100.0) / 100.0;
        double netInc = Math.round((totalInc - totalExp) * 100.0) / 100.0;

        return new CashflowCalculation(items, totalInc, totalExp, netInc);
    }

    private EligibilityAssessment performEligibilityAssessment(
            Double requestedAmount, Double annualInterestRate, Integer loanTermMonths,
            Double totalMonthlyIncome, Double netMonthlyIncome, Double existingMonthlyDebt,
            Integer cibilScore, Double collateralValue) {

        double r = (annualInterestRate / 100.0) / 12.0;
        int n = loanTermMonths;

        // 1. Calculate Monthly EMI for requested loan
        double emi;
        if (r > 0 && n > 0) {
            double factor = Math.pow(1.0 + r, n);
            emi = (requestedAmount * r * factor) / (factor - 1.0);
        } else {
            emi = n > 0 ? requestedAmount / n : 0.0;
        }
        emi = Math.round(emi * 100.0) / 100.0;

        // 2. DTI Ratio (Debt-to-Income / FOIR %): (existingDebt + emi) / totalIncome * 100
        double totalObligations = existingMonthlyDebt + emi;
        double dtiRatio;
        if (totalMonthlyIncome > 0) {
            dtiRatio = Math.round(((totalObligations / totalMonthlyIncome) * 100.0) * 100.0) / 100.0;
        } else {
            dtiRatio = 100.0;
        }

        // 3. Max Eligible Amount: based on 50% max allowable FOIR/DTI on total income minus existing debt
        double maxAllowableEmi = Math.max(0.0, (totalMonthlyIncome * 0.50) - existingMonthlyDebt);
        double maxEligibleAmount;
        if (r > 0 && n > 0) {
            double factor = Math.pow(1.0 + r, n);
            maxEligibleAmount = (maxAllowableEmi * (factor - 1.0)) / (r * factor);
        } else {
            maxEligibleAmount = maxAllowableEmi * n;
        }
        maxEligibleAmount = Math.round(maxEligibleAmount * 100.0) / 100.0;

        // Incorporate collateral enhancement if provided (loan can be up to 80% LTV of collateral)
        if (collateralValue != null && collateralValue > 0) {
            double collateralCap = Math.round(collateralValue * 0.80 * 100.0) / 100.0;
            if (collateralCap > maxEligibleAmount) {
                // If collateral is substantial, bank may offer higher eligible amount capped by income
                maxEligibleAmount = Math.max(maxEligibleAmount, Math.min(requestedAmount, collateralCap));
            }
        }

        // 4. Eligibility Status & Sanctioned Amount
        String status;
        double sanctioned;

        if (cibilScore < 600 || netMonthlyIncome <= 0 || dtiRatio > 65.0) {
            status = "REJECTED";
            sanctioned = 0.0;
        } else if (cibilScore >= 700 && dtiRatio <= 50.0 && requestedAmount <= maxEligibleAmount) {
            status = "ELIGIBLE";
            sanctioned = requestedAmount;
        } else {
            status = "REFERRED";
            sanctioned = Math.min(requestedAmount, maxEligibleAmount);
        }

        return new EligibilityAssessment(emi, dtiRatio, maxEligibleAmount, status, sanctioned);
    }

    private ResolvedCustomer resolveCustomerInfo(CreateLoanApplicationRequest request) {
        String customerNumber = request.getCustomerNumber();
        String applicantName = request.getApplicantName();
        Long customerId = request.getCustomerId();

        if (customerNumber != null && !customerNumber.isBlank()) {
            Optional<Customer> custOpt = customerRepository.findByCustomerCode(customerNumber.trim());
            if (custOpt.isPresent()) {
                Customer c = custOpt.get();
                customerId = c.getId();
                if (applicantName == null || applicantName.isBlank()) {
                    applicantName = (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
                }
            }
        } else if (customerId != null) {
            Optional<Customer> custOpt = customerRepository.findById(customerId);
            if (custOpt.isPresent()) {
                Customer c = custOpt.get();
                customerNumber = c.getCustomerCode();
                if (applicantName == null || applicantName.isBlank()) {
                    applicantName = (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim();
                }
            }
        }

        if (customerNumber == null || customerNumber.isBlank()) {
            customerNumber = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (applicantName == null || applicantName.isBlank()) {
            applicantName = "Applicant " + customerNumber;
        }

        return new ResolvedCustomer(customerNumber, applicantName, customerId);
    }

    private String resolveProductCode(CreateLoanApplicationRequest req) {
        if (req.getLoanProduct() != null && !req.getLoanProduct().isBlank()) {
            return req.getLoanProduct().trim().toUpperCase();
        }
        if (req.getProductCode() != null && !req.getProductCode().isBlank()) {
            return req.getProductCode().trim().toUpperCase();
        }
        return null;
    }

    private Double resolveRequestedAmount(CreateLoanApplicationRequest req) {
        if (req.getRequestedAmount() != null) return req.getRequestedAmount();
        if (req.getAppliedAmount() != null) return req.getAppliedAmount();
        return null;
    }

    private Double resolveInterestRate(CreateLoanApplicationRequest req, LoanProduct product) {
        if (req.getAnnualInterestRate() != null) return req.getAnnualInterestRate();
        if (req.getInterestRate() != null) return req.getInterestRate();
        if (product != null && product.getInterestRatePercent() != null) return product.getInterestRatePercent();
        return 12.0; // Default 12%
    }

    private Integer resolveTenureMonths(CreateLoanApplicationRequest req) {
        if (req.getLoanTermMonths() != null) return req.getLoanTermMonths();
        if (req.getTenureMonths() != null) return req.getTenureMonths();
        return 60; // Default 60 months
    }

    public LoanApplicationResponse mapToResponse(LoanApplication a) {
        List<IncomeExpenseItemDto> items = a.getIncomeExpenses() != null
                ? a.getIncomeExpenses().stream()
                .map(i -> IncomeExpenseItemDto.builder()
                        .type(i.getType())
                        .source(i.getSource())
                        .monthlyAmount(i.getMonthlyAmount())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return LoanApplicationResponse.builder()
                .id(a.getId())
                .applicationNumber(a.getApplicationNumber())
                .customerNumber(a.getCustomerNumber())
                .applicantName(a.getApplicantName())
                .customerId(a.getCustomerId())
                .branchId(a.getBranchId())
                .productCode(a.getProductCode())
                .loanProduct(a.getProductCode())
                .appliedAmount(a.getAppliedAmount())
                .requestedAmount(a.getAppliedAmount())
                .interestRate(a.getInterestRate())
                .annualInterestRate(a.getInterestRate())
                .tenureMonths(a.getTenureMonths())
                .loanTermMonths(a.getTenureMonths())
                .incomeExpenses(items)
                .totalMonthlyIncome(a.getTotalMonthlyIncome())
                .totalMonthlyExpenses(a.getTotalMonthlyExpenses())
                .netMonthlyIncome(a.getNetMonthlyIncome())
                .existingMonthlyDebt(a.getExistingMonthlyDebt())
                .cibilScore(a.getCibilScore())
                .collateralValue(a.getCollateralValue())
                .emi(a.getEmi())
                .dtiRatio(a.getDtiRatio())
                .maxEligibleAmount(a.getMaxEligibleAmount())
                .eligibilityStatus(a.getEligibilityStatus())
                .sanctionedAmount(a.getSanctionedAmount())
                .kycStatus(a.getKycStatus())
                .makerUserId(a.getMakerUserId())
                .checkerUserId(a.getCheckerUserId())
                .status(a.getStatus())
                .checkerRemarks(a.getCheckerRemarks())
                .approvedAt(a.getApprovedAt())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private static class CashflowCalculation {
        final List<IncomeExpenseItem> items;
        final double totalIncome;
        final double totalExpenses;
        final double netIncome;

        CashflowCalculation(List<IncomeExpenseItem> items, double totalIncome, double totalExpenses, double netIncome) {
            this.items = items;
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
            this.netIncome = netIncome;
        }
    }

    private static class EligibilityAssessment {
        final double emi;
        final double dtiRatio;
        final double maxEligibleAmount;
        final String eligibilityStatus;
        final double sanctionedAmount;

        EligibilityAssessment(double emi, double dtiRatio, double maxEligibleAmount, String eligibilityStatus, double sanctionedAmount) {
            this.emi = emi;
            this.dtiRatio = dtiRatio;
            this.maxEligibleAmount = maxEligibleAmount;
            this.eligibilityStatus = eligibilityStatus;
            this.sanctionedAmount = sanctionedAmount;
        }
    }

    private static class ResolvedCustomer {
        final String customerNumber;
        final String applicantName;
        final Long customerId;

        ResolvedCustomer(String customerNumber, String applicantName, Long customerId) {
            this.customerNumber = customerNumber;
            this.applicantName = applicantName;
            this.customerId = customerId;
        }
    }
}
