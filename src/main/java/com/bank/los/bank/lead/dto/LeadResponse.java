package com.bank.los.bank.lead.dto;

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
@Schema(description = "Response representing a loan lead")
public class LeadResponse {
    private Long id;
    private String leadNumber;
    private String customerName;
    private String email;
    private String phone;
    private String loanProductType;
    private Double requestedAmount;
    private Long assignedToUserId;
    private Long branchId;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}
