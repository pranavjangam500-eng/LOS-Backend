package com.bank.los.bank.loan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class IncomeExpenseItem {

    @Column(name = "item_type", length = 20, nullable = false)
    private String type; // INCOME or EXPENSE

    @Column(name = "source", length = 100, nullable = false)
    private String source; // e.g. Salary, Business, Rental, Rent, Utilities, Food

    @Column(name = "monthly_amount", nullable = false)
    private Double monthlyAmount;

    public IncomeExpenseItem() {
    }

    public IncomeExpenseItem(String type, String source, Double monthlyAmount) {
        this.type = type;
        this.source = source;
        this.monthlyAmount = monthlyAmount;
    }

    public static IncomeExpenseItemBuilder builder() {
        return new IncomeExpenseItemBuilder();
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

    public static class IncomeExpenseItemBuilder {
        private String type;
        private String source;
        private Double monthlyAmount;

        IncomeExpenseItemBuilder() {
        }

        public IncomeExpenseItemBuilder type(String type) {
            this.type = type;
            return this;
        }

        public IncomeExpenseItemBuilder source(String source) {
            this.source = source;
            return this;
        }

        public IncomeExpenseItemBuilder monthlyAmount(Double monthlyAmount) {
            this.monthlyAmount = monthlyAmount;
            return this;
        }

        public IncomeExpenseItem build() {
            return new IncomeExpenseItem(type, source, monthlyAmount);
        }
    }
}
