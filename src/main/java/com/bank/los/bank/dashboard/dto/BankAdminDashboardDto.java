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
@Schema(description = "Bank / NBFC Admin Dashboard Data")
public class BankAdminDashboardDto {

    private String bankName;
    private String bankCode;
    private int totalBranches;
    private long totalStaffUsers;
    private long totalCustomers;
    private int activeLoanProducts;
    private List<Map<String, Object>> branchSummaries;
    private Map<String, Object> operationalKpis;
}
