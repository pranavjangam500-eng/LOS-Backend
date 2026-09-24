package com.bank.los.bank.loan.dto;

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
@Schema(description = "Response representing a loan application")
public class LoanApplicationResponse {
    private Long id;
    private String applicationNumber;
    private Long customerId;
    private Long branchId;
    private String productCode;
    private Double appliedAmount;
    private Double sanctionedAmount;
    private Double interestRate;
    private Integer tenureMonths;
    private Integer cibilScore;
    private String kycStatus;
    private Long makerUserId;
    private Long checkerUserId;
    private String status;
    private String checkerRemarks;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
}
