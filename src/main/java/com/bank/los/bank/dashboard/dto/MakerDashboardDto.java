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
@Schema(description = "Maker Role Dashboard Data")
public class MakerDashboardDto {

    private String makerName;
    private String branchName;
    private int draftApplications;
    private int submittedForVerification;
    private int returnedForCorrection;
    private List<Map<String, Object>> recentApplicationsCreated;
    private List<Map<String, Object>> pendingActionItems;
}
