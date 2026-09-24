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
@Schema(description = "Checker Role Dashboard Data")
public class CheckerDashboardDto {

    private String checkerName;
    private String branchName;
    private int pendingApprovalCount;
    private int approvedTodayCount;
    private int rejectedCount;
    private List<Map<String, Object>> approvalQueue;
    private List<Map<String, Object>> highValueAlerts;
}
