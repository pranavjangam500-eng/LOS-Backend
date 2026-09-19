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
public class TenantAdminDashboardDto {
    private String bankName;
    private String bankCode;
    private long totalBranches;
    private long totalStaffUsers;
    private long totalCustomers;
    private long activeLoanProducts;
    private List<Map<String, Object>> branchSummaries;
    private Map<String, Object> operationalKpis;
}
