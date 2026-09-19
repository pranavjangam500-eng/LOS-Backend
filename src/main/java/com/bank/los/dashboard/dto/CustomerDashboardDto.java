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
public class CustomerDashboardDto {
    private String customerName;
    private String customerCode;
    private String bankName;
    private long activeApplicationsCount;
    private long approvedLoansCount;
    private List<Map<String, Object>> loanApplications;
    private List<Map<String, Object>> requiredActionItems;
}
