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
public class MakerDashboardDto {
    private String makerName;
    private String branchName;
    private long draftApplications;
    private long submittedForVerification;
    private long returnedForCorrection;
    private List<Map<String, Object>> recentApplicationsCreated;
    private List<Map<String, Object>> pendingActionItems;
}
