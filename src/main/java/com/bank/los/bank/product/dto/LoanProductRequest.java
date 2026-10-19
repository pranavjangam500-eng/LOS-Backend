package com.bank.los.bank.product.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to configure or update a loan product")
public class LoanProductRequest {

    @NotBlank(message = "Product code is required")
    @JsonProperty("code")
    @JsonAlias({"code", "productCode", "Product Code", "loanProduct"})
    private String code;

    @NotBlank(message = "Product name is required")
    @JsonProperty("name")
    @JsonAlias({"name", "productName", "Product Name"})
    private String name;

    private String description;

    @JsonProperty("emi")
    @JsonAlias({"emi", "EMI"})
    private Double emi;

    @JsonProperty("emiType")
    @JsonAlias({"emiType", "emi_type", "EMI Type"})
    private String emiType;

    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.0", message = "Minimum amount must be non-negative")
    @JsonProperty("minAmount")
    @JsonAlias({"minAmount", "min_amount", "Min Amount"})
    private Double minAmount;

    @NotNull(message = "Maximum amount is required")
    @DecimalMin(value = "0.0", message = "Maximum amount must be non-negative")
    @JsonProperty("maxAmount")
    @JsonAlias({"maxAmount", "max_amount", "Max Amount"})
    private Double maxAmount;

    @NotNull(message = "Interest rate is required")
    @JsonProperty("interestRatePercent")
    @JsonAlias({"interestRatePercent", "interestRate", "interest_rate_percent", "Interest Rate"})
    private Double interestRatePercent;

    @NotNull(message = "Minimum tenure is required")
    @Min(value = 1, message = "Minimum tenure must be at least 1 month")
    @JsonProperty("minTenureMonths")
    @JsonAlias({"minTenureMonths", "minTenure", "min_tenure_months", "Min Tenure"})
    private Integer minTenureMonths;

    @NotNull(message = "Maximum tenure is required")
    @Min(value = 1, message = "Maximum tenure must be at least 1 month")
    @JsonProperty("maxTenureMonths")
    @JsonAlias({"maxTenureMonths", "maxTenure", "max_tenure_months", "Max Tenure"})
    private Integer maxTenureMonths;

    @JsonProperty("penalty")
    @JsonAlias({"penalty", "Penalty"})
    private Double penalty;

    @JsonProperty("penaltyPercent")
    @JsonAlias({"penaltyPercent", "penalty_percent", "Penalty %", "penaltyPercentage"})
    private Double penaltyPercent;

    @JsonProperty("interestCalculationFrequency")
    @JsonAlias({"interestCalculationFrequency", "interest_calculation_frequency", "Interest Calculation Frequency"})
    private String interestCalculationFrequency;

    @JsonProperty("interestCalculationMode")
    @JsonAlias({"interestCalculationMode", "interest_calculation_mode", "Interest Calculation Mode"})
    private String interestCalculationMode;

    @JsonProperty("processingFeePercent")
    @JsonAlias({"processingFeePercent", "processing_fee_percent", "Processing Fee %"})
    private Double processingFeePercent;

    private String status;

    public LoanProductRequest() {
    }

    public LoanProductRequest(String code, String name, String description, Double emi, String emiType,
                              Double minAmount, Double maxAmount, Double interestRatePercent,
                              Integer minTenureMonths, Integer maxTenureMonths, Double penalty,
                              Double penaltyPercent, String interestCalculationFrequency,
                              String interestCalculationMode, Double processingFeePercent, String status) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.emi = emi;
        this.emiType = emiType;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.interestRatePercent = interestRatePercent;
        this.minTenureMonths = minTenureMonths;
        this.maxTenureMonths = maxTenureMonths;
        this.penalty = penalty;
        this.penaltyPercent = penaltyPercent;
        this.interestCalculationFrequency = interestCalculationFrequency;
        this.interestCalculationMode = interestCalculationMode;
        this.processingFeePercent = processingFeePercent;
        this.status = status;
    }

    public static LoanProductRequestBuilder builder() {
        return new LoanProductRequestBuilder();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getEmi() {
        return emi;
    }

    public void setEmi(Double emi) {
        this.emi = emi;
    }

    public String getEmiType() {
        return emiType;
    }

    public void setEmiType(String emiType) {
        this.emiType = emiType;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Double getInterestRatePercent() {
        return interestRatePercent;
    }

    public void setInterestRatePercent(Double interestRatePercent) {
        this.interestRatePercent = interestRatePercent;
    }

    public Integer getMinTenureMonths() {
        return minTenureMonths;
    }

    public void setMinTenureMonths(Integer minTenureMonths) {
        this.minTenureMonths = minTenureMonths;
    }

    public Integer getMaxTenureMonths() {
        return maxTenureMonths;
    }

    public void setMaxTenureMonths(Integer maxTenureMonths) {
        this.maxTenureMonths = maxTenureMonths;
    }

    public Double getPenalty() {
        return penalty;
    }

    public void setPenalty(Double penalty) {
        this.penalty = penalty;
    }

    public Double getPenaltyPercent() {
        return penaltyPercent;
    }

    public void setPenaltyPercent(Double penaltyPercent) {
        this.penaltyPercent = penaltyPercent;
    }

    public String getInterestCalculationFrequency() {
        return interestCalculationFrequency;
    }

    public void setInterestCalculationFrequency(String interestCalculationFrequency) {
        this.interestCalculationFrequency = interestCalculationFrequency;
    }

    public String getInterestCalculationMode() {
        return interestCalculationMode;
    }

    public void setInterestCalculationMode(String interestCalculationMode) {
        this.interestCalculationMode = interestCalculationMode;
    }

    public Double getProcessingFeePercent() {
        return processingFeePercent;
    }

    public void setProcessingFeePercent(Double processingFeePercent) {
        this.processingFeePercent = processingFeePercent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class LoanProductRequestBuilder {
        private String code;
        private String name;
        private String description;
        private Double emi;
        private String emiType;
        private Double minAmount;
        private Double maxAmount;
        private Double interestRatePercent;
        private Integer minTenureMonths;
        private Integer maxTenureMonths;
        private Double penalty;
        private Double penaltyPercent;
        private String interestCalculationFrequency;
        private String interestCalculationMode;
        private Double processingFeePercent;
        private String status;

        LoanProductRequestBuilder() {
        }

        public LoanProductRequestBuilder code(String code) {
            this.code = code;
            return this;
        }

        public LoanProductRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LoanProductRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LoanProductRequestBuilder emi(Double emi) {
            this.emi = emi;
            return this;
        }

        public LoanProductRequestBuilder emiType(String emiType) {
            this.emiType = emiType;
            return this;
        }

        public LoanProductRequestBuilder minAmount(Double minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public LoanProductRequestBuilder maxAmount(Double maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public LoanProductRequestBuilder interestRatePercent(Double interestRatePercent) {
            this.interestRatePercent = interestRatePercent;
            return this;
        }

        public LoanProductRequestBuilder minTenureMonths(Integer minTenureMonths) {
            this.minTenureMonths = minTenureMonths;
            return this;
        }

        public LoanProductRequestBuilder maxTenureMonths(Integer maxTenureMonths) {
            this.maxTenureMonths = maxTenureMonths;
            return this;
        }

        public LoanProductRequestBuilder penalty(Double penalty) {
            this.penalty = penalty;
            return this;
        }

        public LoanProductRequestBuilder penaltyPercent(Double penaltyPercent) {
            this.penaltyPercent = penaltyPercent;
            return this;
        }

        public LoanProductRequestBuilder interestCalculationFrequency(String interestCalculationFrequency) {
            this.interestCalculationFrequency = interestCalculationFrequency;
            return this;
        }

        public LoanProductRequestBuilder interestCalculationMode(String interestCalculationMode) {
            this.interestCalculationMode = interestCalculationMode;
            return this;
        }

        public LoanProductRequestBuilder processingFeePercent(Double processingFeePercent) {
            this.processingFeePercent = processingFeePercent;
            return this;
        }

        public LoanProductRequestBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LoanProductRequest build() {
            return new LoanProductRequest(code, name, description, emi, emiType, minAmount, maxAmount,
                    interestRatePercent, minTenureMonths, maxTenureMonths, penalty, penaltyPercent,
                    interestCalculationFrequency, interestCalculationMode, processingFeePercent, status);
        }
    }
}
