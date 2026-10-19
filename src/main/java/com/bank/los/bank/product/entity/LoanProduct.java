package com.bank.los.bank.product.entity;

import com.bank.los.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "loan_products", schema = "customer")
@EqualsAndHashCode(callSuper = true)
public class LoanProduct extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // HOME_LOAN, PERSONAL_LOAN, AUTO_LOAN, etc.

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "emi")
    private Double emi;

    @Column(name = "emi_type", length = 50)
    private String emiType; // REDUCING_BALANCE, FLAT_RATE, etc.

    @Column(name = "min_amount", nullable = false)
    private Double minAmount;

    @Column(name = "max_amount", nullable = false)
    private Double maxAmount;

    @Column(name = "interest_rate_percent", nullable = false)
    private Double interestRatePercent;

    @Column(name = "min_tenure_months", nullable = false)
    private Integer minTenureMonths;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths;

    @Column(name = "penalty")
    private Double penalty;

    @Column(name = "penalty_percent")
    private Double penaltyPercent;

    @Column(name = "interest_calculation_frequency", length = 50)
    private String interestCalculationFrequency; // MONTHLY, DAILY, ANNUALLY, QUARTERLY

    @Column(name = "interest_calculation_mode", length = 50)
    private String interestCalculationMode; // REDUCING, FLAT, SIMPLE, COMPOUND

    @Column(name = "processing_fee_percent")
    private Double processingFeePercent = 1.0;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public LoanProduct() {
    }

    public LoanProduct(Long id, String code, String name, String description, Double emi, String emiType,
                       Double minAmount, Double maxAmount, Double interestRatePercent,
                       Integer minTenureMonths, Integer maxTenureMonths, Double penalty,
                       Double penaltyPercent, String interestCalculationFrequency,
                       String interestCalculationMode, Double processingFeePercent, String status) {
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
        this.processingFeePercent = processingFeePercent != null ? processingFeePercent : 1.0;
        this.status = status != null ? status : "ACTIVE";
    }

    public static LoanProductBuilder builder() {
        return new LoanProductBuilder();
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

    public static class LoanProductBuilder {
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
        private Double processingFeePercent = 1.0;
        private String status = "ACTIVE";

        LoanProductBuilder() {
        }

        public LoanProductBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public LoanProductBuilder code(String code) {
            this.code = code;
            return this;
        }

        public LoanProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LoanProductBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LoanProductBuilder emi(Double emi) {
            this.emi = emi;
            return this;
        }

        public LoanProductBuilder emiType(String emiType) {
            this.emiType = emiType;
            return this;
        }

        public LoanProductBuilder minAmount(Double minAmount) {
            this.minAmount = minAmount;
            return this;
        }

        public LoanProductBuilder maxAmount(Double maxAmount) {
            this.maxAmount = maxAmount;
            return this;
        }

        public LoanProductBuilder interestRatePercent(Double interestRatePercent) {
            this.interestRatePercent = interestRatePercent;
            return this;
        }

        public LoanProductBuilder minTenureMonths(Integer minTenureMonths) {
            this.minTenureMonths = minTenureMonths;
            return this;
        }

        public LoanProductBuilder maxTenureMonths(Integer maxTenureMonths) {
            this.maxTenureMonths = maxTenureMonths;
            return this;
        }

        public LoanProductBuilder penalty(Double penalty) {
            this.penalty = penalty;
            return this;
        }

        public LoanProductBuilder penaltyPercent(Double penaltyPercent) {
            this.penaltyPercent = penaltyPercent;
            return this;
        }

        public LoanProductBuilder interestCalculationFrequency(String interestCalculationFrequency) {
            this.interestCalculationFrequency = interestCalculationFrequency;
            return this;
        }

        public LoanProductBuilder interestCalculationMode(String interestCalculationMode) {
            this.interestCalculationMode = interestCalculationMode;
            return this;
        }

        public LoanProductBuilder processingFeePercent(Double processingFeePercent) {
            this.processingFeePercent = processingFeePercent;
            return this;
        }

        public LoanProductBuilder status(String status) {
            this.status = status;
            return this;
        }

        public LoanProduct build() {
            return new LoanProduct(id, code, name, description, emi, emiType, minAmount, maxAmount,
                    interestRatePercent, minTenureMonths, maxTenureMonths, penalty, penaltyPercent,
                    interestCalculationFrequency, interestCalculationMode, processingFeePercent, status);
        }
    }
}
