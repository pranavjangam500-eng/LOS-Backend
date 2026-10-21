package com.bank.los.bank.loan.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loan_applications", schema = "customer")
@EqualsAndHashCode(callSuper = true)
public class LoanApplication extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_number", unique = true, length = 40)
    private String applicationNumber;

    // =========================================================================
    // SECTION 1: Applicant & Loan Request
    // =========================================================================

    @Column(name = "customer_number", length = 50)
    private String customerNumber;

    @Column(name = "applicant_name", length = 150)
    private String applicantName;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode; // e.g. HOME_LOAN, PERSONAL_LOAN

    @Column(name = "applied_amount", nullable = false)
    private Double appliedAmount; // Requested Amount

    @Column(name = "interest_rate")
    private Double interestRate = 12.0; // Annual Interest Rate (default 12%)

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths = 60; // Loan Term in Months (default 60)

    // =========================================================================
    // SECTION 2: Monthly Income & Expenses (Repeatable Rows)
    // =========================================================================

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_income_expenses",
            schema = "customer",
            joinColumns = @JoinColumn(name = "loan_application_id")
    )
    private List<IncomeExpenseItem> incomeExpenses = new ArrayList<>();

    @Column(name = "total_monthly_income")
    private Double totalMonthlyIncome = 0.0;

    @Column(name = "total_monthly_expenses")
    private Double totalMonthlyExpenses = 0.0;

    @Column(name = "net_monthly_income")
    private Double netMonthlyIncome = 0.0;

    // =========================================================================
    // SECTION 3: Obligations & Collateral
    // =========================================================================

    @Column(name = "existing_monthly_debt")
    private Double existingMonthlyDebt = 0.0;

    @Column(name = "cibil_score")
    private Integer cibilScore = 750; // 300 - 900

    @Column(name = "collateral_value")
    private Double collateralValue; // Optional Collateral Value

    // =========================================================================
    // ELIGIBILITY ASSESSMENT & CALCULATIONS
    // =========================================================================

    @Column(name = "emi")
    private Double emi;

    @Column(name = "dti_ratio")
    private Double dtiRatio; // Debt-to-Income / FOIR %

    @Column(name = "max_eligible_amount")
    private Double maxEligibleAmount;

    @Column(name = "eligibility_status", length = 40)
    private String eligibilityStatus = "ELIGIBLE"; // ELIGIBLE, REFERRED, REJECTED

    @Column(name = "sanctioned_amount")
    private Double sanctionedAmount;

    // =========================================================================
    // WORKFLOW & AUDIT
    // =========================================================================

    @Column(name = "kyc_status", length = 30)
    private String kycStatus = "VERIFIED";

    @Column(name = "maker_user_id")
    private Long makerUserId;

    @Column(name = "checker_user_id")
    private Long checkerUserId;

    @Column(nullable = false, length = 40)
    private String status = "SUBMITTED_TO_CHECKER"; // DRAFT, SUBMITTED_TO_CHECKER, APPROVED, REJECTED, DISBURSED

    @Column(name = "checker_remarks", length = 500)
    private String checkerRemarks;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public LoanApplication() {
    }

    public LoanApplication(Long id, String applicationNumber, String customerNumber, String applicantName,
                           Long customerId, Long branchId, String productCode, Double appliedAmount,
                           Double interestRate, Integer tenureMonths, List<IncomeExpenseItem> incomeExpenses,
                           Double totalMonthlyIncome, Double totalMonthlyExpenses, Double netMonthlyIncome,
                           Double existingMonthlyDebt, Integer cibilScore, Double collateralValue, Double emi,
                           Double dtiRatio, Double maxEligibleAmount, String eligibilityStatus,
                           Double sanctionedAmount, String kycStatus, Long makerUserId, Long checkerUserId,
                           String status, String checkerRemarks, LocalDateTime approvedAt) {
        this.id = id;
        this.applicationNumber = applicationNumber;
        this.customerNumber = customerNumber;
        this.applicantName = applicantName;
        this.customerId = customerId;
        this.branchId = branchId;
        this.productCode = productCode;
        this.appliedAmount = appliedAmount;
        this.interestRate = interestRate != null ? interestRate : 12.0;
        this.tenureMonths = tenureMonths != null ? tenureMonths : 60;
        this.incomeExpenses = incomeExpenses != null ? incomeExpenses : new ArrayList<>();
        this.totalMonthlyIncome = totalMonthlyIncome != null ? totalMonthlyIncome : 0.0;
        this.totalMonthlyExpenses = totalMonthlyExpenses != null ? totalMonthlyExpenses : 0.0;
        this.netMonthlyIncome = netMonthlyIncome != null ? netMonthlyIncome : 0.0;
        this.existingMonthlyDebt = existingMonthlyDebt != null ? existingMonthlyDebt : 0.0;
        this.cibilScore = cibilScore != null ? cibilScore : 750;
        this.collateralValue = collateralValue;
        this.emi = emi;
        this.dtiRatio = dtiRatio;
        this.maxEligibleAmount = maxEligibleAmount;
        this.eligibilityStatus = eligibilityStatus != null ? eligibilityStatus : "ELIGIBLE";
        this.sanctionedAmount = sanctionedAmount;
        this.kycStatus = kycStatus != null ? kycStatus : "VERIFIED";
        this.makerUserId = makerUserId;
        this.checkerUserId = checkerUserId;
        this.status = status != null ? status : "SUBMITTED_TO_CHECKER";
        this.checkerRemarks = checkerRemarks;
        this.approvedAt = approvedAt;
    }

    public static LoanApplicationBuilder builder() {
        return new LoanApplicationBuilder();
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
    }

    public Double getAppliedAmount() {
        return appliedAmount;
    }

    public void setAppliedAmount(Double appliedAmount) {
        this.appliedAmount = appliedAmount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public List<IncomeExpenseItem> getIncomeExpenses() {
        return incomeExpenses;
    }

    public void setIncomeExpenses(List<IncomeExpenseItem> incomeExpenses) {
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

    // Compatibility aliases
    public Double getRequestedAmount() {
        return appliedAmount;
    }

    public void setRequestedAmount(Double requestedAmount) {
        this.appliedAmount = requestedAmount;
    }

    public String getLoanProduct() {
        return productCode;
    }

    public void setLoanProduct(String loanProduct) {
        this.productCode = loanProduct;
    }

    public static class LoanApplicationBuilder {
        private Long id;
        private String applicationNumber;
        private String customerNumber;
        private String applicantName;
        private Long customerId;
        private Long branchId;
        private String productCode;
        private Double appliedAmount;
        private Double interestRate = 12.0;
        private Integer tenureMonths = 60;
        private List<IncomeExpenseItem> incomeExpenses = new ArrayList<>();
        private Double totalMonthlyIncome = 0.0;
        private Double totalMonthlyExpenses = 0.0;
        private Double netMonthlyIncome = 0.0;
        private Double existingMonthlyDebt = 0.0;
        private Integer cibilScore = 750;
        private Double collateralValue;
        private Double emi;
        private Double dtiRatio;
        private Double maxEligibleAmount;
        private String eligibilityStatus = "ELIGIBLE";
        private Double sanctionedAmount;
        private String kycStatus = "VERIFIED";
        private Long makerUserId;
        private Long checkerUserId;
        private String status = "SUBMITTED_TO_CHECKER";
        private String checkerRemarks;
        private LocalDateTime approvedAt;

        LoanApplicationBuilder() {
        }

        public LoanApplicationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanApplicationBuilder applicationNumber(String applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public LoanApplicationBuilder customerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
            return this;
        }

        public LoanApplicationBuilder applicantName(String applicantName) {
            this.applicantName = applicantName;
            return this;
        }

        public LoanApplicationBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public LoanApplicationBuilder branchId(Long branchId) {
            this.branchId = branchId;
            return this;
        }

        public LoanApplicationBuilder productCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public LoanApplicationBuilder loanProduct(String loanProduct) {
            this.productCode = loanProduct;
            return this;
        }

        public LoanApplicationBuilder appliedAmount(Double appliedAmount) {
            this.appliedAmount = appliedAmount;
            return this;
        }

        public LoanApplicationBuilder requestedAmount(Double requestedAmount) {
            this.appliedAmount = requestedAmount;
            return this;
        }

        public LoanApplicationBuilder interestRate(Double interestRate) {
            this.interestRate = interestRate;
            return this;
        }

        public LoanApplicationBuilder tenureMonths(Integer tenureMonths) {
            this.tenureMonths = tenureMonths;
            return this;
        }

        public LoanApplicationBuilder incomeExpenses(List<IncomeExpenseItem> incomeExpenses) {
            this.incomeExpenses = incomeExpenses;
            return this;
        }

        public LoanApplicationBuilder totalMonthlyIncome(Double totalMonthlyIncome) {
            this.totalMonthlyIncome = totalMonthlyIncome;
            return this;
        }

        public LoanApplicationBuilder totalMonthlyExpenses(Double totalMonthlyExpenses) {
            this.totalMonthlyExpenses = totalMonthlyExpenses;
            return this;
        }

        public LoanApplicationBuilder netMonthlyIncome(Double netMonthlyIncome) {
            this.netMonthlyIncome = netMonthlyIncome;
            return this;
        }

        public LoanApplicationBuilder existingMonthlyDebt(Double existingMonthlyDebt) {
            this.existingMonthlyDebt = existingMonthlyDebt;
            return this;
        }

        public LoanApplicationBuilder cibilScore(Integer cibilScore) {
            this.cibilScore = cibilScore;
            return this;
        }

        public LoanApplicationBuilder collateralValue(Double collateralValue) {
            this.collateralValue = collateralValue;
            return this;
        }

        public LoanApplicationBuilder emi(Double emi) {
            this.emi = emi;
            return this;
        }

        public LoanApplicationBuilder dtiRatio(Double dtiRatio) {
            this.dtiRatio = dtiRatio;
            return this;
        }

        public LoanApplicationBuilder maxEligibleAmount(Double maxEligibleAmount) {
            this.maxEligibleAmount = maxEligibleAmount;
            return this;
        }

        public LoanApplicationBuilder eligibilityStatus(String eligibilityStatus) {
            this.eligibilityStatus = eligibilityStatus;
            return this;
        }

        public LoanApplicationBuilder sanctionedAmount(Double sanctionedAmount) {
            this.sanctionedAmount = sanctionedAmount;
            return this;
        }

        public LoanApplicationBuilder kycStatus(String kycStatus) {
            this.kycStatus = kycStatus;
            return this;
        }

        public LoanApplicationBuilder makerUserId(Long makerUserId) {
            this.makerUserId = makerUserId;
            return this;
        }

        public LoanApplicationBuilder checkerUserId(Long checkerUserId) {
            this.checkerUserId = checkerUserId;
            return this;
        }

        public LoanApplicationBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LoanApplicationBuilder checkerRemarks(String checkerRemarks) {
            this.checkerRemarks = checkerRemarks;
            return this;
        }

        public LoanApplicationBuilder approvedAt(LocalDateTime approvedAt) {
            this.approvedAt = approvedAt;
            return this;
        }

        public LoanApplication build() {
            return new LoanApplication(id, applicationNumber, customerNumber, applicantName, customerId, branchId,
                    productCode, appliedAmount, interestRate, tenureMonths, incomeExpenses, totalMonthlyIncome,
                    totalMonthlyExpenses, netMonthlyIncome, existingMonthlyDebt, cibilScore, collateralValue, emi,
                    dtiRatio, maxEligibleAmount, eligibilityStatus, sanctionedAmount, kycStatus, makerUserId,
                    checkerUserId, status, checkerRemarks, approvedAt);
        }
    }
}
