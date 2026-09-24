package com.bank.los.bank.product.dto;

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
@Schema(description = "Request body to configure a loan product")
public class LoanProductRequest {

    @NotBlank(message = "Product code is required")
    private String code;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Minimum amount is required")
    private Double minAmount;

    @NotNull(message = "Maximum amount is required")
    private Double maxAmount;

    @NotNull(message = "Interest rate is required")
    private Double interestRatePercent;

    @NotNull(message = "Minimum tenure is required")
    private Integer minTenureMonths;

    @NotNull(message = "Maximum tenure is required")
    private Integer maxTenureMonths;

    private Double processingFeePercent;
    private String status;
}
