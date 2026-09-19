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
public class InternalAdminDashboardDto {
    private String panelTitle;
    private long totalOrganizations;
    private long totalActiveBanks;
    private long totalActiveNbfcs;
    private long totalGlobalUsers;
    private List<Map<String, Object>> registeredTenants;
    private Map<String, Object> systemHealth;
}
