package com.bank.los.bank.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response representing a loan product")
public class LoanProductResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Double minAmount;
    private Double maxAmount;
    private Double interestRatePercent;
    private Integer minTenureMonths;
    private Integer maxTenureMonths;
    private Double processingFeePercent;
    private String status;
    private LocalDateTime createdAt;
}
