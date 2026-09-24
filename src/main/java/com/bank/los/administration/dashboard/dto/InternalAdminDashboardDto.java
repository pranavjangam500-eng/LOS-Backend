package com.bank.los.administration.dashboard.dto;

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
@Schema(description = "Platform Master Administration Dashboard Overview")
public class InternalAdminDashboardDto {

    @Schema(description = "Dashboard Panel Title")
    private String panelTitle;

    @Schema(description = "Total registered organizations across all types")
    private int totalOrganizations;

    @Schema(description = "Active commercial banks")
    private long totalActiveBanks;

    @Schema(description = "Active NBFC institutions")
    private long totalActiveNbfcs;

    @Schema(description = "Total registered users across the entire platform")
    private long totalGlobalUsers;

    @Schema(description = "List of all tenant / bank organization profiles")
    private List<Map<String, Object>> registeredOrganizations;

    // Backward-compatible alias
    public List<Map<String, Object>> getRegisteredTenants() {
        return registeredOrganizations;
    }

    public static class InternalAdminDashboardDtoBuilder {
        public InternalAdminDashboardDtoBuilder registeredTenants(List<Map<String, Object>> registeredTenants) {
            this.registeredOrganizations = registeredTenants;
            return this;
        }
    }

    @Schema(description = "System & database routing cluster health status")
    private Map<String, Object> systemHealth;
}
