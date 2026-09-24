package com.bank.los.bank.integration.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LMS loan creation & disbursal sync request")
public class LmsDisbursalSyncRequest {

    @NotBlank(message = "Application number is required")
    private String applicationNumber;

    @NotNull(message = "Approved loan amount is required")
    private BigDecimal approvedAmount;

    @NotNull(message = "Interest rate is required")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure in months is required")
    private Integer tenureMonths;

    @NotBlank(message = "Customer account number is required")
    private String customerAccountNumber;

    private String ifscCode;
}
