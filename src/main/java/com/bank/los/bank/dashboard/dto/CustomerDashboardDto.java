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
@Schema(description = "Customer Portal Dashboard Data")
public class CustomerDashboardDto {

    private String customerName;
    private String customerCode;
    private String bankName;
    private int activeApplicationsCount;
    private int approvedLoansCount;
    private List<Map<String, Object>> loanApplications;
    private List<Map<String, Object>> requiredActionItems;
}
