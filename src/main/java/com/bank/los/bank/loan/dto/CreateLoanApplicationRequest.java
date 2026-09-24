package com.bank.los.bank.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to initiate a loan application")
public class CreateLoanApplicationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long branchId;

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotNull(message = "Applied amount is required")
    private Double appliedAmount;

    @NotNull(message = "Tenure in months is required")
    private Integer tenureMonths;
}
