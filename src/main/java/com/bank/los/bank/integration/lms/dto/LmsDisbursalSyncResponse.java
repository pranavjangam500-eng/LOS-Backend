package com.bank.los.bank.integration.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LMS Disbursal Sync Response")
public class LmsDisbursalSyncResponse {
    private String lmsLoanAccountNumber;
    private String applicationNumber;
    private String syncStatus; // SYNCED, PENDING, FAILED
    private BigDecimal disbursedAmount;
    private LocalDateTime synchronizedAt;
    private String remarks;
}
