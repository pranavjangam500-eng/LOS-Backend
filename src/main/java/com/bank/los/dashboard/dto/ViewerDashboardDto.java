package com.bank.los.dashboard.dto;

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
public class ViewerDashboardDto {
    private String organizationName;
    private long totalPortfolioSize;
    private double totalDisbursedAmountCr;
    private double defaultRatePercentage;
    private List<Map<String, Object>> portfolioByBranch;
    private List<Map<String, Object>> performanceTrends;
}
