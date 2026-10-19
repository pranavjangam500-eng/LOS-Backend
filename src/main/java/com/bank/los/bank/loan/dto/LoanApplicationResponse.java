package com.bank.los.bank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Response representing a loan eligibility assessment and application details")
public class LoanApplicationResponse {
    private Long id;
    private String applicationNumber;

    // Section 1: Applicant & Loan Request
    private String customerNumber;
    private String applicantName;
    private Long customerId;
    private Long branchId;
    private String productCode;
    private String loanProduct;
    private Double appliedAmount;
    private Double requestedAmount;
    private Double interestRate;
    private Double annualInterestRate;
    private Integer tenureMonths;
    private Integer loanTermMonths;

    // Section 2: Monthly Income & Expenses
    private List<IncomeExpenseItemDto> incomeExpenses = new ArrayList<>();
    private Double totalMonthlyIncome;
    private Double totalMonthlyExpenses;
    private Double netMonthlyIncome;

    // Section 3: Obligations & Collateral
    private Double existingMonthlyDebt;
    private Integer cibilScore;
    private Double collateralValue;

    // Calculations & Underwriting
    private Double emi;
    private Double dtiRatio;
    private Double maxEligibleAmount;
    private String eligibilityStatus;
    private Double sanctionedAmount;

    // Workflow & Auditing
    private String kycStatus;
    private Long makerUserId;
    private Long checkerUserId;
    private String status;
    private String checkerRemarks;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanApplicationResponse() {
    }

    public LoanApplicationResponse(Long id, String applicationNumber, String customerNumber, String applicantName,
                                   Long customerId, Long branchId, String productCode, String loanProduct,
                                   Double appliedAmount, Double requestedAmount, Double interestRate,
                                   Double annualInterestRate, Integer tenureMonths, Integer loanTermMonths,
                                   List<IncomeExpenseItemDto> incomeExpenses, Double totalMonthlyIncome,
                                   Double totalMonthlyExpenses, Double netMonthlyIncome, Double existingMonthlyDebt,
                                   Integer cibilScore, Double collateralValue, Double emi, Double dtiRatio,
                                   Double maxEligibleAmount, String eligibilityStatus, Double sanctionedAmount,
                                   String kycStatus, Long makerUserId, Long checkerUserId, String status,
                                   String checkerRemarks, LocalDateTime approvedAt, LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        this.id = id;
        this.applicationNumber = applicationNumber;
        this.customerNumber = customerNumber;
        this.applicantName = applicantName;
        this.customerId = customerId;
        this.branchId = branchId;
        this.productCode = productCode;
        this.loanProduct = loanProduct != null ? loanProduct : productCode;
        this.appliedAmount = appliedAmount;
        this.requestedAmount = requestedAmount != null ? requestedAmount : appliedAmount;
        this.interestRate = interestRate;
        this.annualInterestRate = annualInterestRate != null ? annualInterestRate : interestRate;
        this.tenureMonths = tenureMonths;
        this.loanTermMonths = loanTermMonths != null ? loanTermMonths : tenureMonths;
        this.incomeExpenses = incomeExpenses != null ? incomeExpenses : new ArrayList<>();
        this.totalMonthlyIncome = totalMonthlyIncome;
        this.totalMonthlyExpenses = totalMonthlyExpenses;
        this.netMonthlyIncome = netMonthlyIncome;
        this.existingMonthlyDebt = existingMonthlyDebt;
        this.cibilScore = cibilScore;
        this.collateralValue = collateralValue;
        this.emi = emi;
        this.dtiRatio = dtiRatio;
        this.maxEligibleAmount = maxEligibleAmount;
        this.eligibilityStatus = eligibilityStatus;
        this.sanctionedAmount = sanctionedAmount;
        this.kycStatus = kycStatus;
        this.makerUserId = makerUserId;
        this.checkerUserId = checkerUserId;
        this.status = status;
        this.checkerRemarks = checkerRemarks;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LoanApplicationResponseBuilder builder() {
        return new LoanApplicationResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
        if (this.loanProduct == null) {
            this.loanProduct = productCode;
        }
    }

    public String getLoanProduct() {
        return loanProduct;
    }

    public void setLoanProduct(String loanProduct) {
        this.loanProduct = loanProduct;
        if (this.productCode == null) {
            this.productCode = loanProduct;
        }
    }

    public Double getAppliedAmount() {
        return appliedAmount;
    }

    public void setAppliedAmount(Double appliedAmount) {
        this.appliedAmount = appliedAmount;
        if (this.requestedAmount == null) {
            this.requestedAmount = appliedAmount;
        }
    }

    public Double getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(Double requestedAmount) {
        this.requestedAmount = requestedAmount;
        if (this.appliedAmount == null) {
            this.appliedAmount = requestedAmount;
        }
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
        if (this.annualInterestRate == null) {
            this.annualInterestRate = interestRate;
        }
    }

    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
        if (this.interestRate == null) {
            this.interestRate = annualInterestRate;
        }
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
        if (this.loanTermMonths == null) {
            this.loanTermMonths = tenureMonths;
        }
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
        if (this.tenureMonths == null) {
            this.tenureMonths = loanTermMonths;
        }
    }

    public List<IncomeExpenseItemDto> getIncomeExpenses() {
        return incomeExpenses;
    }

    public void setIncomeExpenses(List<IncomeExpenseItemDto> incomeExpenses) {
        this.incomeExpenses = incomeExpenses;
    }

    public Double getTotalMonthlyIncome() {
        return totalMonthlyIncome;
    }

    public void setTotalMonthlyIncome(Double totalMonthlyIncome) {
        this.totalMonthlyIncome = totalMonthlyIncome;
    }

    public Double getTotalMonthlyExpenses() {
        return totalMonthlyExpenses;
    }

    public void setTotalMonthlyExpenses(Double totalMonthlyExpenses) {
        this.totalMonthlyExpenses = totalMonthlyExpenses;
    }

    public Double getNetMonthlyIncome() {
        return netMonthlyIncome;
    }

    public void setNetMonthlyIncome(Double netMonthlyIncome) {
        this.netMonthlyIncome = netMonthlyIncome;
    }

    public Double getExistingMonthlyDebt() {
        return existingMonthlyDebt;
    }

    public void setExistingMonthlyDebt(Double existingMonthlyDebt) {
        this.existingMonthlyDebt = existingMonthlyDebt;
    }

    public Integer getCibilScore() {
        return cibilScore;
    }

    public void setCibilScore(Integer cibilScore) {
        this.cibilScore = cibilScore;
    }

    public Double getCollateralValue() {
        return collateralValue;
    }

    public void setCollateralValue(Double collateralValue) {
        this.collateralValue = collateralValue;
    }

    public Double getEmi() {
        return emi;
    }

    public void setEmi(Double emi) {
        this.emi = emi;
    }

    public Double getDtiRatio() {
        return dtiRatio;
    }

    public void setDtiRatio(Double dtiRatio) {
        this.dtiRatio = dtiRatio;
    }

    public Double getMaxEligibleAmount() {
        return maxEligibleAmount;
    }

    public void setMaxEligibleAmount(Double maxEligibleAmount) {
        this.maxEligibleAmount = maxEligibleAmount;
    }

    public String getEligibilityStatus() {
        return eligibilityStatus;
    }

    public void setEligibilityStatus(String eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public Double getSanctionedAmount() {
        return sanctionedAmount;
    }

    public void setSanctionedAmount(Double sanctionedAmount) {
        this.sanctionedAmount = sanctionedAmount;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public Long getMakerUserId() {
        return makerUserId;
    }

    public void setMakerUserId(Long makerUserId) {
        this.makerUserId = makerUserId;
    }

    public Long getCheckerUserId() {
        return checkerUserId;
    }

    public void setCheckerUserId(Long checkerUserId) {
        this.checkerUserId = checkerUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckerRemarks() {
        return checkerRemarks;
    }

    public void setCheckerRemarks(String checkerRemarks) {
        this.checkerRemarks = checkerRemarks;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class LoanApplicationResponseBuilder {
        private Long id;
        private String applicationNumber;
        private String customerNumber;
        private String applicantName;
        private Long customerId;
        private Long branchId;
        private String productCode;
        private String loanProduct;
        private Double appliedAmount;
        private Double requestedAmount;
        private Double interestRate;
        private Double annualInterestRate;
        private Integer tenureMonths;
        private Integer loanTermMonths;
        private List<IncomeExpenseItemDto> incomeExpenses = new ArrayList<>();
        private Double totalMonthlyIncome;
        private Double totalMonthlyExpenses;
        private Double netMonthlyIncome;
        private Double existingMonthlyDebt;
        private Integer cibilScore;
        private Double collateralValue;
        private Double emi;
        private Double dtiRatio;
        private Double maxEligibleAmount;
        private String eligibilityStatus;
        private Double sanctionedAmount;
        private String kycStatus;
        private Long makerUserId;
        private Long checkerUserId;
        private String status;
        private String checkerRemarks;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        LoanApplicationResponseBuilder() {
        }

        public LoanApplicationResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanApplicationResponseBuilder applicationNumber(String applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public LoanApplicationResponseBuilder customerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
            return this;
        }

        public LoanApplicationResponseBuilder applicantName(String applicantName) {
            this.applicantName = applicantName;
            return this;
        }

        public LoanApplicationResponseBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public LoanApplicationResponseBuilder branchId(Long branchId) {
            this.branchId = branchId;
            return this;
        }

        public LoanApplicationResponseBuilder productCode(String productCode) {
            this.productCode = productCode;
            if (this.loanProduct == null) {
                this.loanProduct = productCode;
            }
            return this;
        }

        public LoanApplicationResponseBuilder loanProduct(String loanProduct) {
            this.loanProduct = loanProduct;
            if (this.productCode == null) {
                this.productCode = loanProduct;
            }
            return this;
        }

        public LoanApplicationResponseBuilder appliedAmount(Double appliedAmount) {
            this.appliedAmount = appliedAmount;
            if (this.requestedAmount == null) {
                this.requestedAmount = appliedAmount;
            }
            return this;
        }

        public LoanApplicationResponseBuilder requestedAmount(Double requestedAmount) {
            this.requestedAmount = requestedAmount;
            if (this.appliedAmount == null) {
                this.appliedAmount = requestedAmount;
            }
            return this;
        }

        public LoanApplicationResponseBuilder interestRate(Double interestRate) {
            this.interestRate = interestRate;
            if (this.annualInterestRate == null) {
                this.annualInterestRate = interestRate;
            }
            return this;
        }

        public LoanApplicationResponseBuilder annualInterestRate(Double annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
            if (this.interestRate == null) {
                this.interestRate = annualInterestRate;
            }
            return this;
        }

        public LoanApplicationResponseBuilder tenureMonths(Integer tenureMonths) {
            this.tenureMonths = tenureMonths;
            if (this.loanTermMonths == null) {
                this.loanTermMonths = tenureMonths;
            }
            return this;
        }

        public LoanApplicationResponseBuilder loanTermMonths(Integer loanTermMonths) {
            this.loanTermMonths = loanTermMonths;
            if (this.tenureMonths == null) {
                this.tenureMonths = loanTermMonths;
            }
            return this;
        }

        public LoanApplicationResponseBuilder incomeExpenses(List<IncomeExpenseItemDto> incomeExpenses) {
            this.incomeExpenses = incomeExpenses;
            return this;
        }

        public LoanApplicationResponseBuilder totalMonthlyIncome(Double totalMonthlyIncome) {
            this.totalMonthlyIncome = totalMonthlyIncome;
            return this;
        }

        public LoanApplicationResponseBuilder totalMonthlyExpenses(Double totalMonthlyExpenses) {
            this.totalMonthlyExpenses = totalMonthlyExpenses;
            return this;
        }

        public LoanApplicationResponseBuilder netMonthlyIncome(Double netMonthlyIncome) {
            this.netMonthlyIncome = netMonthlyIncome;
            return this;
        }

        public LoanApplicationResponseBuilder existingMonthlyDebt(Double existingMonthlyDebt) {
            this.existingMonthlyDebt = existingMonthlyDebt;
            return this;
        }

        public LoanApplicationResponseBuilder cibilScore(Integer cibilScore) {
            this.cibilScore = cibilScore;
            return this;
        }

        public LoanApplicationResponseBuilder collateralValue(Double collateralValue) {
            this.collateralValue = collateralValue;
            return this;
        }

        public LoanApplicationResponseBuilder emi(Double emi) {
            this.emi = emi;
            return this;
        }

        public LoanApplicationResponseBuilder dtiRatio(Double dtiRatio) {
            this.dtiRatio = dtiRatio;
            return this;
        }

        public LoanApplicationResponseBuilder maxEligibleAmount(Double maxEligibleAmount) {
            this.maxEligibleAmount = maxEligibleAmount;
            return this;
        }

        public LoanApplicationResponseBuilder eligibilityStatus(String eligibilityStatus) {
            this.eligibilityStatus = eligibilityStatus;
            return this;
        }

        public LoanApplicationResponseBuilder sanctionedAmount(Double sanctionedAmount) {
            this.sanctionedAmount = sanctionedAmount;
            return this;
        }

        public LoanApplicationResponseBuilder kycStatus(String kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }

        public LoanApplicationResponseBuilder makerUserId(Long makerUserId) {
            this.makerUserId = makerUserId;
            return this;
        }

        public LoanApplicationResponseBuilder checkerUserId(Long checkerUserId) {
            this.checkerUserId = checkerUserId;
            return this;
        }

        public LoanApplicationResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LoanApplicationResponseBuilder checkerRemarks(String checkerRemarks) {
            this.checkerRemarks = checkerRemarks;
            return this;
        }

        public LoanApplicationResponseBuilder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public LoanApplicationResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LoanApplicationResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public LoanApplicationResponse build() {
            return new LoanApplicationResponse(id, applicationNumber, customerNumber, applicantName, customerId,
                    branchId, productCode, loanProduct, appliedAmount, requestedAmount, interestRate,
                    annualInterestRate, tenureMonths, loanTermMonths, incomeExpenses, totalMonthlyIncome,
                    totalMonthlyExpenses, netMonthlyIncome, existingMonthlyDebt, cibilScore, collateralValue, emi,
                    dtiRatio, maxEligibleAmount, eligibilityStatus, sanctionedAmount, kycStatus, makerUserId,
                    checkerUserId, status, checkerRemarks, approvedAt, createdAt, updatedAt);
        }
    }
}
