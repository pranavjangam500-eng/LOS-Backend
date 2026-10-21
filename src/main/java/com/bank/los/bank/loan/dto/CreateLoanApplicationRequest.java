package com.bank.los.bank.loan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Request body to assess loan eligibility and create/update loan application")
public class CreateLoanApplicationRequest {

    // =========================================================================
    // SECTION 1: Applicant & Loan Request
    // =========================================================================

    @JsonProperty("customerNumber")
    @JsonAlias({"customerNumber", "customer_number", "Customer Number", "custNo"})
    private String customerNumber;

    @JsonProperty("applicantName")
    @JsonAlias({"applicantName", "applicant_name", "Applicant Name", "customerName", "name"})
    private String applicantName;

    @JsonProperty("customerId")
    @JsonAlias({"customerId", "customer_id", "Customer ID"})
    private Long customerId;

    @JsonProperty("branchId")
    @JsonAlias({"branchId", "branch_id", "Branch ID"})
    private Long branchId;

    @JsonProperty("loanProduct")
    @JsonAlias({"loanProduct", "loan_product", "Loan Product", "productCode", "product_code"})
    private String loanProduct;

    @JsonProperty("requestedAmount")
    @JsonAlias({"requestedAmount", "requested_amount", "Requested Amount", "appliedAmount", "applied_amount", "loanAmount"})
    @DecimalMin(value = "0.01", message = "Requested amount must be greater than zero")
    private Double requestedAmount;

    @JsonProperty("annualInterestRate")
    @JsonAlias({"annualInterestRate", "annual_interest_rate", "Annual Interest Rate", "interestRate", "interest_rate"})
    @DecimalMin(value = "0.0", message = "Annual interest rate must be non-negative")
    private Double annualInterestRate = 12.0;

    @JsonProperty("loanTermMonths")
    @JsonAlias({"loanTermMonths", "loan_term_months", "Loan Term in Months", "tenureMonths", "tenure_months", "tenure"})
    @Min(value = 1, message = "Loan term in months must be at least 1")
    private Integer loanTermMonths = 60;

    // =========================================================================
    // SECTION 2: Monthly Income & Expenses (Repeatable Rows)
    // =========================================================================

    @Valid
    @JsonProperty("incomeExpenses")
    @JsonAlias({"incomeExpenses", "income_expenses", "monthlyCashflows", "cashflows", "items"})
    private List<IncomeExpenseItemDto> incomeExpenses = new ArrayList<>();

    // Optional manual summary overrides if provided directly
    @JsonProperty("totalMonthlyIncome")
    @JsonAlias({"totalMonthlyIncome", "total_monthly_income", "Total Monthly Income"})
    private Double totalMonthlyIncome;

    @JsonProperty("totalMonthlyExpenses")
    @JsonAlias({"totalMonthlyExpenses", "total_monthly_expenses", "Total Monthly Expenses"})
    private Double totalMonthlyExpenses;

    // =========================================================================
    // SECTION 3: Obligations & Collateral
    // =========================================================================

    @JsonProperty("existingMonthlyDebt")
    @JsonAlias({"existingMonthlyDebt", "existing_monthly_debt", "Existing Monthly Debt", "monthlyDebt", "existingDebt"})
    @DecimalMin(value = "0.0", message = "Existing monthly debt must be non-negative")
    private Double existingMonthlyDebt = 0.0;

    @JsonProperty("cibilScore")
    @JsonAlias({"cibilScore", "cibil_score", "CIBIL Score", "creditScore"})
    @Min(value = 300, message = "CIBIL score must be at least 300")
    @Max(value = 900, message = "CIBIL score cannot exceed 900")
    private Integer cibilScore = 750;

    @JsonProperty("collateralValue")
    @JsonAlias({"collateralValue", "collateral_value", "Collateral Value", "securityAmount"})
    @DecimalMin(value = "0.0", message = "Collateral value must be non-negative")
    private Double collateralValue;

    public CreateLoanApplicationRequest() {
    }

    public CreateLoanApplicationRequest(String customerNumber, String applicantName, Long customerId, Long branchId,
                                        String loanProduct, Double requestedAmount, Double annualInterestRate,
                                        Integer loanTermMonths, List<IncomeExpenseItemDto> incomeExpenses,
                                        Double totalMonthlyIncome, Double totalMonthlyExpenses,
                                        Double existingMonthlyDebt, Integer cibilScore, Double collateralValue) {
        this.customerNumber = customerNumber;
        this.applicantName = applicantName;
        this.customerId = customerId;
        this.branchId = branchId;
        this.loanProduct = loanProduct;
        this.requestedAmount = requestedAmount;
        this.annualInterestRate = annualInterestRate != null ? annualInterestRate : 12.0;
        this.loanTermMonths = loanTermMonths != null ? loanTermMonths : 60;
        this.incomeExpenses = incomeExpenses != null ? incomeExpenses : new ArrayList<>();
        this.totalMonthlyIncome = totalMonthlyIncome;
        this.totalMonthlyExpenses = totalMonthlyExpenses;
        this.existingMonthlyDebt = existingMonthlyDebt != null ? existingMonthlyDebt : 0.0;
        this.cibilScore = cibilScore != null ? cibilScore : 750;
        this.collateralValue = collateralValue;
    }

    public static CreateLoanApplicationRequestBuilder builder() {
        return new CreateLoanApplicationRequestBuilder();
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

    public String getLoanProduct() {
        return loanProduct;
    }

    public void setLoanProduct(String loanProduct) {
        this.loanProduct = loanProduct;
    }

    public Double getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(Double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
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

    // Backward compatibility helper getters/setters
    public String getProductCode() {
        return loanProduct;
    }

    public void setProductCode(String productCode) {
        this.loanProduct = productCode;
    }

    public Double getAppliedAmount() {
        return requestedAmount;
    }

    public void setAppliedAmount(Double appliedAmount) {
        this.requestedAmount = appliedAmount;
    }

    public Integer getTenureMonths() {
        return loanTermMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.loanTermMonths = tenureMonths;
    }

    public Double getInterestRate() {
        return annualInterestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.annualInterestRate = interestRate;
    }

    public static class CreateLoanApplicationRequestBuilder {
        private String customerNumber;
        private String applicantName;
        private Long customerId;
        private Long branchId;
        private String loanProduct;
        private Double requestedAmount;
        private Double annualInterestRate = 12.0;
        private Integer loanTermMonths = 60;
        private List<IncomeExpenseItemDto> incomeExpenses = new ArrayList<>();
        private Double totalMonthlyIncome;
        private Double totalMonthlyExpenses;
        private Double existingMonthlyDebt = 0.0;
        private Integer cibilScore = 750;
        private Double collateralValue;

        CreateLoanApplicationRequestBuilder() {
        }

        public CreateLoanApplicationRequestBuilder customerNumber(String customerNumber) {
            this.customerNumber = customerNumber;
            return this;
        }

        public CreateLoanApplicationRequestBuilder applicantName(String applicantName) {
            this.applicantName = applicantName;
            return this;
        }

        public CreateLoanApplicationRequestBuilder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public CreateLoanApplicationRequestBuilder branchId(Long branchId) {
            this.branchId = branchId;
            return this;
        }

        public CreateLoanApplicationRequestBuilder loanProduct(String loanProduct) {
            this.loanProduct = loanProduct;
            return this;
        }

        public CreateLoanApplicationRequestBuilder productCode(String productCode) {
            this.loanProduct = productCode;
            return this;
        }

        public CreateLoanApplicationRequestBuilder requestedAmount(Double requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public CreateLoanApplicationRequestBuilder appliedAmount(Double appliedAmount) {
            this.requestedAmount = appliedAmount;
            return this;
        }

        public CreateLoanApplicationRequestBuilder annualInterestRate(Double annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
            return this;
        }

        public CreateLoanApplicationRequestBuilder interestRate(Double interestRate) {
            this.annualInterestRate = interestRate;
            return this;
        }

        public CreateLoanApplicationRequestBuilder loanTermMonths(Integer loanTermMonths) {
            this.loanTermMonths = loanTermMonths;
            return this;
        }

        public CreateLoanApplicationRequestBuilder tenureMonths(Integer tenureMonths) {
            this.loanTermMonths = tenureMonths;
            return this;
        }

        public CreateLoanApplicationRequestBuilder incomeExpenses(List<IncomeExpenseItemDto> incomeExpenses) {
            this.incomeExpenses = incomeExpenses;
            return this;
        }

        public CreateLoanApplicationRequestBuilder totalMonthlyIncome(Double totalMonthlyIncome) {
            this.totalMonthlyIncome = totalMonthlyIncome;
            return this;
        }

        public CreateLoanApplicationRequestBuilder totalMonthlyExpenses(Double totalMonthlyExpenses) {
            this.totalMonthlyExpenses = totalMonthlyExpenses;
            return this;
        }

        public CreateLoanApplicationRequestBuilder existingMonthlyDebt(Double existingMonthlyDebt) {
            this.existingMonthlyDebt = existingMonthlyDebt;
            return this;
        }

        public CreateLoanApplicationRequestBuilder cibilScore(Integer cibilScore) {
            this.cibilScore = cibilScore;
            return this;
        }

        public CreateLoanApplicationRequestBuilder collateralValue(Double collateralValue) {
            this.collateralValue = collateralValue;
            return this;
        }

        public CreateLoanApplicationRequest build() {
            return new CreateLoanApplicationRequest(customerNumber, applicantName, customerId, branchId,
                    loanProduct, requestedAmount, annualInterestRate, loanTermMonths, incomeExpenses,
                    totalMonthlyIncome, totalMonthlyExpenses, existingMonthlyDebt, cibilScore, collateralValue);
        }
    }
}
