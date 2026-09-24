package com.bank.los.bank.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Viewer Role Dashboard Data")
public class ViewerDashboardDto {

    private String organizationName;
    private int totalPortfolioSize;
    private double totalDisbursedAmountCr;
    private double defaultRatePercentage;
    private List<Map<String, Object>> portfolioByBranch;
    private List<Map<String, Object>> performanceTrends;
}
