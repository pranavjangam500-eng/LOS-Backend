package com.bank.los.bank.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response representing a loan product")
public class LoanProductResponse {
    private Long id;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanProductResponse() {
    }

    public LoanProductResponse(Long id, String code, String name, String description, Double emi, String emiType,
                               Double minAmount, Double maxAmount, Double interestRatePercent,
                               Integer minTenureMonths, Integer maxTenureMonths, Double penalty,
                               Double penaltyPercent, String interestCalculationFrequency,
                               String interestCalculationMode, Double processingFeePercent, String status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static LoanProductResponseBuilder builder() {
        return new LoanProductResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public static class LoanProductResponseBuilder {
        private Long id;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        LoanProductResponseBuilder() {
        }

        public LoanProductResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanProductResponseBuilder code(String code) {
            this.code = code;
            return this;
        }

        public LoanProductResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LoanProductResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LoanProductResponseBuilder emi(Double emi) {
            this.emi = emi;
            return this;
        }

        public LoanProductResponseBuilder emiType(String emiType) {
            this.emiType = emiType;
            return this;
        }

        public LoanProductResponseBuilder minAmount(Double minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public LoanProductResponseBuilder maxAmount(Double maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public LoanProductResponseBuilder interestRatePercent(Double interestRatePercent) {
            this.interestRatePercent = interestRatePercent;
            return this;
        }

        public LoanProductResponseBuilder minTenureMonths(Integer minTenureMonths) {
            this.minTenureMonths = minTenureMonths;
            return this;
        }

        public LoanProductResponseBuilder maxTenureMonths(Integer maxTenureMonths) {
            this.maxTenureMonths = maxTenureMonths;
            return this;
        }

        public LoanProductResponseBuilder penalty(Double penalty) {
            this.penalty = penalty;
            return this;
        }

        public LoanProductResponseBuilder penaltyPercent(Double penaltyPercent) {
            this.penaltyPercent = penaltyPercent;
            return this;
        }

        public LoanProductResponseBuilder interestCalculationFrequency(String interestCalculationFrequency) {
            this.interestCalculationFrequency = interestCalculationFrequency;
            return this;
        }

        public LoanProductResponseBuilder interestCalculationMode(String interestCalculationMode) {
            this.interestCalculationMode = interestCalculationMode;
            return this;
        }

        public LoanProductResponseBuilder processingFeePercent(Double processingFeePercent) {
            this.processingFeePercent = processingFeePercent;
            return this;
        }

        public LoanProductResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LoanProductResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LoanProductResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public LoanProductResponse build() {
            return new LoanProductResponse(id, code, name, description, emi, emiType, minAmount, maxAmount,
                    interestRatePercent, minTenureMonths, maxTenureMonths, penalty, penaltyPercent,
                    interestCalculationFrequency, interestCalculationMode, processingFeePercent, status,
                    createdAt, updatedAt);
        }
    }
}
