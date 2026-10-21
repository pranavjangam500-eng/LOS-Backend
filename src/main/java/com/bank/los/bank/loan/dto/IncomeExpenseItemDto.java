package com.bank.los.bank.loan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Repeatable row representing an income or expense item")
public class IncomeExpenseItemDto {

    @NotBlank(message = "Type is required (INCOME or EXPENSE)")
    @JsonProperty("type")
    @JsonAlias({"type", "itemType", "Type"})
    private String type; // INCOME or EXPENSE

    @NotBlank(message = "Source is required")
    @JsonProperty("source")
    @JsonAlias({"source", "category", "Source"})
    private String source;

    @NotNull(message = "Monthly amount is required")
    @DecimalMin(value = "0.0", message = "Monthly amount must be non-negative")
    @JsonProperty("monthlyAmount")
    @JsonAlias({"monthlyAmount", "amount", "Monthly Amount"})
    private Double monthlyAmount;

    public IncomeExpenseItemDto() {
    }

    public IncomeExpenseItemDto(String type, String source, Double monthlyAmount) {
        this.type = type;
        this.source = source;
        this.monthlyAmount = monthlyAmount;
    }

    public static IncomeExpenseItemDtoBuilder builder() {
        return new IncomeExpenseItemDtoBuilder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(Double monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public static class IncomeExpenseItemDtoBuilder {
        private String type;
        private String source;
        private Double monthlyAmount;

        IncomeExpenseItemDtoBuilder() {
        }

        public IncomeExpenseItemDtoBuilder type(String type) {
            this.type = type;
            return this;
        }

        public IncomeExpenseItemDtoBuilder source(String source) {
            this.source = source;
            return this;
        }

        public IncomeExpenseItemDtoBuilder monthlyAmount(Double monthlyAmount) {
            this.monthlyAmount = monthlyAmount;
            return this;
        }

        public IncomeExpenseItemDto build() {
            return new IncomeExpenseItemDto(type, source, monthlyAmount);
        }
    }
}
