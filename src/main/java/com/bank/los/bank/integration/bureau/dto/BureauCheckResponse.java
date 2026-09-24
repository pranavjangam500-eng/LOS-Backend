package com.bank.los.bank.integration.bureau.dto;

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
@Schema(description = "Credit Bureau check response")
public class BureauCheckResponse {
    private String reportId;
    private String bureauType;
    private String panNumber;
    private Integer creditScore;
    private String riskCategory; // LOW_RISK, MEDIUM_RISK, HIGH_RISK
    private Integer totalActiveLoans;
    private Integer defaultAccountsCount;
    private LocalDateTime reportGeneratedAt;
}
